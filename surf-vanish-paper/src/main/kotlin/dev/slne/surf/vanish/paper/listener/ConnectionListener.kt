package dev.slne.surf.vanish.paper.listener

import dev.slne.surf.surfapi.bukkit.api.glow.glowingApi
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.tab.api.redis.TabHideRedisEvent
import dev.slne.surf.vanish.core.service.vanishPlayerService
import dev.slne.surf.vanish.core.service.vanishService
import dev.slne.surf.vanish.paper.config.VanishConfiguration
import dev.slne.surf.vanish.paper.plugin
import dev.slne.surf.vanish.paper.redisApi
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

            Bukkit.getOnlinePlayers()
                .filterNot { it.hasPermission(VanishPermissionRegistry.VANISH_BYPASS) }.forEach {
                    it.hidePlayer(plugin, event.player)

                    redisApi.publishEvent(
                        TabHideRedisEvent(
                            it.uniqueId,
                            event.player.uniqueId
                        )
                    )
                }


            event.player.sendText {
                appendPrefix()
                info("Du bist für andere Spieler unsichtbar.")
            }
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