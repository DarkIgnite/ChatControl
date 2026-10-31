package com.leftycraft.chatcontrol.data;

import com.leftycraft.chatcontrol.ChatControlPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class DataManager {

    private final ChatControlPlugin plugin;
    private final File dataFile;
    private YamlConfiguration dataConfig;

    // UUIDs of players who have disabled public chat (only want to see own chat)
    private final Set<UUID> disabledChatPlayers = ConcurrentHashMap.newKeySet();

    // UUIDs of players who have hidden system messages (AutoMessage, Clan War, Dungeon, ChatGames)
    private final Set<UUID> disabledSystemPlayers = ConcurrentHashMap.newKeySet();

    // Global server mute state
    private boolean serverMuted = false;

    // Whitelist for server mute bypass (UUID -> Username)
    private final Map<UUID, String> whitelist = new ConcurrentHashMap<>();

    public DataManager(@NotNull ChatControlPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        loadData();
    }

    public synchronized void loadData() {
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create data.yml", e);
            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        // Load disabled chat players
        disabledChatPlayers.clear();
        List<String> disabledList = dataConfig.getStringList("disabled-chat-players");
        for (String uuidStr : disabledList) {
            try {
                disabledChatPlayers.add(UUID.fromString(uuidStr));
            } catch (IllegalArgumentException ignored) {
            }
        }

        // Load disabled system messages players
        disabledSystemPlayers.clear();
        List<String> disabledSysList = dataConfig.getStringList("disabled-system-players");
        for (String uuidStr : disabledSysList) {
            try {
                disabledSystemPlayers.add(UUID.fromString(uuidStr));
            } catch (IllegalArgumentException ignored) {
            }
        }

        // Load server mute state
        if (plugin.getConfigManager().isServerMutePersistState()) {
            this.serverMuted = dataConfig.getBoolean("server-muted", false);
        } else {
            this.serverMuted = false;
        }

        // Load whitelist
        whitelist.clear();
        if (dataConfig.isConfigurationSection("whitelist")) {
            for (String key : dataConfig.getConfigurationSection("whitelist").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    String name = dataConfig.getString("whitelist." + key, "Unknown");
                    whitelist.put(uuid, name);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    public synchronized void saveData() {
        if (dataConfig == null) {
            dataConfig = new YamlConfiguration();
        }

        List<String> disabledList = new ArrayList<>();
        for (UUID uuid : disabledChatPlayers) {
            disabledList.add(uuid.toString());
        }
        dataConfig.set("disabled-chat-players", disabledList);

        List<String> disabledSysList = new ArrayList<>();
        for (UUID uuid : disabledSystemPlayers) {
            disabledSysList.add(uuid.toString());
        }
        dataConfig.set("disabled-system-players", disabledSysList);
        dataConfig.set("server-muted", serverMuted);

        dataConfig.set("whitelist", null); // clear
        for (Map.Entry<UUID, String> entry : whitelist.entrySet()) {
            dataConfig.set("whitelist." + entry.getKey().toString(), entry.getValue());
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save data.yml", e);
        }
    }

    public void saveDataAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveData);
    }

    public boolean isChatDisabled(@NotNull UUID uuid) {
        return disabledChatPlayers.contains(uuid);
    }

    public boolean toggleChat(@NotNull UUID uuid) {
        if (disabledChatPlayers.contains(uuid)) {
            disabledChatPlayers.remove(uuid);
            saveDataAsync();
            return true; // Now enabled
        } else {
            disabledChatPlayers.add(uuid);
            saveDataAsync();
            return false; // Now disabled
        }
    }

    public void setChatDisabled(@NotNull UUID uuid, boolean disabled) {
        if (disabled) {
            disabledChatPlayers.add(uuid);
        } else {
            disabledChatPlayers.remove(uuid);
        }
        saveDataAsync();
    }

    public boolean isSystemDisabled(@NotNull UUID uuid) {
        return disabledSystemPlayers.contains(uuid);
    }

    public boolean toggleSystem(@NotNull UUID uuid) {
        if (disabledSystemPlayers.contains(uuid)) {
            disabledSystemPlayers.remove(uuid);
            saveDataAsync();
            return true; // Now enabled
        } else {
            disabledSystemPlayers.add(uuid);
            saveDataAsync();
            return false; // Now disabled
        }
    }

    public void setSystemDisabled(@NotNull UUID uuid, boolean disabled) {
        if (disabled) {
            disabledSystemPlayers.add(uuid);
        } else {
            disabledSystemPlayers.remove(uuid);
        }
        saveDataAsync();
    }

    public boolean isServerMuted() {
        return serverMuted;
    }

    public void setServerMuted(boolean serverMuted) {
        this.serverMuted = serverMuted;
        saveDataAsync();
    }

    public boolean isWhitelisted(@NotNull UUID uuid) {
        return whitelist.containsKey(uuid);
    }

    public boolean isWhitelisted(@NotNull String name) {
        for (String whitelistedName : whitelist.values()) {
            if (whitelistedName.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public void addWhitelist(@NotNull UUID uuid, @NotNull String name) {
        whitelist.put(uuid, name);
        saveDataAsync();
    }

    public boolean removeWhitelist(@NotNull UUID uuid) {
        boolean removed = whitelist.remove(uuid) != null;
        if (removed) {
            saveDataAsync();
        }
        return removed;
    }

    @Nullable
    public UUID removeWhitelistByName(@NotNull String name) {
        UUID target = null;
        for (Map.Entry<UUID, String> entry : whitelist.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(name)) {
                target = entry.getKey();
                break;
            }
        }
        if (target != null) {
            whitelist.remove(target);
            saveDataAsync();
        }
        return target;
    }

    public void clearWhitelist() {
        whitelist.clear();
        saveDataAsync();
    }

    @NotNull
    public Map<UUID, String> getWhitelist() {
        return Collections.unmodifiableMap(whitelist);
    }
}
