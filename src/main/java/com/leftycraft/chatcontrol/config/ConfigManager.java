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
    private boolean soundEnabled;
    private String soundName;
    private float soundVolume;
    private float soundPitch;
    private boolean soundOnlyForEnabledChat;
    private boolean playMoreSoundsIntegration;

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

        this.soundEnabled = config.getBoolean("sound.enabled", true);
        this.soundName = config.getString("sound.name", "minecraft:entity.experience_orb.pickup");
        this.soundVolume = (float) config.getDouble("sound.volume", 1.0);
        this.soundPitch = (float) config.getDouble("sound.pitch", 1.0);
        this.soundOnlyForEnabledChat = config.getBoolean("sound.only-for-enabled-chat", true);
        this.playMoreSoundsIntegration = config.getBoolean("sound.playmoresounds-integration", true);

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

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    @NotNull
    public String getSoundName() {
        return soundName;
    }

    public float getSoundVolume() {
        return soundVolume;
    }

    public float getSoundPitch() {
        return soundPitch;
    }

    public boolean isSoundOnlyForEnabledChat() {
        return soundOnlyForEnabledChat;
    }

    public boolean isPlayMoreSoundsIntegration() {
        return playMoreSoundsIntegration;
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
