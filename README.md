# MCLicense - Single Class Version

This is a single-class implementation of the MCLicense library, designed for developers who prefer to copy-paste the code directly into their projects instead of managing build tool dependencies.

## Why a Single Class?

The single-class version consolidates all functionality from the modular MCLicense library into one file (`MCLicense.java`). This is perfect for:

- Projects that can't easily integrate Maven/Gradle dependencies
- Quick integration without build tool configuration
- Copy-paste deployment scenarios
- Custom build systems

## Features

✅ License key validation with MCLicense servers  
✅ RSA signature verification for security  
✅ Automatic heartbeat monitoring  
✅ Marketplace integration (Polymart, BuiltByBit)  
✅ Folia support (async scheduler compatibility)  
✅ Spigot/Paper/Bukkit compatibility  

## Usage

### 1. Copy the Class

Copy `src/main/java/org/mclicense/library/MCLicense.java` into your project at `org/mclicense/library/MCLicense.java`. That's it — no extra dependencies needed, Paper and `org.json` are already on the Paper server classpath.

### 2. Use in Your Plugin

```java
import org.mclicense.library.MCLicense;
import org.bukkit.plugin.java.JavaPlugin;

public class MyPlugin extends JavaPlugin {
    
    @Override
    public void onEnable() {
        String pluginId = "your-plugin-id"; // Get from MCLicense dashboard
        
        if (MCLicense.validateKey(this, pluginId)) {
            getLogger().info("License validated! Plugin is ready.");
        } else {
            getLogger().info("License validation failed. Plugin disabled.");
            setEnabled(false);
        }
    }
}
```

## API Reference

### License Validation

#### `validateKey(JavaPlugin plugin, String pluginId)`

Validates the license key from `mclicense.txt` in the plugin's data folder.

```java
boolean isValid = MCLicense.validateKey(this, "your-plugin-id");
```

- **Returns**: `true` if valid and active, `false` otherwise
- **Side Effects**: Starts automatic heartbeat if successful

#### `writeAndValidate(JavaPlugin plugin, String pluginId, String key)`

Writes a license key to the license file and validates it immediately.

```java
boolean isValid = MCLicense.writeAndValidate(this, "your-plugin-id", "your-license-key");
```

- **Returns**: `true` if valid and active, `false` otherwise
- **Use Case**: Programmatic license configuration

## License File Location

The library looks for `mclicense.txt` in your plugin's data folder:

```
plugins/
  YourPlugin/
    mclicense.txt
```

If the file doesn't exist, it will be created automatically. Users can then place their license key there.

## Marketplace Integration

For Polymart and BuiltByBit marketplace distributions, the license key is automatically embedded via placeholder replacement:

- `%%__POLYMART__%%` - Set to `1` for Polymart
- `%%__LICENSE__%%` - Polymart license key
- `%%__USER__%%` - Polymart user ID
- `%%__BBB_LICENSE__%%` - BuiltByBit license key

## What's Included

This single class contains:

- **License Validation** - Communicates with MCLicense servers and verifies cryptographic signatures
- **Heartbeat Manager** - Keeps the license session alive on the server
- **Marketplace Provider** - Handles marketplace-specific license embedding
- **Shutdown Listener** - Automatically notifies the server when the plugin disables
- **Constants** - RSA public key, API URLs, and configuration

## Differences from the Library Version

This version consolidates all the modular classes into one file. Functionality is identical:

| Modular Version | Single Class Version |
|---|---|
| Constants.java | Embedded as static fields |
| MCLicense.java | Main public methods |
| HeartbeatManager.java | `startHeartbeat()`, `sendHeartbeat()` |
| MarketplaceProvider.java | `getHardcodedLicense()`, `getPolymartUserId()` |
| ShutdownListener.java | `ShutdownListenerImpl` inner class |

## Documentation

For complete documentation and integration guides, visit: https://docs.mclicense.org

## Support

For issues or questions about MCLicense, visit the documentation or contact support through the MCLicense dashboard.
