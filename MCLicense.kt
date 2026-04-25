package org.mclicense.library

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

/**
 * A single-class implementation for MC License validation in Kotlin.
 * This is a consolidated version of the official MC License library with all functionality in one file.
 * Perfect for copy-pasting into projects for those who prefer not to have to shade external dependencies.
 *
 * All required dependencies (Paper API, org.json) are already available on the Paper server classpath at
 * runtime, so this class should compile and run without needing to bundle any additional libraries.
 *
 * For usage instructions, see: https://docs.mclicense.org/license-check#checking-a-license
 */
object MCLicense {

    // ==================== Constants ====================
    private val LOGGER: Logger = Logger.getLogger("MCLicense")

    private const val API_BASE_URL = "https://api.mclicense.org"
    private const val API_URL = "$API_BASE_URL/validate/%s/%s"
    private const val HEARTBEAT_URL = "$API_BASE_URL/heartbeat/%s/%s"

    private const val TIMEOUT_MS = 5000
    private const val HEARTBEAT_INTERVAL_SECONDS = 30L

    private const val PUBLIC_KEY =
        "-----BEGIN PUBLIC KEY-----\n" +
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArhw7oQaOrgCzUxDi5D+N\n" +
        "TH9te0JYB1EvW7CEI40+n2drmHJ4+g0CXXYjJc5LyuePskSUPnnHf3UkRvi1GTUd\n" +
        "6Bqi2Jpeu+qbBfm3hg6rcyLUWo8d5MrBQDbVcIvKmQNegTaJGxFRpEFR9XOeHI1g\n" +
        "4dfF+hOfy+1rbEF4p4fgiz0irtKv8l3uSPOKVoEjTL9xnZx4MU5rIsn6W3jee04q\n" +
        "ESPJpCg8nmmZSuJ+9EzzoLnLnUc2/sBuqJ/jexpNfMrXIR11+L8DFqei7J2M7aKi\n" +
        "0KZvQNIqzqTPBCR9VLZPBjFu6cYT/E/WUjjFROuRhi+7Xsa6tKLqoiO4VwJSrn5L\n" +
        "zwIDAQAB\n" +
        "-----END PUBLIC KEY-----"

    private val IS_FOLIA: Boolean = runCatching {
        Class.forName("io.papermc.paper.threadedregions.RegionizedServer")
        true
    }.getOrDefault(false)

    // ==================== Marketplace Provider Fields ====================
    private var pmPlaceholder = "%%__POLYMART__%%"
    private var pmLicense = "%%__LICENSE__%%"
    private var pmUser = "%%__USER__%%"
    private var bbbLicense = "%%__BBB_LICENSE__%%"

    // ==================== Heartbeat Manager Fields ====================
    private var heartbeatRunning = false
    private var heartbeatPluginId: String? = null
    private var heartbeatKey: String? = null
    private var heartbeatSessionId: String? = null
    private var foliaTask: ScheduledTask? = null
    private var bukkitTask: BukkitTask? = null
    private var listenerRegistered = false

    // ==================== Public License Validation Methods ====================

