package dev.slne.surf.vanish.paper.listener

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent
import dev.slne.surf.surfapi.bukkit.api.event.cancel
import dev.slne.surf.vanish.paper.util.vanishPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.EntityBlockFormEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
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
    fun onPressurePlateActive(event: PlayerInteractEvent) {
        val player = event.player
        val vanishPlayer = player.vanishPlayer

        if (!vanishPlayer.isVanished()) {
            return
        }

        if (event.action != Action.PHYSICAL) {
            return
        }

        event.cancel()
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
    fun onBucketEmpty(event: PlayerBucketEmptyEvent) {
        val vanishPlayer = event.player.vanishPlayer

        if (vanishPlayer.isVanished()) {
            event.cancel()
        }
    }

    @EventHandler
    fun onEntityTargetLivingEntity(event: EntityTargetLivingEntityEvent) {
        val target = event.target as? Player ?: return
        val vanishPlayer = target.vanishPlayer

        if (vanishPlayer.isVanished()) {
            event.cancel()
        }
    }

}