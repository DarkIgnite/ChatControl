package com.leftycraft.chatcontrol.hook;

import com.leftycraft.chatcontrol.ChatControlPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChatControlExpansion extends PlaceholderExpansion {

    private final ChatControlPlugin plugin;

    public ChatControlExpansion(@NotNull ChatControlPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "chatcontrol";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Leftycraft";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        // %chatcontrol_status% -> "enabled" (can see public chat) or "disabled" (chat muted)
        if (params.equalsIgnoreCase("status") || params.equalsIgnoreCase("chat")) {
            return plugin.getDataManager().isChatDisabled(player.getUniqueId()) ? "disabled" : "enabled";
        }

        // %chatcontrol_system% -> "enabled" (can see system messages) or "disabled" (system messages hidden)
        if (params.equalsIgnoreCase("system") || params.equalsIgnoreCase("system_status")) {
            return plugin.getDataManager().isSystemDisabled(player.getUniqueId()) ? "disabled" : "enabled";
        }

        // %chatcontrol_server_mute% -> "true" or "false"
        if (params.equalsIgnoreCase("server_mute") || params.equalsIgnoreCase("muted")) {
            return String.valueOf(plugin.getDataManager().isServerMuted());
        }

        return null;
    }
}
