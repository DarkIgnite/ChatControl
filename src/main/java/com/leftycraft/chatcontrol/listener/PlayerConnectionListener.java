package com.leftycraft.chatcontrol.listener;

import com.leftycraft.chatcontrol.ChatControlPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerConnectionListener implements Listener {

    private final ChatControlPlugin plugin;

    public PlayerConnectionListener(@NotNull ChatControlPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Ensure data is synced when players leave
    }
}
