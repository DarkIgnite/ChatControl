package com.leftycraft.chatcontrol.listener;

import com.leftycraft.chatcontrol.ChatControlPlugin;
import com.leftycraft.chatcontrol.util.MessageUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ChatListener implements Listener {

    private final ChatControlPlugin plugin;

    public ChatListener(@NotNull ChatControlPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();

        // 1. Check Server Mute Status
        if (plugin.getDataManager().isServerMuted()) {
            boolean canBypass = false;

            // Check admin/bypass permission
            if (plugin.getConfigManager().isAllowPermissionBypass()) {
                if (sender.hasPermission("chatcontrol.bypass.muteall") || sender.hasPermission("chatcontrol.admin")) {
                    canBypass = true;
                }
            }

            // Check whitelist
            if (!canBypass && plugin.getDataManager().isWhitelisted(sender.getUniqueId())) {
                canBypass = true;
            }

            if (!canBypass) {
                event.setCancelled(true);
                String msg = plugin.getConfigManager().getMessage("server-is-muted");
                MessageUtil.sendMessage(sender, plugin.getConfigManager().getPrefix(), msg);
                return;
            }
        }

        // 2. Filter Viewers based on per-player chat toggle
        boolean senderBypassesToggle = sender.hasPermission("chatcontrol.bypass.toggle");

        List<Audience> viewersToRemove = new ArrayList<>();
        List<Player> eligibleSoundRecipients = new ArrayList<>();

        for (Audience audience : event.viewers()) {
            if (audience instanceof Player viewerPlayer) {
                // Sender always sees their own message
                if (viewerPlayer.getUniqueId().equals(sender.getUniqueId())) {
                    eligibleSoundRecipients.add(viewerPlayer);
                    continue;
                }

                // If viewer has disabled public chat
                if (plugin.getDataManager().isChatDisabled(viewerPlayer.getUniqueId())) {
                    if (!senderBypassesToggle) {
                        viewersToRemove.add(viewerPlayer);
                        continue; // Do NOT play sound and remove from viewers
                    }
                }

                eligibleSoundRecipients.add(viewerPlayer);
            }
        }

        // Remove viewers who turned off chat
        for (Audience toRemove : viewersToRemove) {
            event.viewers().remove(toRemove);
        }

        // 3. Play chat sound only to eligible players who have chat enabled
        if (plugin.getConfigManager().isSoundEnabled()) {
            for (Player recipient : eligibleSoundRecipients) {
                plugin.getSoundManager().playChatSound(recipient);
            }
        }
    }
}