    /**
     * Validates a license key with the MCLicense validation server.
     *
     * Checks the license file, validates with the remote server, verifies the response signature,
     * and ensures the response matches the requested plugin and key.
     *
     * The license key should be placed in a file named 'mclicense.txt' in the plugin's data folder
     * by the user, or be hardcoded by a marketplace.
     *
     * @param plugin The JavaPlugin instance requesting validation
     * @param pluginId The unique identifier assigned to your plugin by MCLicense
     * @return true if the license is valid and active, false otherwise
     */
    @JvmStatic
    fun validateKey(plugin: JavaPlugin, pluginId: String): Boolean {
        return try {
            val licenseFile = File(plugin.dataFolder, "mclicense.txt")
            var fileContent = ""
            if (!licenseFile.exists()) {
                plugin.dataFolder.mkdirs()
                licenseFile.createNewFile()
            } else {
                fileContent = licenseFile.readText(StandardCharsets.UTF_8).trim()
            }

            var key = fileContent
            var usedHardcodedFallback = false
            if (key.isEmpty()) {
                val hardcodedKey = getHardcodedLicense()
                if (hardcodedKey != null) {
                    key = hardcodedKey
                    usedHardcodedFallback = true
                    Files.write(licenseFile.toPath(), key.toByteArray(StandardCharsets.UTF_8))
                } else {
                    LOGGER.info("License key is empty for ${plugin.name}! Place your key in the 'mclicense.txt' file in the plugin folder and restart the server.")
                    return false
                }
            }

            val isValid = validateLicenseWithServer(plugin, pluginId, key, licenseFile)

            if (isValid && !usedHardcodedFallback && key != fileContent) {
                Files.write(licenseFile.toPath(), key.toByteArray(StandardCharsets.UTF_8))
            }

            isValid
        } catch (e: Exception) {
            LOGGER.info("License validation failed for ${plugin.name} (System error)")
            e.printStackTrace()
            false
        }
    }

    /**
     * Writes a license key to the license file and validates it immediately.
     *
     * @param plugin The JavaPlugin instance requesting validation
     * @param pluginId The unique identifier assigned to your plugin by MCLicense
     * @param key The license key to write
     * @return true if the license is valid and active, false otherwise
     */
    @JvmStatic
    fun writeAndValidate(plugin: JavaPlugin, pluginId: String, key: String): Boolean {
        return try {
            val licenseFile = File(plugin.dataFolder, "mclicense.txt")
            if (!licenseFile.exists()) {
                plugin.dataFolder.mkdirs()
                licenseFile.createNewFile()
            }
            Files.write(licenseFile.toPath(), key.toByteArray(StandardCharsets.UTF_8))
            validateLicenseWithServer(plugin, pluginId, key, licenseFile)
        } catch (e: Exception) {
            LOGGER.info("License write and validation failed for ${plugin.name} (System error)")
            e.printStackTrace()
            false
        }
    }

    // ==================== Private License Validation Methods ====================

    private fun validateLicenseWithServer(plugin: JavaPlugin, pluginId: String, key: String, licenseFile: File): Boolean {
        return try {
            val sessionId = UUID.randomUUID().toString()
            val nonce = UUID.randomUUID().toString()

            val encodedPluginId = URLEncoder.encode(pluginId, StandardCharsets.UTF_8.toString()).replace("+", "%20")
            val encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8.toString()).replace("+", "%20")

            var baseUrl = String.format(API_URL, encodedPluginId, encodedKey) +
                    "?sessionId=$sessionId&nonce=$nonce"

            getPolymartUserId()?.let {
                baseUrl += "&polymartUserId=${URLEncoder.encode(it, StandardCharsets.UTF_8.toString())}"
            }

            val connection = URL(baseUrl).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS

            val response: String
            BufferedReader(InputStreamReader(
                if (connection.responseCode >= 400) connection.errorStream else connection.inputStream
            )).use { reader ->
                response = reader.readText()
            }

            if (connection.responseCode != 200) {
                val message = runCatching { JSONObject(response).getString("message") }.getOrDefault("Server error")
                LOGGER.info("License validation failed for ${plugin.name} ($message)")
                return false
            }

            val responseJson = JSONObject(response)

            if (responseJson.getString("nonce") != nonce) {
                LOGGER.info("License validation failed for ${plugin.name} (Nonce mismatch)")
                return false
            }

            if (responseJson.getString("key") != key || responseJson.getString("pluginId") != pluginId) {
                LOGGER.info("License validation failed for ${plugin.name} (Key or pluginId mismatch)")
                return false
            }

            val signature = responseJson.getString("signature")
            val dataToVerify = JSONObject().apply {
                put("key", responseJson.getString("key"))
                put("pluginId", responseJson.getString("pluginId"))
                put("status", responseJson.getString("status"))
                put("message", responseJson.getString("message"))
                put("nonce", responseJson.getString("nonce"))
            }

            val publicKeyPEM = PUBLIC_KEY
                .replace("-----BEGIN PUBLIC KEY-----\n", "")
                .replace("\n-----END PUBLIC KEY-----", "")
                .replace("\n", "")

            val publicKeyBytes = Base64.getDecoder().decode(publicKeyPEM)
            val publicKey = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(publicKeyBytes))

