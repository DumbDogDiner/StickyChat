package com.dumbdogdiner.stickychat.bukkit.listeners

import com.dumbdogdiner.stickychat.bukkit.WithPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Listens for player joins and caches appropriate values. Forgets them when they leave.
 */
class PlayerJoinQuitListener : WithPlugin, Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerJoin(e: PlayerJoinEvent) {
        this.plugin.getNicknameService(e.player).loadNickname()
        // disable message hotfix
        if (this.plugin.config.getBoolean("chat.disable-join-messages", false)) {
            e.joinMessage = null
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerLeave(e: PlayerQuitEvent) {
        logger.info("Saving settings for ${e.player} (${e.player.uniqueId})...")
        // disable message hotfix
        if (this.plugin.config.getBoolean("chat.disable-quit-messages", false)) {
            e.quitMessage = null
        }
    }
}
