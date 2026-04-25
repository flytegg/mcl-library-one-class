# Quick Start

Get up and running with MCLicense in 5 minutes!

## For Copy-Paste Users

### 1. Copy the File
Copy `src/main/java/org/mclicense/library/MCLicense.java` into your project:
```
your-plugin/src/main/java/org/mclicense/library/MCLicense.java
```

No extra dependencies needed — Paper and `org.json` are already on the Paper server classpath.

### 2. Use in Your Plugin
```java
import org.mclicense.library.MCLicense;

public class MyPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        if (MCLicense.validateKey(this, "my-plugin-id")) {
            getLogger().info("License OK!");
        } else {
            setEnabled(false);
        }
    }
}
```

### 3. Add License File
Players place their license key in: `plugins/MyPlugin/mclicense.txt`

That's it! Your plugin is licensed.

## Common Patterns

### Disable Plugin on Invalid License
```java
if (!MCLicense.validateKey(this, "plugin-id")) {
    setEnabled(false);
    return;
}
```

### Set License Programmatically
```java
MCLicense.writeAndValidate(this, "plugin-id", "user-provided-key");
```

### Add Licensing Command
```java
@Override
public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (cmd.getName().equals("license")) {
        MCLicense.writeAndValidate(this, "plugin-id", args[0]);
        return true;
    }
    return false;
}
```

## Where to Get Your Plugin ID

1. Go to: https://mclicense.org/dashboard
2. Create a new plugin
3. Copy your **Plugin ID**
4. Replace `"plugin-id"` in the code above

## What Happens Automatically

✓ License validated with MCLicense servers  
✓ License key signature verified  
✓ Heartbeat sent every 30 seconds  
✓ Session tracked on server  
✓ Shutdown notification sent  

## License File Format

The `mclicense.txt` file contains just the raw license key:
```
your-license-key-here
```

## Troubleshooting

| Problem | Solution |
|---------|----------|
| "Cannot find MCLicense" | Ensure file is in `org/mclicense/library/` package |
| "json not found" | Add org.json dependency |
| "License always fails" | Check plugin ID matches dashboard |
| "File not created" | Check folder permissions |

## Full Examples

See the `examples/` folder for complete working examples:
- `BasicUsageExample.java` - Simple validation in onEnable
- `ProgrammaticUsageExample.java` - License activation command

## Get Help

- **Docs**: https://docs.mclicense.org
- **Dashboard**: https://mclicense.org/dashboard
