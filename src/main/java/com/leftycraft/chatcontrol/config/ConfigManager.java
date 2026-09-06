package com.leftycraft.chatcontrol.config;

import com.leftycraft.chatcontrol.ChatControlPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final ChatControlPlugin plugin;
    private FileConfiguration config;

    private String prefix;
    private boolean serverMutePersistState;
    private boolean allowPermissionBypass;
    private int clearChatLines;

    private final Map<String, String> messages = new HashMap<>();

    public ConfigManager(@NotNull ChatControlPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();

        this.prefix = config.getString("prefix", "<gradient:#00d2ff:#3a7bd5>[LeftyChat]</gradient> &8» &r");
        this.serverMutePersistState = config.getBoolean("server-mute.persist-state", true);
        this.allowPermissionBypass = config.getBoolean("server-mute.allow-permission-bypass", true);
        this.clearChatLines = config.getInt("clear-chat.lines", 100);

        this.messages.clear();
        if (config.isConfigurationSection("messages")) {
            for (String key : config.getConfigurationSection("messages").getKeys(false)) {
                this.messages.put(key, config.getString("messages." + key, ""));
            }
        }
    }

    @NotNull
    public String getPrefix() {
        return prefix;
    }

    public boolean isServerMutePersistState() {
        return serverMutePersistState;
    }

    public boolean isAllowPermissionBypass() {
        return allowPermissionBypass;
    }

    public int getClearChatLines() {
        return clearChatLines;
    }

    @NotNull
    public String getMessage(@NotNull String key) {
        return messages.getOrDefault(key, key);
    }

    @NotNull
    public String getMessage(@NotNull String key, @NotNull Map<String, String> replacements) {
        String msg = getMessage(key);
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            msg = msg.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return msg;
    }

    @NotNull
    public String getMessage(@NotNull String key, @NotNull String placeholder, @NotNull String value) {
        return getMessage(key).replace("%" + placeholder + "%", value);
    }
}