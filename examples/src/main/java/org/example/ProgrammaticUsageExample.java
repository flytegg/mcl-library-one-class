package org.example;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.mclicense.library.MCLicense;

/**
 * Example: Programmatic License Configuration
 * 
 * This example shows how to validate a license key programmatically
 * via a command (useful for in-game license activation).
 */
public class ProgrammaticUsageExample extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        // Register the command handler
        getCommand("setlicense").setExecutor(this);
        getLogger().info("License plugin loaded. Use /setlicense <key> to activate.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("setlicense")) {
            return false;
        }

        // Check permission
        if (!sender.hasPermission("plugin.setlicense")) {
            sender.sendMessage("You don't have permission to use this command.");
            return true;
        }

        // Validate arguments
        if (args.length < 1) {
            sender.sendMessage("Usage: /setlicense <license-key>");
            return true;
        }

        String licenseKey = args[0];
        String pluginId = "your-plugin-id"; // Replace with your actual plugin ID

        // Validate the license programmatically
        sender.sendMessage("Validating license key...");
        
        if (MCLicense.writeAndValidate(this, pluginId, licenseKey)) {
            sender.sendMessage("✓ License validated successfully!");
        } else {
            sender.sendMessage("✗ License validation failed. Please check your license key.");
        }

        return true;
    }
}
