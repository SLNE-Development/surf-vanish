package dev.slne.surf.vanish.paper.listener

import dev.slne.surf.surfapi.bukkit.api.glow.glowingApi
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.vanish.core.service.vanishPlayerService
import dev.slne.surf.vanish.core.service.vanishService
import dev.slne.surf.vanish.paper.config.VanishConfiguration
import dev.slne.surf.vanish.paper.plugin
import dev.slne.surf.vanish.paper.util.VanishPermissionRegistry
import dev.slne.surf.vanish.paper.util.bukkitPlayer
import dev.slne.surf.vanish.paper.util.vanishPlayer
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

object ConnectionListener : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onConnect(event: PlayerJoinEvent) {
        val vanishPlayer = event.player.vanishPlayer

        if (!event.player.hasPermission(VanishPermissionRegistry.VANISH_BYPASS)) {
            vanishService.allOnline().forEach {
                event.player.hidePlayer(plugin, it.bukkitPlayer)
            }
        }

        if (vanishPlayer.isVanished()) {
            event.joinMessage(null)
            vanishService.createAndShowScoreboard(vanishPlayer)

            vanishService.current(vanishPlayer)?.bukkitPlayer?.let { currentPlayer ->
                glowingApi.makeGlowing(currentPlayer, event.player, VanishConfiguration.GLOW_COLOR)
            }

            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, {
                Bukkit.getOnlinePlayers()
                    .filterNot { it.hasPermission(VanishPermissionRegistry.VANISH_BYPASS) }
                    .forEach {
                        it.hidePlayer(plugin, event.player)
                    }

                event.player.sendText {
                    appendInfoPrefix()
                    info("Du bist für andere Spieler unsichtbar.")
                }
            }, 17)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onDisconnect(event: PlayerQuitEvent) {
        val vanishPlayer = vanishPlayerService.getPlayer(event.player.uniqueId, event.player.name)

        vanishService.hideAndDeleteScoreboard(vanishPlayer)

        if (vanishPlayer.isVanished()) {
            event.quitMessage(null)
        }
    }
}