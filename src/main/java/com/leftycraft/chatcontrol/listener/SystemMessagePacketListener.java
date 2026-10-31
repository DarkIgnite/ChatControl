package com.leftycraft.chatcontrol.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.leftycraft.chatcontrol.ChatControlPlugin;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.regex.Pattern;

public class SystemMessagePacketListener extends PacketAdapter {

    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)&[0-9A-FK-ORX]|§[0-9A-FK-ORX]");
    private static final Pattern STRIP_MINIMESSAGE_PATTERN = Pattern.compile("<[^>]+>");

    private final ChatControlPlugin plugin;

    public SystemMessagePacketListener(@NotNull ChatControlPlugin plugin) {
        super(plugin, ListenerPriority.NORMAL,
                PacketType.Play.Server.SYSTEM_CHAT,
                PacketType.Play.Server.DISGUISED_CHAT,
                PacketType.Play.Server.SET_TITLE_TEXT,
                PacketType.Play.Server.SET_SUBTITLE_TEXT,
                PacketType.Play.Server.TITLE
        );
        this.plugin = plugin;
    }

    public static void register(@NotNull ChatControlPlugin plugin) {
        try {
            ProtocolLibrary.getProtocolManager().addPacketListener(new SystemMessagePacketListener(plugin));
            plugin.getLogger().info("ProtocolLib hook registered for System Messages packet filtering.");
        } catch (Throwable t) {
            plugin.getLogger().warning("Could not register ProtocolLib packet listener: " + t.getMessage());
        }
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        // Only filter for players who have hidden system messages
        if (!plugin.getDataManager().isSystemDisabled(player.getUniqueId())) {
            return;
        }

        PacketType type = event.getPacketType();
        PacketContainer packet = event.getPacket();

        if (type == PacketType.Play.Server.SYSTEM_CHAT || type == PacketType.Play.Server.DISGUISED_CHAT) {
            String extracted = extractTextFromPacket(packet);
            if (isSystemMessage(extracted)) {
                event.setCancelled(true);
            }
        } else if (type == PacketType.Play.Server.SET_TITLE_TEXT ||
                   type == PacketType.Play.Server.SET_SUBTITLE_TEXT ||
                   type == PacketType.Play.Server.TITLE) {
            String extracted = extractTextFromPacket(packet);
            if (isChatGamesTitle(extracted)) {
                event.setCancelled(true);
            }
        }
    }

    private String extractTextFromPacket(PacketContainer packet) {
        StringBuilder sb = new StringBuilder();

        try {
            if (packet.getChatComponents().size() > 0) {
                WrappedChatComponent component = packet.getChatComponents().readSafely(0);
                if (component != null) {
                    sb.append(component.getJson()).append(" ");
                }
            }
        } catch (Throwable ignored) {}

        try {
            if (packet.getStrings().size() > 0) {
                for (String s : packet.getStrings().getValues()) {
                    if (s != null) {
                        sb.append(s).append(" ");
                    }
                }
            }
        } catch (Throwable ignored) {}

        try {
            for (Object obj : packet.getModifier().getValues()) {
                if (obj != null) {
                    sb.append(obj.toString()).append(" ");
                }
            }
        } catch (Throwable ignored) {}

        return sb.toString();
    }

    public static String cleanText(String raw) {
        if (raw == null) return "";
        String s = STRIP_COLOR_PATTERN.matcher(raw).replaceAll("");
        s = STRIP_MINIMESSAGE_PATTERN.matcher(s).replaceAll("");
        // Replace unicode small caps or weird spaces
        return s.toLowerCase(Locale.ROOT).trim();
    }

    /**
     * Checks if the message content matches AutoMessage, Clan War results,
     * Skript /dungeon broadcast, or ChatGames announcement.
     */
    public static boolean isSystemMessage(String raw) {
        if (raw == null || raw.isEmpty()) return false;
        String text = cleanText(raw);

        // 1. Skript /dungeon broadcast
        // "ʟᴇғᴛʏ » <player> ᴍᴇᴍᴀsᴜᴋɪ ᴅᴜɴɢᴇᴏɴ! (/dg)"
        if (text.contains("memasuki dungeon") || text.contains("ᴍᴇᴍᴀsᴜᴋɪ ᴅᴜɴɢᴇᴏɴ") ||
            ((text.contains("ʟᴇғᴛʏ") || text.contains("lefty")) && text.contains("dungeon"))) {
            return true;
        }

        // 2. Clan War results / announcements
        // "CLAN WAR", "topScoreClaimed", "topDamage", "Warpoint"
        if (text.contains("clan war") || text.contains("warpoint") ||
            text.contains("topscoreclaimed") || text.contains("topdamage")) {
            return true;
        }

        // 3. AutoMessage broadcasts
        // "SERVER »", "TUTORIAL »", "VOTING »", "PEKERJAAN »", "TOKO KERJA »", etc.
        if (text.contains("»") || text.contains("»") || text.contains(">")) {
            if (text.contains("server") && (text.contains("»") || text.contains(">")) && text.contains("/menu")) return true;
            if (text.contains("tutorial") && (text.contains("»") || text.contains(">"))) return true;
            if (text.contains("voting") && (text.contains("»") || text.contains(">"))) return true;
            if (text.contains("pekerjaan") && (text.contains("»") || text.contains(">"))) return true;
            if (text.contains("toko kerja") && (text.contains("»") || text.contains(">"))) return true;
            if (text.contains("keahlian") && (text.contains("»") || text.contains(">"))) return true;
            if (text.contains("quest") && (text.contains("»") || text.contains(">"))) return true;
            if (text.contains("discord") && (text.contains("»") || text.contains(">"))) return true;
            if (text.contains("enchant") && (text.contains("»") || text.contains(">"))) return true;
            if (text.contains("hadiah") && (text.contains("»") || text.contains(">"))) return true;
            if (text.contains("ekonomi") && (text.contains("»") || text.contains(">"))) return true;
            if (text.contains("proteksi") && (text.contains("»") || text.contains(">"))) return true;
            if (text.contains("clan") && (text.contains("»") || text.contains(">")) && text.contains("/clan")) return true;
            if (text.contains("pwarps") && (text.contains("»") || text.contains(">"))) return true;
            if (text.contains("toko peti") && (text.contains("»") || text.contains(">"))) return true;
            if (text.contains("afk zone") && (text.contains("»") || text.contains(">"))) return true;
            if (text.contains("brankas") && (text.contains("»") || text.contains(">"))) return true;
            if (text.contains("eksplorasi") && (text.contains("»") || text.contains(">"))) return true;
            if (text.contains("fokus chat") && (text.contains("»") || text.contains(">"))) return true;
            if (text.contains("skin") && (text.contains("»") || text.contains(">"))) return true;
            if (text.contains("voice chat") && (text.contains("»") || text.contains(">"))) return true;
            if (text.contains("peringkat") && (text.contains("»") || text.contains(">"))) return true;
            if (text.contains("pamer item") && (text.contains("»") || text.contains(">"))) return true;
            if (text.contains("emote") && (text.contains("»") || text.contains(">"))) return true;
            if (text.contains("peraturan") && (text.contains("»") || text.contains(">"))) return true;
            if (text.contains("donasi") && (text.contains("»") || text.contains(">"))) return true;
            if (text.contains("bantuan") && (text.contains("»") || text.contains(">"))) return true;
        }

        // 4. ChatGames announcements / endings
        // "teka teki chat", "top chatgames", "unscramble", "unreverse", "reaction", "fillout", "math quiz"
        if (text.contains("teka teki chat") || text.contains("tekatekichat") ||
            text.contains("top chatgames") || text.contains("topchatgames") ||
            text.contains("chatgames") ||
            text.contains("ᴜɴsᴄʀᴀᴍʙʟᴇ") || text.contains("unscramble") ||
            text.contains("ᴜɴʀᴇᴠᴇʀsᴇ") || text.contains("unreverse") ||
            text.contains("ʀᴇᴀᴄᴛɪᴏɴ") || text.contains("reaction") ||
            text.contains("ꜰɪʟʟᴏᴜᴛ") || text.contains("fillout") ||
            text.contains("ᴍᴀᴛʜ") || text.contains("math quiz") ||
            text.contains("susun kembali huruf acak") ||
            text.contains("berhasil menyusun") ||
            text.contains("waktu tebak telah berakhir") ||
            text.contains("hadiah telah diberikan")) {
            return true;
        }

        return false;
    }

    public static boolean isChatGamesTitle(String raw) {
        if (raw == null || raw.isEmpty()) return false;
        String text = cleanText(raw);

        return text.contains("unscramble") || text.contains("ᴜɴsᴄʀᴀᴍʙʟᴇ") ||
               text.contains("unreverse") || text.contains("ᴜɴʀᴇᴠᴇʀsᴇ") ||
               text.contains("reaction") || text.contains("ʀᴇᴀᴄᴛɪᴏɴ") ||
               text.contains("fillout") || text.contains("ꜰɪʟʟᴏᴜᴛ") ||
               text.contains("math") || text.contains("ᴍᴀᴛʜ") ||
               text.contains("trivia") || text.contains("ᴛʀɪᴠɪᴀ") ||
               text.contains("teka teki") || text.contains("tekateki");
    }
}
