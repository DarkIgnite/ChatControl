package com.leftycraft.chatcontrol;

import com.leftycraft.chatcontrol.command.ChatCommand;
import com.leftycraft.chatcontrol.config.ConfigManager;
import com.leftycraft.chatcontrol.data.DataManager;
import com.leftycraft.chatcontrol.hook.ChatControlExpansion;
import com.leftycraft.chatcontrol.listener.ChatListener;
import com.leftycraft.chatcontrol.listener.PlayerConnectionListener;
import com.leftycraft.chatcontrol.listener.SystemMessagePacketListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChatControlPlugin extends JavaPlugin {

    private static ChatControlPlugin instance;

    private ConfigManager configManager;
    private DataManager dataManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize configuration and data storage
        this.configManager = new ConfigManager(this);
        this.dataManager = new DataManager(this);

        // Register commands
        ChatCommand chatCommand = new ChatCommand(this);
        PluginCommand cmd = getCommand("chat");
        if (cmd != null) {
            cmd.setExecutor(chatCommand);
            cmd.setTabCompleter(chatCommand);
        } else {
            getLogger().severe("Failed to register /chat command!");
        }

        // Register event listeners
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);

        // Register PlaceholderAPI expansion if present
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new ChatControlExpansion(this).register();
            getLogger().info("Hooked into PlaceholderAPI expansion (%chatcontrol_status%, %chatcontrol_system%).");
        }

        // Register ProtocolLib packet listener if present
        if (getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            SystemMessagePacketListener.register(this);
        }

        getLogger().info("========================================");
        getLogger().info(" ChatControl v" + getPluginMeta().getVersion() + " by Leftycraft");
        getLogger().info(" Successfully enabled for Paper 1.21+");
        getLogger().info("========================================");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveData();
        }
        getLogger().info("ChatControl disabled successfully.");
        instance = null;
    }

    public static ChatControlPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}