            val sig = Signature.getInstance("SHA256withRSA")
            sig.initVerify(publicKey)
            sig.update(dataToVerify.toString().toByteArray(StandardCharsets.UTF_8))

            val isValid = sig.verify(Base64.getDecoder().decode(signature))
            if (!isValid) {
                LOGGER.info("License validation failed for ${plugin.name} (Signature mismatch)")
                return false
            }

            startHeartbeat(plugin, pluginId, key, sessionId)
            LOGGER.info("License validation succeeded for ${plugin.name}!")
            true
        } catch (e: Exception) {
            LOGGER.info("License validation failed for ${plugin.name} (System error)")
            e.printStackTrace()
            false
        }
    }

    // ==================== Heartbeat Manager Methods ====================

    private fun startHeartbeat(plugin: JavaPlugin, pluginId: String, key: String, sessionId: String) {
        if (heartbeatRunning) killHeartbeat()

        if (!listenerRegistered) {
            plugin.server.pluginManager.registerEvents(ShutdownListenerImpl(plugin), plugin)
            listenerRegistered = true
        }

        heartbeatPluginId = pluginId
        heartbeatKey = key
        heartbeatSessionId = sessionId

        if (IS_FOLIA) {
            foliaTask = Bukkit.getAsyncScheduler().runAtFixedRate(
                plugin, { sendHeartbeat(false) },
                HEARTBEAT_INTERVAL_SECONDS, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS
            )
        } else {
            bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                plugin, Runnable { sendHeartbeat(false) },
                HEARTBEAT_INTERVAL_SECONDS * 20, HEARTBEAT_INTERVAL_SECONDS * 20
            )
        }

        heartbeatRunning = true
    }

    internal fun sendHeartbeat(isShutdown: Boolean) {
        try {
            val connection = URL(String.format(HEARTBEAT_URL, heartbeatPluginId, heartbeatKey))
                .openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.doOutput = true

            val payload = JSONObject().apply {
                put("serverIp", heartbeatSessionId)
                if (isShutdown) put("shutdown", true)
            }

            connection.outputStream.use { os: OutputStream ->
                os.write(payload.toString().toByteArray(StandardCharsets.UTF_8))
            }

            connection.responseCode // trigger the request
        } catch (_: Exception) {
            // Ignore heartbeat errors
        }
    }

    private fun killHeartbeat() {
        sendHeartbeat(true)

        if (IS_FOLIA) {
            foliaTask?.cancel()
            foliaTask = null
        } else {
            bukkitTask?.cancel()
            bukkitTask = null
        }

        heartbeatRunning = false
    }

    // ==================== Marketplace Provider Methods ====================

    private fun getHardcodedLicense(): String? {
        if (!bbbLicense.startsWith("%%__")) return bbbLicense
        if (pmPlaceholder == "1" && !pmLicense.startsWith("%%__")) return "pm_$pmLicense"
        return null
    }

    private fun getPolymartUserId(): String? {
        if (pmPlaceholder == "1" && !pmUser.startsWith("%%__")) return pmUser
        return null
    }

    // ==================== Shutdown Listener ====================

    private class ShutdownListenerImpl(private val activePlugin: JavaPlugin) : Listener {
        @EventHandler
        fun onPluginDisable(event: PluginDisableEvent) {
            if (event.plugin == activePlugin) {
                runCatching { sendHeartbeat(true) }
            }
        }
    }
}
