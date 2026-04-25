# MCLicense - Single Class

A consolidated, single-file version of the [MCLicense library](https://docs.mclicense.org).

Copy `MCLicense.java` (Java) or `MCLicense.kt` (Kotlin) directly into your project — no shading or extra dependencies required. All dependencies (`org.json`, Paper API) are already present on the Paper server classpath at runtime.

## How to use

See the docs: https://docs.mclicense.org/license-check#checking-a-license

## Notes

- Supports both Bukkit/Spigot/Paper and Folia schedulers
- Marketplace integrations (Polymart, BuiltByBit) are handled automatically via placeholder replacement
- The `mclicense.txt` license file is created in your plugin's data folder if it doesn't exist
