package com.leftycraft.chatcontrol.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private MessageUtil() {
    }

    /**
     * Parse a message into an Adventure Component supporting both MiniMessage and Legacy color codes.
     *
     * @param message Text string
     * @return Component
     */
    @NotNull
    public static Component parse(@NotNull String message) {
        if (message.isEmpty()) {
            return Component.empty();
        }

        // Convert legacy hex &#RRGGBB to MiniMessage <#RRGGBB> or legacy format
        String formatted = translateHexColorCodes(message);

        // If message contains MiniMessage tags like <color> or <gradient>
        if (formatted.contains("<") && formatted.contains(">")) {
            try {
                // First replace legacy color codes in the string to allow mixed formatting
                Component legacyPart = LEGACY_SERIALIZER.deserialize(formatted);
                String serialized = LegacyComponentSerializer.legacySection().serialize(legacyPart);
                // Also parse MiniMessage tags
                return MINI_MESSAGE.deserialize(formatted);
            } catch (Exception ignored) {
                // Fallback to legacy serializer
            }
        }

        return LEGACY_SERIALIZER.deserialize(formatted);
    }

    /**
     * Translates &#RRGGBB format.
     */
    @NotNull
    public static String translateHexColorCodes(@NotNull String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder buffer = new StringBuilder(message.length() + 32);
        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(buffer, "<#" + hex + ">");
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * Send formatted message with prefix.
     */
    public static void sendMessage(@NotNull CommandSender sender, @NotNull String prefix, @NotNull String message) {
        if (message.isEmpty()) {
            return;
        }
        String fullMessage = (prefix.isEmpty() ? "" : prefix) + message;
        sender.sendMessage(parse(fullMessage));
    }

    /**
     * Send raw formatted message without prefix.
     */
    public static void sendRawMessage(@NotNull CommandSender sender, @NotNull String message) {
        if (message.isEmpty()) {
            return;
        }
        sender.sendMessage(parse(message));
    }
}
