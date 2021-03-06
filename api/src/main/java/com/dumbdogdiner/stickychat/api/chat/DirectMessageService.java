package com.dumbdogdiner.stickychat.api.chat;

import com.dumbdogdiner.stickychat.api.Priority;
import com.dumbdogdiner.stickychat.api.util.WithPlayer;
import com.dumbdogdiner.stickychat.api.result.DirectMessageResult;
import com.dumbdogdiner.stickychat.api.result.MessageResult;
import com.dumbdogdiner.stickychat.api.StickyChat;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Analogous to {@link MessageService}, manages the sending of
 * direct messages to other players for a specific player.
 * Classes implementing this interface should instantiate new
 * instances for each player who requests a direct message.
 * These should be cached, and returned when access is requested.
 */
public interface DirectMessageService extends WithPlayer {
    /**
     * Send a DM to the target player. Calling this method should invoke
     * {@link DirectMessageService#receive(Player, String)} of the DM
     * service of the target player.
     *
     * @param target The player receiving the message
     * @param message The message to send
     * @return {@link MessageResult}
     */
    @NotNull
    DirectMessageResult sendTo(@NotNull Player target, @NotNull String message);

    /**
     * Gets the last player this player sent a message to.
     * @return {@link Player}
     */
    @Nullable
    Player getLast();

    /**
     * Get the direct message service of the player this player last sent a message to.
     * Returns null if this player could not be found.
     * @return {@link DirectMessageService}
     */
    @Nullable
    default DirectMessageService getLastDirectMessageService() {
        if (this.getLast() == null) {
            return null;
        }
        return StickyChat.getService().getDirectMessageService(this.getLast());
    }

    /**
     * Set the last player this player sent a message to.
     */
    void setLastPlayer(@NotNull Player player);

    /**
     * Send a DM to the last player who this player sent a message to, or received a
     * message from. Returns true if the message was sent successfully.
     *
     * @param message The message to send
     * @return {@link Boolean}
     */
    @NotNull
    DirectMessageResult sendToLast(@NotNull String message);

    /**
     * Handle a received DM from the specified player. Implementations of this method
     * should check the priority of the player, as well as whether the sender is blocked.
     * Returns a direct message result.
     *
     * @param from The player who sent the message
     * @param message The message being sent
     * @return {@link DirectMessageResult}
     */
    @NotNull
    DirectMessageResult receive(@NotNull Player from, @NotNull String message);

    /**
     * Block the target player. Returns false if the player is already blocked.
     *
     * @param target The player to block
     * @return {@link Boolean}
     */
    @NotNull
    Boolean block(@NotNull Player target);

    /**
     * Unblock the target player. Returns false if the player is not blocked.
     *
     * @param target The player to unblock
     * @return {@link Boolean}
     */
    @NotNull
    Boolean unblock(@NotNull Player target);

    /**
     * Send this player a system message.
     *
     * @param message The message to send
     * @return {@link DirectMessageResult}
     */
    default DirectMessageResult sendSystemMessage(String message) {
        return this.sendSystemMessage(new TextComponent(message));
    }

    /**
     * Send this player a system
     *
     * @param message The message to send
     * @return {@link DirectMessageResult}
     */
    @NotNull
    DirectMessageResult sendSystemMessage(@NotNull BaseComponent message);

    /**
     * Check if the target player is blocked. Returns true if they are.
     *
     * @param target The player to test
     * @return {@link Boolean}
     */
    @NotNull
    default Boolean isBlocked(@NotNull Player target) {
        return this.getDataService().getBlocked(target);
    }

    /**
     * Return a list of players this player can message.
     *
     * @return {@link List}
     */
    default List<Player> getMessageablePlayers() {
        var players = new ArrayList<Player>();
        for (var data : StickyChat.getService().getDataServices()) {
            if (data.getPlayer().getUniqueId().equals(this.getPlayer().getUniqueId())) {
                continue;
            }
            if (data.getBlocked(this.getPlayer())) {
                continue;
            }
            players.add(data.getPlayer());
        }
        return players;
    }

    /**
     * Test if this player can message the target player.
     *
     * @param target The player to test
     * @return {@link Boolean}
     */
    default Boolean canMessagePlayer(Player target) {
        var data = StickyChat.getService().getDataService(target);
        if (data.getPriority().isGreaterThan(Priority.DIRECT)) {
            return false;
        }
        return !data.getBlocked(target);
    }
}
