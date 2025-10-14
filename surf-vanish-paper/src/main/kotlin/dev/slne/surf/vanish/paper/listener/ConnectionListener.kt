package dev.slne.surf.vanish.paper.listener

import dev.slne.surf.vanish.paper.plugin
import dev.slne.surf.vanish.paper.util.VanishPermissionRegistry
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

        if (vanishPlayer.isVanished()) {
            event.joinMessage(null)

            Bukkit.getOnlinePlayers()
                .filterNot { it.hasPermission(VanishPermissionRegistry.VANISH_BYPASS) }.forEach {
                    it.hidePlayer(plugin, event.player)
                }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onDisconnect(event: PlayerQuitEvent) {
        val vanishPlayer = event.player.vanishPlayer

        if (vanishPlayer.isVanished()) {
            event.quitMessage(null)

            Bukkit.getOnlinePlayers()
                .filterNot { it.hasPermission(VanishPermissionRegistry.VANISH_BYPASS) }.forEach {
                    it.showPlayer(plugin, event.player)
                }
        }
    }
}