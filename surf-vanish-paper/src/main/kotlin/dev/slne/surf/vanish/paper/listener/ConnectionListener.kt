package dev.slne.surf.vanish.paper.listener

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.regionDispatcher
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.glow.SurfGlowingApi
import dev.slne.surf.vanish.core.service.vanishService
import dev.slne.surf.vanish.paper.config.VanishConfiguration
import dev.slne.surf.vanish.paper.plugin
import dev.slne.surf.vanish.paper.util.VanishPermissionRegistry
import dev.slne.surf.vanish.paper.util.canVanishSee
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

object ConnectionListener : Listener {
    @EventHandler
    fun onConnect(event: PlayerJoinEvent) {
        val player = event.player

        if (vanishService.isVanished(player)) {
            event.joinMessage(null)
        }

        plugin.launch(plugin.entityDispatcher(player)) {
            if (player.hasPermission(VanishPermissionRegistry.VANISH_SAVE_FLY_STATE)) {
                if (vanishService.getFlyState(player.uniqueId)) {
                    player.allowFlight = true
                    player.isFlying = true
                }
            }

            vanishService.allOnline().forEach { vanishedPlayer ->
                if (!player.canVanishSee(vanishedPlayer)) {
                    player.hidePlayer(plugin, vanishedPlayer)
                }
            }

            if (vanishService.isVanished(player)) {
                if (vanishService.isSpectating(player.uniqueId)) {
                    vanishService.createAndShowScoreboard(player)
                }

                vanishService.current(player)?.player?.let { currentPlayer ->
                    SurfGlowingApi.makeGlowing(
                        currentPlayer,
                        player,
                        VanishConfiguration.GLOW_COLOR
                    )
                }

                Bukkit.getOnlinePlayers()
                    .filterNot { it.uniqueId == player.uniqueId }
                    .forEach { onlinePlayer ->

                        launch(plugin.regionDispatcher(onlinePlayer.location)) {
                            if (!onlinePlayer.canVanishSee(player)) {
                                onlinePlayer.hidePlayer(plugin, player)
                            } else {
                                onlinePlayer.sendText {
                                    appendInfoPrefix()
                                    variableValue(player.name)
                                    info(" hat den Server unsichtbar betreten.")
                                }
                            }
                        }
                    }

                player.sendText {
                    appendInfoPrefix()
                    info("Du bist für andere Spieler unsichtbar.")
                }
            }
        }
    }

    @EventHandler
    fun onDisconnect(event: PlayerQuitEvent) {
        val player = event.player

        vanishService.hideAndDeleteScoreboard(player)
        vanishService.setFlyState(player.uniqueId, player.isFlying)

        if (vanishService.isVanished(player)) {
            event.quitMessage(null)

            Bukkit.getOnlinePlayers()
                .filterNot { it.uniqueId == player.uniqueId }
                .forEach { onlinePlayer ->
                    if (onlinePlayer.canVanishSee(player)) {
                        onlinePlayer.sendText {
                            appendInfoPrefix()
                            variableValue(player.name)
                            info(" hat den Server unsichtbar verlassen.")
                        }
                    }
                }
        }
    }
}