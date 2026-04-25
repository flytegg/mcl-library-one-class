A consolidated, single-file version of the [MC License library](https://github.com/flytegg/mcl-library), aimed at developers who are having trouble shading the library into their plugin via Gradle/Maven, or simply prefer not to manage external dependencies at all.

Copy `MCLicense.java` (Java) or `MCLicense.kt` (Kotlin) directly into your project — no shading or extra dependencies required. All dependencies (`org.json`, Spigot/Paper API) are already present on the server classpath at runtime.

> **Note:** By copy-pasting it you'll need to manually check back here for any updates. It's not as simple as bumping a version number in your build tool, so bear that in mind.

## How to use

See the docs: https://docs.mclicense.org/license-check#checking-a-license