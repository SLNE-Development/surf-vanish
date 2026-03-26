package dev.slne.surf.vanish.paper.listener

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.surfapi.bukkit.api.glow.glowingApi
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.vanish.core.service.vanishService
import dev.slne.surf.vanish.paper.config.VanishConfiguration
import dev.slne.surf.vanish.paper.plugin
import dev.slne.surf.vanish.paper.util.VanishPermissionRegistry
import dev.slne.surf.vanish.paper.util.getVanishPriority
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

object ConnectionListener : Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onConnect(event: PlayerJoinEvent) {
        val joiningPlayerPriority = event.player.getVanishPriority()

        if (event.player.hasPermission(VanishPermissionRegistry.VANISH_SAVE_FLY_STATE)) {
            if (vanishService.getFlyState(event.player.uniqueId)) {
                plugin.launch(plugin.entityDispatcher(event.player)) {
                    event.player.allowFlight = true
                    event.player.isFlying = true
                }
            }
        }

        if (!event.player.hasPermission(VanishPermissionRegistry.VANISH_BYPASS)) {
            vanishService.allOnline().forEach { vanishedPlayer ->
                val vanishedPlayerPriority = vanishedPlayer.getVanishPriority()
                if (joiningPlayerPriority < vanishedPlayerPriority) {
                    event.player.hidePlayer(plugin, vanishedPlayer)
                }
            }
        }

        if (vanishService.isVanished(event.player)) {
            event.joinMessage(null)

            if (vanishService.isSpectating(event.player.uniqueId)) {
                vanishService.createAndShowScoreboard(event.player)
            }

            vanishService.current(event.player)?.player?.let { currentPlayer ->
                glowingApi.makeGlowing(currentPlayer, event.player, VanishConfiguration.GLOW_COLOR)
            }

            Bukkit.getOnlinePlayers()
                .filterNot { it.uniqueId == event.player.uniqueId }.forEach { onlinePlayer ->
                    if (!onlinePlayer.hasPermission(VanishPermissionRegistry.VANISH_BYPASS)) {
                        if (onlinePlayer.getVanishPriority() < joiningPlayerPriority) {
                            onlinePlayer.hidePlayer(plugin, event.player)
                        } else {
                            onlinePlayer.sendText {
                                appendInfoPrefix()
                                variableValue(event.player.name)
                                info(" hat den Server unsichtbar betreten.")
                            }
                        }
                    } else {
                        onlinePlayer.sendText {
                            appendInfoPrefix()
                            variableValue(event.player.name)
                            info(" hat den Server unsichtbar betreten.")
                        }
                    }
                }

            event.player.sendText {
                appendInfoPrefix()
                info("Du bist für andere Spieler unsichtbar.")
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onDisconnect(event: PlayerQuitEvent) {
        vanishService.hideAndDeleteScoreboard(event.player)
        vanishService.setFlyState(event.player.uniqueId, event.player.isFlying)

        if (vanishService.isVanished(event.player)) {
            event.quitMessage(null)

            val leavingPlayerPriority = event.player.getVanishPriority()

            Bukkit.getOnlinePlayers()
                .filterNot { it.uniqueId == event.player.uniqueId }.forEach { onlinePlayer ->
                    if (onlinePlayer.hasPermission(VanishPermissionRegistry.VANISH_BYPASS) ||
                        onlinePlayer.getVanishPriority() > leavingPlayerPriority
                    ) {
                        onlinePlayer.sendText {
                            appendInfoPrefix()
                            variableValue(event.player.name)
                            info(" hat den Server unsichtbar verlassen.")
                        }
                    }
                }
        }
    }
}