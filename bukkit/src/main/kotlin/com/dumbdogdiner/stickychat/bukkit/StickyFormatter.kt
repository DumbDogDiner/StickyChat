package com.dumbdogdiner.stickychat.bukkit

import com.dumbdogdiner.stickychat.api.Formatter
import com.dumbdogdiner.stickychat.api.misc.SignNotification
import com.dumbdogdiner.stickychat.api.util.Placeholders
import com.dumbdogdiner.stickychat.api.util.StringModifier
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player

class StickyFormatter private constructor(private val player: Player) : WithPlugin, Formatter {
    companion object {
        private val formatters = HashMap<Player, StickyFormatter>()

        /**
         * Get the formatter for the target player.
         */
        fun get(player: Player): StickyFormatter {
            if (formatters.containsKey(player)) {
                return formatters[player]!!
            }
            val formatter = StickyFormatter(player)
            formatters[player] = formatter
            return formatter
        }

        private val linkRegex = Regex("https?://[-a-zA-Z0-9@:%._+~#=]{1,256}.(?:[-a-zA-Z0-9@:%._+~#=/?=%&]{3,})+")
    }

    override fun getPlayer(): Player {
        return this.player
    }

    /**
     * Format a chat message for this player. Fetches the chat format
     * from the cached configuration and interpolates placeholders as required.
     */
    override fun formatMessage(message: String): BaseComponent {
        val interp = StringModifier(this.config.getString("chat.format", "%player_name%: %message%"))
                .replace("%player_name%", this.player.name)
                .replace("%message%", message)
                .apply { Placeholders.setPlaceholdersSafe(this.player, it) }
                .get()

        val result = Formatter.formatHexCodes(interp)
        Formatter.linkLinks(result)
        return result
    }

    /**
     * Format a staff chat message for this player. Fetches the SC format
     * from the cached configuration and interpolates placeholders as required.
     */
    override fun formatStaffChatMessage(message: String): TextComponent {
        val interp = StringModifier(this.config.getString("chat.staff-chat-format", "[SC] %player_name%: %message%"))
                .replace("%player_name%", this.player.name)
                .replace("%message%", message)
                .apply { Placeholders.setPlaceholdersSafe(this.player, it) }
                .get()

        val result = Formatter.formatHexCodes(interp)
        Formatter.linkLinks(result)
        return result
    }

    override fun formatOutgoingDM(to: Player, message: String): TextComponent {
        val interp = StringModifier(this.config.getString("dms.outgoing.format", "&8[&e&lPM&r&8] &a%from_name% &8» &r%message%")!!)
                .replace("%from_name%", this.player.name)
                .replace("%to_name%", to.name)
                .replace("%message%", message)
                .get()

        val result = Formatter.formatHexCodes(interp)
        Formatter.linkLinks(result)
        return result
    }

    override fun formatIncomingDM(from: Player, message: String): TextComponent {
        val interp = StringModifier(this.config.getString("dms.incoming.format", "&8[&e&lPM&r&8] &a%from_name% &8» &r%message%")!!)
                .replace("%from_name%", from.name)
                .replace("%to_name%", this.player.name)
                .replace("%message%", message)
                .get()

        val result = Formatter.formatHexCodes(interp)
        Formatter.linkLinks(result)
        return result
    }

    override fun formatSignSpyNotification(notification: SignNotification): BaseComponent {
        return TextComponent()
    }
}
