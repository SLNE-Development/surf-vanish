package dev.slne.surf.vanish.paper.listener

import dev.slne.surf.vanish.core.service.vanishService
import dev.slne.surf.vanish.paper.plugin
import dev.slne.surf.vanish.paper.util.VanishPermissionRegistry
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerGameModeChangeEvent

object GameModeChangeListener : Listener {
    @EventHandler
    fun onGameModeChange(event: PlayerGameModeChangeEvent) {
        if (event.newGameMode == GameMode.SURVIVAL || event.newGameMode == GameMode.ADVENTURE) {
            if(!event.player.hasPermission(VanishPermissionRegistry.VANISH_SAVE_FLY_STATE)) {
                return
            }

            if(vanishService.getFlyState(event.player.uniqueId)) {
                Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                    event.player.allowFlight = true
                    event.player.isFlying = true
                }, 1L)
            }
        } else {
            if(!event.player.hasPermission(VanishPermissionRegistry.VANISH_SAVE_FLY_STATE)) {
                return
            }

            vanishService.setFlyState(event.player.uniqueId,event.player.isFlying)
        }
    }
}