package dev.slne.surf.vanish.paper.listener

import dev.slne.surf.vanish.paper.util.vanishPlayer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAdvancementDoneEvent

object AdvancementListener : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onAdvancementDone(event: PlayerAdvancementDoneEvent) {
        val vanishPlayer = event.player.vanishPlayer

        if (vanishPlayer.isVanished()) {
            event.message(null)
        }
    }
}