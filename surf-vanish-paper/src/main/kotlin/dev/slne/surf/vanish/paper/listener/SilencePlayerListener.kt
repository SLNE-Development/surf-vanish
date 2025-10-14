package dev.slne.surf.vanish.paper.listener

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent
import dev.slne.surf.surfapi.bukkit.api.event.cancel
import dev.slne.surf.vanish.paper.util.vanishPlayer
import io.papermc.paper.block.TileStateInventoryHolder
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.EntityBlockFormEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.*
import org.bukkit.event.raid.RaidTriggerEvent

object SilencePlayerListener : Listener {
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


    @EventHandler
    fun onDrop(event: PlayerDropItemEvent) {
        val vanishPlayer = event.player.vanishPlayer

        if (vanishPlayer.isVanished()) {
            event.cancel()
        }
    }

    @EventHandler
    fun onPickupArrow(event: PlayerPickupArrowEvent) {
        val vanishPlayer = event.player.vanishPlayer

        if (vanishPlayer.isVanished()) {
            event.cancel()
        }
    }

    @EventHandler
    fun onRaidTrigger(event: RaidTriggerEvent) {
        val vanishPlayer = event.player.vanishPlayer

        if (vanishPlayer.isVanished()) {
            event.cancel()
        }
    }

    @EventHandler
    fun onBlockForm(event: EntityBlockFormEvent) {
        val entity = event.entity

        if (entity is Player) {
            val vanishPlayer = entity.vanishPlayer

            if (vanishPlayer.isVanished()) {
                event.cancel()
            }
        }
    }

    @EventHandler
    fun onFoodChange(event: FoodLevelChangeEvent) {
        val entity = event.entity

        if (entity is Player) {
            val vanishPlayer = entity.vanishPlayer

            if (vanishPlayer.isVanished()) {
                event.cancel()
            }
        }
    }

    @EventHandler
    fun onXpPickup(event: PlayerPickupExperienceEvent) {
        val vanishPlayer = event.player.vanishPlayer

        if (vanishPlayer.isVanished()) {
            event.cancel()
        }
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        val entity = event.entity

        if (entity is Player) {
            val vanishPlayer = entity.vanishPlayer

            if (vanishPlayer.isVanished()) {
                event.cancel()
            }
        }
    }

    @EventHandler
    fun onBucketFill(event: PlayerBucketFillEvent) {
        val vanishPlayer = event.player.vanishPlayer

        if (vanishPlayer.isVanished()) {
            event.cancel()
        }
    }

    @EventHandler
    fun onBucketEmtry(event: PlayerBucketEmptyEvent) {
        val vanishPlayer = event.player.vanishPlayer

        if (vanishPlayer.isVanished()) {
            event.cancel()
        }
    }
}