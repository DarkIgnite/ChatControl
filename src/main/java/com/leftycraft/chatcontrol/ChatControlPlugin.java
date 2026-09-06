package com.leftycraft.chatcontrol;

import com.leftycraft.chatcontrol.command.ChatCommand;
import com.leftycraft.chatcontrol.config.ConfigManager;
import com.leftycraft.chatcontrol.data.DataManager;
import com.leftycraft.chatcontrol.listener.ChatListener;
import com.leftycraft.chatcontrol.listener.PlayerConnectionListener;
import com.leftycraft.chatcontrol.manager.SoundManager;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

public final class ChatControlPlugin extends JavaPlugin {

    private static ChatControlPlugin instance;

    private ConfigManager configManager;
    private DataManager dataManager;
    private SoundManager soundManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize configuration and storage
        this.configManager = new ConfigManager(this);
        this.dataManager = new DataManager(this);
        this.soundManager = new SoundManager(this);

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

        // Check for PlayMoreSounds
        if (getServer().getPluginManager().isPluginEnabled("PlayMoreSounds")) {
            getLogger().info("PlayMoreSounds detected! Chat sound integration active.");
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

    public SoundManager getSoundManager() {
        return soundManager;
    }
}
