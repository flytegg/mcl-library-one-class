package org.example;

import org.bukkit.plugin.java.JavaPlugin;
import org.mclicense.library.MCLicense;

/**
 * Example: Basic License Validation
 * 
 * This example shows the simplest way to validate a license in your onEnable() method.
 */
public class BasicUsageExample extends JavaPlugin {

    @Override
    public void onEnable() {
        // Replace "your-plugin-id" with your actual plugin ID from MCLicense dashboard
        String pluginId = "your-plugin-id";
        
        // Validate the license
        if (MCLicense.validateKey(this, pluginId)) {
            getLogger().info("✓ License validated! Your plugin is ready to use.");
            // Enable all plugin functionality
        } else {
            getLogger().warning("✗ License validation failed. Plugin will be disabled.");
            // Disable the plugin
            setEnabled(false);
        }
    }
}
