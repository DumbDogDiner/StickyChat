package com.dumbdogdiner.stickychat.api;

import com.dumbdogdiner.stickychat.api.misc.SignNotification;
import com.dumbdogdiner.stickychat.api.util.WithPlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * An interface for chat formatting.
 */
public interface Formatter extends WithPlayer {

    /**
     * Colorize a string using both Minecraft char codes, and hexadecimal color codes.
     * @deprecated Deprecated in favor of the nex hex code translator.
     * @param string The string to colorize
     * @return {@link String}
     */
    static String colorize(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    /**
     * Regular expression used for formatting hexadecimal color codes.
     */
    Pattern COLOR_FORMATTING_REGEX = Pattern.compile("(?<formatting>&(?:#[a-f0-9]{6}|[a-f0-9k-or]))?(?<content>.*?)(?=(&(?:#[a-f0-9]{6}|[a-f0-9k-or]))|$)", Pattern.MULTILINE);

    /**
     * Colorize a text component using Minecraft hex codes.
     * @param component The component to colorize
     * @return {@link TextComponent}
     */
    static TextComponent formatHexCodes(TextComponent component) {
        return Formatter.formatHexCodes(component.getText());
    }

    /**
     * Colorize a string using Minecraft hex codes.
     * @param message The message to colorize
     * @return {@link TextComponent}
     */
    static TextComponent formatHexCodes(String message) {
        var matcher = COLOR_FORMATTING_REGEX.matcher(message);

        var magic = false;
        var bold = false;
        var strike = false;
        var underline = false;
        var italic = false;
        ChatColor color = null;

        var rootComponent = new TextComponent();
        var nextComponent = new TextComponent();

        while (matcher.find()) {
            if (matcher.group("formatting") != null) {
                var format = matcher.group("formatting");
                if (format.charAt(1) == '#') {
                    color = ChatColor.of(format.substring(1));
                } else {
                    switch (format.charAt(1)) {
                        case 'x', 'X' -> magic = true;
                        case 'l', 'L' -> bold = true;
                        case 'm', 'M' -> strike = true;
                        case 'n', 'N' -> underline = true;
                        case 'o', 'O' -> italic = true;
                        case 'r', 'R' -> {
                            magic = false;
                            bold = false;
                            strike = false;
                            underline = false;
                            italic = false;
                            color = ChatColor.WHITE;
                        }
                        default -> color = ChatColor.getByChar(format.charAt(1));
                    }
                }
            }

            if (matcher.group("content") == null) {
                continue;
            }

            nextComponent.setText(matcher.group("content"));
            nextComponent.setObfuscated(magic);
            nextComponent.setBold(bold);
            nextComponent.setStrikethrough(strike);
            nextComponent.setUnderlined(underline);
            nextComponent.setItalic(italic);
            nextComponent.setColor(color);
            rootComponent.addExtra(nextComponent);

            // reset component
            nextComponent = new TextComponent();
            magic = false;
            bold = false;
            strike = false;
            underline = false;
            italic = false;
        }

        return rootComponent;
    }

    // Wrote this myself, based off of the HTTP RFC; should be correct enough for our use
    Pattern URI_REGEX = Pattern.compile("https?://(?:(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)*[A-Za-z](?:[A-Za-z0-9-]*[A-Za-z0-9])?|[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+)(?::[0-9]+)?(?:/(?:[A-Za-z0-9$_.+!*'(),;:@&=-]|%[0-9A-Fa-f]{2})*(?:/(?:[A-Za-z0-9$_.+!*'(),;:@&=-]|%[0-9A-Fa-f]{2})*)*(?:\\?(?:[A-Za-z0-9$_.+!*'(),;:@&=-]|%[0-9A-Fa-f]{2})*)?)?");

    /**
     * Make links clickable.
     *
     * @param message The message
     */
    static void linkLinks(TextComponent message) {
        var messageString = message.getText();
        // See if our message contains any links
        var matcher = URI_REGEX.matcher(messageString);
        var indices = new ArrayList<Integer>();
        indices.add(0);
        // find links
        var messageContainedLinks = false;
        while (matcher.find()) {
            indices.add(matcher.start());
            indices.add(matcher.end());
            messageContainedLinks = true;
        }
        indices.add(messageString.length());
        // Alternate through segments, linking the links
        var messageSegments = new ArrayList<BaseComponent>();
        var isLink = false;
        for (int i = 1; i < indices.size(); ++i) {
            var substring = messageString.substring(indices.get(i - 1), indices.get(i));
            if (!substring.isEmpty()) {
                var component = new TextComponent(substring);
                if (isLink) {
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, substring));
                }
                messageSegments.add(component);
            }
            isLink = !isLink;
        }
        // need to be extra careful not to create a component with an empty extras list
        var currentExtra = message.getExtra();
        if (currentExtra != null) {
            var newExtra = new ArrayList<BaseComponent>();
            // recursively apply
            for (var extra : currentExtra) {
                if (extra instanceof TextComponent) {
                    linkLinks((TextComponent) extra);
                }
                newExtra.add(extra);
            }
            if (messageContainedLinks) {
                messageSegments.addAll(newExtra);
                message.setText("");
                message.setExtra(messageSegments);
            } else {
                message.setExtra(newExtra);
            }
        } else {
            if (messageContainedLinks) {
                message.setText("");
                message.setExtra(messageSegments);
            }
        }
    }

    /**
     * Format a message sent to the entire server.
     *
     * @param message The message
     * @return {@link BaseComponent}
     */
    @NotNull
    BaseComponent formatMessage(@NotNull String message);

    /**
     * Format a message sent in staff chat.
     *
     * @param message The message
     * @return {@link BaseComponent}
     */
    @NotNull
    BaseComponent formatStaffChatMessage(@NotNull String message);

    /**
     * Format an incoming direct message sent between two players.
     *
     * @param to The recipient of the message
     * @param message The message
     * @return {@link BaseComponent}
     */
    @NotNull
    BaseComponent formatOutgoingDM(@NotNull Player to, @NotNull String message);

    /**
     * Format a direct message sent between two players.
     *
     * @param from The sender of the message
     * @param message The message
     * @return {@link BaseComponent}
     */
    @NotNull
    BaseComponent formatIncomingDM(@NotNull Player from, @NotNull String message);

    /**
     * Format a SignSpy notification.
     *
     * @param notification - The notification to format
     * @return {@link BaseComponent}
     */
    @NotNull
    BaseComponent formatSignSpyNotification(@NotNull SignNotification notification);
}
