package dev.slne.surf.vanish.paper.listener

import dev.slne.surf.surfapi.bukkit.api.event.cancel
import dev.slne.surf.vanish.paper.util.vanishPlayer
import io.papermc.paper.block.TileStateInventoryHolder
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAttemptPickupItemEvent
import org.bukkit.event.player.PlayerInteractEvent

object SilenceListener : Listener {
    @EventHandler
    fun onPickup(event: PlayerAttemptPickupItemEvent) {
        val vanishPlayer = event.player.vanishPlayer

        if (vanishPlayer.isVanished()) {
            event.cancel()
        }
    }

    @EventHandler
    fun onContainerOpen(event: PlayerInteractEvent) {
        val player = event.player
        val vanishPlayer = player.vanishPlayer

        if (!vanishPlayer.isVanished()) {
            return
        }

        val block = event.clickedBlock ?: return
        val blockState = block.state

        if (blockState is TileStateInventoryHolder) {
            event.cancel()

            player.openInventory(blockState.inventory)
        }
    }
}