package com.leftycraft.chatcontrol.command;

import com.leftycraft.chatcontrol.ChatControlPlugin;
import com.leftycraft.chatcontrol.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ChatCommand implements CommandExecutor, TabCompleter {

    private final ChatControlPlugin plugin;

    public ChatCommand(@NotNull ChatControlPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("chatcontrol.use")) {
            sendMsg(sender, "no-permission");
            return true;
        }

        if (args.length == 0) {
            // Default action for player is toggle or show help
            if (sender instanceof Player player) {
                handleToggle(player, null);
            } else {
                sendHelp(sender);
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "toggle" -> {
                if (!(sender instanceof Player player)) {
                    sendMsg(sender, "player-only");
                    return true;
                }
                if (!player.hasPermission("chatcontrol.toggle")) {
                    sendMsg(player, "no-permission");
                    return true;
                }
                handleToggle(player, null);
            }
            case "on" -> {
                if (!(sender instanceof Player player)) {
                    sendMsg(sender, "player-only");
                    return true;
                }
                if (!player.hasPermission("chatcontrol.toggle")) {
                    sendMsg(player, "no-permission");
                    return true;
                }
                handleToggle(player, true);
            }
            case "off" -> {
                if (!(sender instanceof Player player)) {
                    sendMsg(sender, "player-only");
                    return true;
                }
                if (!player.hasPermission("chatcontrol.toggle")) {
                    sendMsg(player, "no-permission");
                    return true;
                }
                handleToggle(player, false);
            }
            case "status" -> {
                if (!sender.hasPermission("chatcontrol.status")) {
                    sendMsg(sender, "no-permission");
                    return true;
                }
                handleStatus(sender);
            }
            case "system", "sys", "pesansistem" -> {
                if (!(sender instanceof Player player)) {
                    sendMsg(sender, "player-only");
                    return true;
                }
                String mode = (args.length > 1) ? args[1].toLowerCase() : "toggle";
                handleSystem(player, mode);
            }
            case "muteall", "servermute", "globalmute" -> {
                if (!sender.hasPermission("chatcontrol.admin.muteall") && !sender.hasPermission("chatcontrol.admin")) {
                    sendMsg(sender, "no-permission");
                    return true;
                }
                String mode = (args.length > 1) ? args[1].toLowerCase() : "toggle";
                handleMuteAll(sender, mode);
            }
            case "whitelist", "wl" -> {
                if (!sender.hasPermission("chatcontrol.admin.whitelist") && !sender.hasPermission("chatcontrol.admin")) {
                    sendMsg(sender, "no-permission");
                    return true;
                }
                handleWhitelist(sender, args);
            }
            case "clear", "clearchat" -> {
                if (!sender.hasPermission("chatcontrol.admin.clear") && !sender.hasPermission("chatcontrol.admin")) {
                    sendMsg(sender, "no-permission");
                    return true;
                }
                handleClearChat(sender);
            }
            case "reload", "rl" -> {
                if (!sender.hasPermission("chatcontrol.admin.reload") && !sender.hasPermission("chatcontrol.admin")) {
                    sendMsg(sender, "no-permission");
                    return true;
                }
                handleReload(sender);
            }
            case "help" -> sendHelp(sender);
            default -> {
                sendHelp(sender);
            }
        }

        return true;
    }

    private void handleToggle(@NotNull Player player, @Nullable Boolean explicitEnable) {
        boolean nowEnabled;
        if (explicitEnable != null) {
            plugin.getDataManager().setChatDisabled(player.getUniqueId(), !explicitEnable);
            nowEnabled = explicitEnable;
        } else {
            nowEnabled = plugin.getDataManager().toggleChat(player.getUniqueId());
        }

        if (nowEnabled) {
            sendMsg(player, "chat-toggled-on");
        } else {
            sendMsg(player, "chat-toggled-off");
        }
    }

    private void handleSystem(@NotNull Player player, @NotNull String mode) {
        boolean nowEnabled;
        if (mode.equalsIgnoreCase("on") || mode.equalsIgnoreCase("enable")) {
            plugin.getDataManager().setSystemDisabled(player.getUniqueId(), false);
            nowEnabled = true;
        } else if (mode.equalsIgnoreCase("off") || mode.equalsIgnoreCase("disable")) {
            plugin.getDataManager().setSystemDisabled(player.getUniqueId(), true);
            nowEnabled = false;
        } else {
            nowEnabled = plugin.getDataManager().toggleSystem(player.getUniqueId());
        }

        // Sync with ChatGames if installed (via reflection)
        try {
            org.bukkit.plugin.Plugin cg = Bukkit.getPluginManager().getPlugin("ChatGames");
            if (cg != null && cg.isEnabled()) {
                Object gm = cg.getClass().getMethod("getGameManager").invoke(cg);
                gm.getClass().getMethod("toggleGames", Player.class, boolean.class).invoke(gm, player, nowEnabled);
            }
        } catch (Throwable ignored) {}

        if (nowEnabled) {
            sendMsg(player, "system-enabled");
        } else {
            sendMsg(player, "system-disabled");
        }
    }

    private void handleStatus(@NotNull CommandSender sender) {
        MessageUtil.sendRawMessage(sender, plugin.getConfigManager().getMessage("status-header"));

        if (sender instanceof Player player) {
            boolean disabled = plugin.getDataManager().isChatDisabled(player.getUniqueId());
            if (disabled) {
                MessageUtil.sendRawMessage(sender, plugin.getConfigManager().getMessage("status-personal-off"));
            } else {
                MessageUtil.sendRawMessage(sender, plugin.getConfigManager().getMessage("status-personal-on"));
            }
        }

        boolean serverMuted = plugin.getDataManager().isServerMuted();
        if (serverMuted) {
            MessageUtil.sendRawMessage(sender, plugin.getConfigManager().getMessage("status-server-muted"));
        } else {
            MessageUtil.sendRawMessage(sender, plugin.getConfigManager().getMessage("status-server-unmuted"));
        }

        if (sender instanceof Player player) {
            boolean isWl = plugin.getDataManager().isWhitelisted(player.getUniqueId())
                    || player.hasPermission("chatcontrol.bypass.muteall")
                    || player.hasPermission("chatcontrol.admin");
            if (isWl) {
                MessageUtil.sendRawMessage(sender, plugin.getConfigManager().getMessage("status-whitelist-yes"));
            } else {
                MessageUtil.sendRawMessage(sender, plugin.getConfigManager().getMessage("status-whitelist-no"));
            }
        }
    }

    private void handleMuteAll(@NotNull CommandSender sender, @NotNull String mode) {
        boolean currentState = plugin.getDataManager().isServerMuted();
        boolean newState;

        if (mode.equalsIgnoreCase("on") || mode.equalsIgnoreCase("true") || mode.equalsIgnoreCase("mute")) {
            newState = true;
        } else if (mode.equalsIgnoreCase("off") || mode.equalsIgnoreCase("false") || mode.equalsIgnoreCase("unmute")) {
            newState = false;
        } else {
            newState = !currentState;
        }

        plugin.getDataManager().setServerMuted(newState);

        String adminName = sender.getName();
        if (newState) {
            String broadcast = plugin.getConfigManager().getMessage("server-muted-broadcast", "admin", adminName);
            Bukkit.broadcast(MessageUtil.parse(broadcast));
        } else {
            String broadcast = plugin.getConfigManager().getMessage("server-unmuted-broadcast", "admin", adminName);
            Bukkit.broadcast(MessageUtil.parse(broadcast));
        }
    }

    private void handleWhitelist(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 2) {
            MessageUtil.sendMessage(sender, plugin.getConfigManager().getPrefix(), "&ePenggunaan: &f/chat whitelist <add|remove|list|clear> [player]");
            return;
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "add" -> {
                if (args.length < 3) {
                    MessageUtil.sendMessage(sender, plugin.getConfigManager().getPrefix(), "&cMohon masukkan nama player: &f/chat whitelist add <player>");
                    return;
                }
                String targetName = args[2];
                Player online = Bukkit.getPlayer(targetName);
                UUID targetUuid;
                String exactName;

                if (online != null) {
                    targetUuid = online.getUniqueId();
                    exactName = online.getName();
                } else {
                    @SuppressWarnings("deprecation")
                    OfflinePlayer offline = Bukkit.getOfflinePlayer(targetName);
                    targetUuid = offline.getUniqueId();
                    exactName = offline.getName() != null ? offline.getName() : targetName;
                }

                if (plugin.getDataManager().isWhitelisted(targetUuid)) {
                    String msg = plugin.getConfigManager().getMessage("whitelist-already-added", "player", exactName);
                    MessageUtil.sendMessage(sender, plugin.getConfigManager().getPrefix(), msg);
                    return;
                }

                plugin.getDataManager().addWhitelist(targetUuid, exactName);
                String msg = plugin.getConfigManager().getMessage("whitelist-added", "player", exactName);
                MessageUtil.sendMessage(sender, plugin.getConfigManager().getPrefix(), msg);
            }
            case "remove", "rem", "delete" -> {
                if (args.length < 3) {
                    MessageUtil.sendMessage(sender, plugin.getConfigManager().getPrefix(), "&cMohon masukkan nama player: &f/chat whitelist remove <player>");
                    return;
                }
                String targetName = args[2];
                UUID removedUuid = plugin.getDataManager().removeWhitelistByName(targetName);

                if (removedUuid == null) {
                    // Try by exact name from offline player
                    @SuppressWarnings("deprecation")
                    OfflinePlayer offline = Bukkit.getOfflinePlayer(targetName);
                    if (plugin.getDataManager().removeWhitelist(offline.getUniqueId())) {
                        removedUuid = offline.getUniqueId();
                    }
                }

                if (removedUuid != null) {
                    String msg = plugin.getConfigManager().getMessage("whitelist-removed", "player", targetName);
                    MessageUtil.sendMessage(sender, plugin.getConfigManager().getPrefix(), msg);
                } else {
                    String msg = plugin.getConfigManager().getMessage("whitelist-not-found", "player", targetName);
                    MessageUtil.sendMessage(sender, plugin.getConfigManager().getPrefix(), msg);
                }
            }
            case "list" -> {
                Map<UUID, String> list = plugin.getDataManager().getWhitelist();
                MessageUtil.sendRawMessage(sender, plugin.getConfigManager().getMessage("whitelist-list-header"));
                if (list.isEmpty()) {
                    MessageUtil.sendRawMessage(sender, plugin.getConfigManager().getMessage("whitelist-list-empty"));
                } else {
                    for (Map.Entry<UUID, String> entry : list.entrySet()) {
                        String entryMsg = plugin.getConfigManager().getMessage("whitelist-list-entry")
                                .replace("%player%", entry.getValue())
                                .replace("%uuid%", entry.getKey().toString().substring(0, 8) + "...");
                        MessageUtil.sendRawMessage(sender, entryMsg);
                    }
                }
            }
            case "clear" -> {
                plugin.getDataManager().clearWhitelist();
                sendMsg(sender, "whitelist-cleared");
            }
            default -> MessageUtil.sendMessage(sender, plugin.getConfigManager().getPrefix(), "&ePenggunaan: &f/chat whitelist <add|remove|list|clear> [player]");
        }
    }

    private void handleClearChat(@NotNull CommandSender sender) {
        int lines = plugin.getConfigManager().getClearChatLines();
        Component blank = Component.text(" ");

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("chatcontrol.bypass.clear")) {
                continue; // Staff bypasses screen clear
            }
            for (int i = 0; i < lines; i++) {
                online.sendMessage(blank);
            }
        }

        String broadcast = plugin.getConfigManager().getMessage("chat-cleared-broadcast", "admin", sender.getName());
        Bukkit.broadcast(MessageUtil.parse(broadcast));
    }

    private void handleReload(@NotNull CommandSender sender) {
        plugin.getConfigManager().loadConfig();
        plugin.getDataManager().loadData();
        sendMsg(sender, "reload-success");
    }

    private void sendHelp(@NotNull CommandSender sender) {
        MessageUtil.sendRawMessage(sender, plugin.getConfigManager().getMessage("help-header"));
        MessageUtil.sendRawMessage(sender, plugin.getConfigManager().getMessage("help-toggle"));
        MessageUtil.sendRawMessage(sender, plugin.getConfigManager().getMessage("help-system"));
        MessageUtil.sendRawMessage(sender, plugin.getConfigManager().getMessage("help-status"));

        boolean isAdmin = sender.hasPermission("chatcontrol.admin");

        if (isAdmin || sender.hasPermission("chatcontrol.admin.muteall")) {
            MessageUtil.sendRawMessage(sender, plugin.getConfigManager().getMessage("help-muteall"));
        }
        if (isAdmin || sender.hasPermission("chatcontrol.admin.whitelist")) {
            MessageUtil.sendRawMessage(sender, plugin.getConfigManager().getMessage("help-whitelist-add"));
            MessageUtil.sendRawMessage(sender, plugin.getConfigManager().getMessage("help-whitelist-remove"));
            MessageUtil.sendRawMessage(sender, plugin.getConfigManager().getMessage("help-whitelist-list"));
            MessageUtil.sendRawMessage(sender, plugin.getConfigManager().getMessage("help-whitelist-clear"));
        }
        if (isAdmin || sender.hasPermission("chatcontrol.admin.clear")) {
            MessageUtil.sendRawMessage(sender, plugin.getConfigManager().getMessage("help-clear"));
        }
        if (isAdmin || sender.hasPermission("chatcontrol.admin.reload")) {
            MessageUtil.sendRawMessage(sender, plugin.getConfigManager().getMessage("help-reload"));
        }
    }

    private void sendMsg(@NotNull CommandSender sender, @NotNull String messageKey) {
        String msg = plugin.getConfigManager().getMessage(messageKey);
        MessageUtil.sendMessage(sender, plugin.getConfigManager().getPrefix(), msg);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> options = new ArrayList<>(List.of("toggle", "on", "off", "system", "status", "help"));
            if (sender.hasPermission("chatcontrol.admin") || sender.hasPermission("chatcontrol.admin.muteall")) {
                options.add("muteall");
            }
            if (sender.hasPermission("chatcontrol.admin") || sender.hasPermission("chatcontrol.admin.whitelist")) {
                options.add("whitelist");
            }
            if (sender.hasPermission("chatcontrol.admin") || sender.hasPermission("chatcontrol.admin.clear")) {
                options.add("clear");
            }
            if (sender.hasPermission("chatcontrol.admin") || sender.hasPermission("chatcontrol.admin.reload")) {
                options.add("reload");
            }

            for (String opt : options) {
                if (opt.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(opt);
                }
            }
            return completions;
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("system") || args[0].equalsIgnoreCase("sys")) {
                for (String opt : List.of("on", "off", "toggle")) {
                    if (opt.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(opt);
                    }
                }
                return completions;
            }

            if (args[0].equalsIgnoreCase("muteall")) {
                for (String opt : List.of("on", "off", "toggle")) {
                    if (opt.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(opt);
                    }
                }
                return completions;
            }

            if (args[0].equalsIgnoreCase("whitelist") || args[0].equalsIgnoreCase("wl")) {
                for (String opt : List.of("add", "remove", "list", "clear")) {
                    if (opt.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(opt);
                    }
                }
                return completions;
            }
        }

        if (args.length == 3 && (args[0].equalsIgnoreCase("whitelist") || args[0].equalsIgnoreCase("wl"))) {
            if (args[1].equalsIgnoreCase("add")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                        completions.add(player.getName());
                    }
                }
                return completions;
            }

            if (args[1].equalsIgnoreCase("remove")) {
                for (String name : plugin.getDataManager().getWhitelist().values()) {
                    if (name.toLowerCase().startsWith(args[2].toLowerCase())) {
                        completions.add(name);
                    }
                }
                return completions;
            }
        }

        return completions;
    }
}
