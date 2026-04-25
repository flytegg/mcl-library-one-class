# Copy-Paste Guide

This guide explains how to copy the MCLicense class into your existing Minecraft plugin project.

## Step-by-Step Instructions

### Step 1: Locate the MCLicense.java file

Find the `MCLicense.java` file in this repository:

```
mcl-library-one-class/
└── src/main/java/org/mclicense/library/
    └── MCLicense.java
```

### Step 2: Create the package directory in your project

If you don't already have an `org.mclicense.library` package, create the directory structure:

```
your-plugin/
└── src/main/java/
    └── org/
        └── mclicense/
            └── library/
```

For Maven projects, this is typically `src/main/java/org/mclicense/library/`

### Step 3: Copy MCLicense.java

Copy the `MCLicense.java` file into your `org/mclicense/library/` directory.

### Step 4: Add Dependencies

Add the required dependencies to your `pom.xml` or `build.gradle`:

**Maven (pom.xml):**

```xml
<dependencies>
    <dependency>
        <groupId>io.papermc.paper</groupId>
        <artifactId>paper-api</artifactId>
        <version>1.20.1-R0.1-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>org.json</groupId>
        <artifactId>json</artifactId>
        <version>20240303</version>
    </dependency>
</dependencies>

<repositories>
    <repository>
        <id>papermc-repo</id>
        <url>https://repo.papermc.io/repository/maven-public/</url>
    </repository>
</repositories>
```

**Gradle (build.gradle):**

```gradle
dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    implementation("org.json:json:20240303")
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    mavenCentral()
}
```

### Step 5: Use the MCLicense class

In your main plugin class, import and use MCLicense:

```java
import org.mclicense.library.MCLicense;
import org.bukkit.plugin.java.JavaPlugin;

public class YourPlugin extends JavaPlugin {
    
    @Override
    public void onEnable() {
        if (MCLicense.validateKey(this, "your-plugin-id")) {
            getLogger().info("License validated!");
        } else {
            setEnabled(false);
        }
    }
}
```

### Step 6: Build your project

Build your plugin as usual:

- **Maven**: `mvn clean package`
- **Gradle**: `gradle build`

## Verification

After copying, you should be able to:

1. ✓ Build without errors
2. ✓ Import `org.mclicense.library.MCLicense` in your code
3. ✓ Call `MCLicense.validateKey()` and `MCLicense.writeAndValidate()`
4. ✓ See license validation logs when the plugin starts

## Troubleshooting

### "Cannot find symbol: class MCLicense"

- Ensure you've placed MCLicense.java in the correct package directory
- Rebuild your project: `mvn clean build` or `gradle clean build`

### "Missing required dependencies"

- Make sure you've added the Paper API and org.json dependencies to your build file
- Add the PaperMC repository to your pom.xml or build.gradle

### "Unsupported class version"

- Ensure your project uses Java 11 or higher (target 1.8+ compatible with the library)
- Update your Maven compiler plugin or Gradle toolchain settings

## Package Structure Example

If your plugin is called "MyPlugin", your final structure should look like:

```
MyPlugin/
├── src/main/java/
│   ├── org/
│   │   ├── example/
│   │   │   └── MyPlugin.java         (your main plugin class)
│   │   └── mclicense/
│   │       └── library/
│   │           └── MCLicense.java    (copied from this repo)
│   └── resources/
│       └── plugin.yml
├── pom.xml
└── build.gradle
```

## Support

For integration issues or questions about the MCLicense library, refer to:
- Official docs: https://docs.mclicense.org
- GitHub: https://github.com/MCLicense/
