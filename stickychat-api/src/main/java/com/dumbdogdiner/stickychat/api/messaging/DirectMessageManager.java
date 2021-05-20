package com.dumbdogdiner.stickychat.api.messaging;



import java.util.Collection;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Manages direct messages. */
public interface DirectMessageManager {
    /**
     * Send a message to the player with the target name. They many not necessarily
     * be on the same server.
     *
     * @param from The player this message is from
     * @param to The name of the target plyaer
     * @param message The message content
     * @return A {@link DirectMessageResult} determining the success of the action.
     */
    @NotNull
    DirectMessageResult sendMessage(@NotNull Player from, @NotNull String to, @NotNull String message);

    /**
     * Send a message to the player with the target name. The two players are not on
     * the same server.
     *
     * @param from The player this message is from
     * @param to The name of the target player
     * @param message The message content
     * @return A {@link DirectMessageResult} determining the success of the action.
     */
    @NotNull
    DirectMessageResult sendExternalMessage(@NotNull Player from, @NotNull String to, @NotNull String message);

    /**
     * Send a message to the player with the target name. They are on the same
     * server.
     *
     * @param from The player who is sending a message
     * @param to The player receiving the message
     * @param message The message to send
     * @return {@link DirectMessageResult}
     */
    @NotNull
    DirectMessageResult sendInternalMessage(@NotNull Player from, @NotNull Player to, @NotNull String message);

    /**
     * Send the target player a raw message
     *
     * @param player The target player
     * @param message The text component message to send
     * @return A {@link DirectMessageResult} determining the outcome of this
     *         operation.
     */
    @NotNull
    DirectMessageResult sendRawMessage(@NotNull Player player, @NotNull TextComponent message);

    /**
     * Send the target player a raw message.
     *
     * @param player The target player
     * @param message The message to send
     * @return A {@link DirectMessageResult} determining the outcome of this
     *         operation.
     */
    default @NotNull DirectMessageResult sendRawMessage(@NotNull Player player, @NotNull String message) {
        return this.sendRawMessage(player, new TextComponent(message));
    }

    /**
     * Gets the last player the target player sent a message to.
     *
     * @param player The target player
     * @return A {@link String} containing the name of the player they last
     *         successfuly messaged.
     */
    @Nullable
    String getLast(@NotNull Player player);

    /**
     * Test if this player has previously sent a message to someone on the server.
     *
     * @param player The target player
     * @return True if they have previously sent a message
     */
    default @NotNull Boolean hasLastPlayer(@NotNull Player player) {
        return this.getLast(player) != null;
    }

    /**
     * Send a DM to the last player who this player sent a message to, or received a
     * message from. Returns true if the message was sent successfully.
     *
     * @param player The player sending the message
     * @param message The message to send
     * @return {@link Boolean}
     */
    @NotNull
    DirectMessageResult reply(@NotNull Player player, @NotNull String message);

    /**
     * @return A {@link Collection} of {@link Player}s who have direct messages
     *         disabled.
     */
    @NotNull
    Collection<Player> getDisabledPlayers();

    /**
     * Test if the target player has direct messages disabled.
     *
     * @param player The target player
     * @return True if the player has direct messages disabled.
     */
    @NotNull
    Boolean hasDirectMessagesDisabled(@NotNull Player player);

    /**
     * Enable direct messages for the target player.
     *
     * @param player The target player
     */
    void enableDirectMessages(@NotNull Player player);

    /**
     * Disable direct messages for the target player
     *
     * @param player The target player
     */
    void disableDirectMessages(@NotNull Player player);
}