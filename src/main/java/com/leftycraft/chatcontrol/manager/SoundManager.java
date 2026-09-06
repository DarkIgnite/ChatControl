package com.leftycraft.chatcontrol.manager;

import com.leftycraft.chatcontrol.ChatControlPlugin;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SoundManager {

    private final ChatControlPlugin plugin;

    public SoundManager(@NotNull ChatControlPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Plays the chat notification sound to a specific player if their chat is enabled.
     *
     * @param player The target player
     */
    public void playChatSound(@NotNull Player player) {
        if (!plugin.getConfigManager().isSoundEnabled()) {
            return;
        }

        // If configured to only play sound when chat is enabled, check player state
        if (plugin.getConfigManager().isSoundOnlyForEnabledChat()) {
            if (plugin.getDataManager().isChatDisabled(player.getUniqueId())) {
                return; // Chat is off, DO NOT play sound!
            }
        }

        String soundKeyStr = plugin.getConfigManager().getSoundName();
        if (soundKeyStr == null || soundKeyStr.trim().isEmpty()) {
            return;
        }

        float volume = plugin.getConfigManager().getSoundVolume();
        float pitch = plugin.getConfigManager().getSoundPitch();

        try {
            Key key;
            if (soundKeyStr.contains(":")) {
                String[] parts = soundKeyStr.split(":", 2);
                key = Key.key(parts[0].toLowerCase(), parts[1].toLowerCase());
            } else {
                key = Key.key("minecraft", soundKeyStr.toLowerCase().replace('_', '.'));
            }

            Sound sound = Sound.sound(key, Sound.Source.PLAYER, volume, pitch);
            player.playSound(sound);
        } catch (Exception e) {
            // Fallback to Bukkit sound if key format differs
            try {
                org.bukkit.Sound bukkitSound = org.bukkit.Sound.valueOf(soundKeyStr.toUpperCase().replace('.', '_').replace("MINECRAFT:", ""));
                player.playSound(player.getLocation(), bukkitSound, volume, pitch);
            } catch (Exception ignored) {
            }
        }
    }
}
