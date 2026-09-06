package com.leftycraft.chatcontrol.listener;

import com.leftycraft.chatcontrol.ChatControlPlugin;
import com.leftycraft.chatcontrol.util.MessageUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatListener implements Listener {

    private final ChatControlPlugin plugin;
    private final Map<UUID, Long> lastMuteAlertTime = new ConcurrentHashMap<>();

    public ChatListener(@NotNull ChatControlPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean canSenderBypassMute(@NotNull Player sender) {
        if (!plugin.getDataManager().isServerMuted()) {
            return true;
        }

        if (plugin.getConfigManager().isAllowPermissionBypass()) {
            if (sender.hasPermission("chatcontrol.bypass.muteall") || sender.hasPermission("chatcontrol.admin")) {
                return true;
            }
        }

        return plugin.getDataManager().isWhitelisted(sender.getUniqueId());
    }

    private void sendMuteAlertIfNeeded(@NotNull Player sender) {
        long now = System.currentTimeMillis();
        Long lastTime = lastMuteAlertTime.get(sender.getUniqueId());
        if (lastTime == null || (now - lastTime) > 800) {
            lastMuteAlertTime.put(sender.getUniqueId(), now);
            String msg = plugin.getConfigManager().getMessage("server-is-muted");
            MessageUtil.sendMessage(sender, plugin.getConfigManager().getPrefix(), msg);
        }
    }

    // =========================================================================
    // 1. Legacy Spigot Event (Used by PlayMoreSounds and other plugins)
    // =========================================================================

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerChatLowest(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();

        // Check if server is muted
        if (!canSenderBypassMute(sender)) {
            event.setCancelled(true);
            try {
                event.getRecipients().clear();
            } catch (UnsupportedOperationException ignored) {
            }
            sendMuteAlertIfNeeded(sender);
            return;
        }

        // Filter recipients for per-player chat toggle
        boolean senderBypassesToggle = sender.hasPermission("chatcontrol.bypass.toggle");
        if (!senderBypassesToggle) {
            try {
                // Filter out any recipient whose chat is disabled (except the sender themselves)
                event.getRecipients().removeIf(recipient ->
                        !recipient.getUniqueId().equals(sender.getUniqueId())
                                && plugin.getDataManager().isChatDisabled(recipient.getUniqueId())
                );
            } catch (Exception ignored) {
            }
        }
    }

    // =========================================================================
    // 2. Modern Paper AsyncChatEvent
    // =========================================================================

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncChatLowest(AsyncChatEvent event) {
        Player sender = event.getPlayer();

        // Check if server is muted
        if (!canSenderBypassMute(sender)) {
            event.setCancelled(true);
            try {
                event.viewers().clear();
            } catch (UnsupportedOperationException ignored) {
            }
            sendMuteAlertIfNeeded(sender);
            return;
        }

        // Filter viewers for per-player chat toggle
        boolean senderBypassesToggle = sender.hasPermission("chatcontrol.bypass.toggle");

        List<Audience> viewersToRemove = new ArrayList<>();

        for (Audience audience : event.viewers()) {
            if (audience instanceof Player viewerPlayer) {
                // The sender always sees their own message
                if (viewerPlayer.getUniqueId().equals(sender.getUniqueId())) {
                    continue;
                }

                // If viewer disabled chat and sender does not bypass
                if (!senderBypassesToggle && plugin.getDataManager().isChatDisabled(viewerPlayer.getUniqueId())) {
                    viewersToRemove.add(viewerPlayer);
                }
            }
        }

        // Remove viewers who have chat disabled
        for (Audience toRemove : viewersToRemove) {
            event.viewers().remove(toRemove);
        }
    }
}