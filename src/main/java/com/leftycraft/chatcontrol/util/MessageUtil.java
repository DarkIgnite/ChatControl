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
    private static final Pattern LEGACY_COLOR_PATTERN = Pattern.compile("(?i)[&§]([0-9a-fk-or])");
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private MessageUtil() {
    }

    /**
     * Parse a message into an Adventure Component supporting both MiniMessage tags
     * (<gradient>, <color>, etc.) and Legacy color codes (&a, &c, &l, &#rrggbb, etc.)
     * even when mixed together in the same string.
     *
     * @param message Text string
     * @return Component
     */
    @NotNull
    public static Component parse(@NotNull String message) {
        if (message.isEmpty()) {
            return Component.empty();
        }

        try {
            // Convert legacy color codes (&c, &#ffffff) to MiniMessage tags (<red>, <#ffffff>)
            String converted = convertLegacyToMiniMessage(message);
            return MINI_MESSAGE.deserialize(converted);
        } catch (Exception e) {
            // Fallback to legacy serializer if MiniMessage fails
            try {
                return LEGACY_SERIALIZER.deserialize(message);
            } catch (Exception ex) {
                return Component.text(message);
            }
        }
    }

    /**
     * Converts all legacy & and § color codes and hex &#rrggbb codes into valid MiniMessage tags.
     */
    @NotNull
    public static String convertLegacyToMiniMessage(@NotNull String input) {
        if (input.isEmpty()) {
            return "";
        }

        // 1. Convert hex format &#RRGGBB to <#RRGGBB>
        Matcher hexMatcher = HEX_PATTERN.matcher(input);
        StringBuilder hexBuffer = new StringBuilder(input.length() + 32);
        while (hexMatcher.find()) {
            hexMatcher.appendReplacement(hexBuffer, "<#" + hexMatcher.group(1).toLowerCase() + ">");
        }
        hexMatcher.appendTail(hexBuffer);
        String step1 = hexBuffer.toString();

        // 2. Convert standard legacy & and § color codes to MiniMessage tags
        Matcher legacyMatcher = LEGACY_COLOR_PATTERN.matcher(step1);
        StringBuilder legacyBuffer = new StringBuilder(step1.length() + 32);
        while (legacyMatcher.find()) {
            char code = Character.toLowerCase(legacyMatcher.group(1).charAt(0));
            String tag = switch (code) {
                case '0' -> "<black>";
                case '1' -> "<dark_blue>";
                case '2' -> "<dark_green>";
                case '3' -> "<dark_aqua>";
                case '4' -> "<dark_red>";
                case '5' -> "<dark_purple>";
                case '6' -> "<gold>";
                case '7' -> "<gray>";
                case '8' -> "<dark_gray>";
                case '9' -> "<blue>";
                case 'a' -> "<green>";
                case 'b' -> "<aqua>";
                case 'c' -> "<red>";
                case 'd' -> "<light_purple>";
                case 'e' -> "<yellow>";
                case 'f' -> "<white>";
                case 'k' -> "<obfuscated>";
                case 'l' -> "<bold>";
                case 'm' -> "<strikethrough>";
                case 'n' -> "<underlined>";
                case 'o' -> "<italic>";
                case 'r' -> "<reset>";
                default -> legacyMatcher.group(0);
            };
            legacyMatcher.appendReplacement(legacyBuffer, Matcher.quoteReplacement(tag));
        }
        legacyMatcher.appendTail(legacyBuffer);

        return legacyBuffer.toString();
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