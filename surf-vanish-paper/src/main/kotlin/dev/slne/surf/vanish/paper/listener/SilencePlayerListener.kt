package dev.slne.surf.vanish.paper.listener

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent
import dev.slne.surf.surfapi.bukkit.api.event.cancel
import dev.slne.surf.vanish.core.service.vanishService
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.EntityBlockFormEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.*
import org.bukkit.event.raid.RaidTriggerEvent

object SilencePlayerListener : Listener {
    @EventHandler
    fun onPickup(event: PlayerAttemptPickupItemEvent) {
        if (vanishService.isVanished(event.player)) {
            event.cancel()
        }
    }

    @EventHandler
    fun onPressurePlateActive(event: PlayerInteractEvent) {
        val player = event.player

        if (!vanishService.isVanished(event.player)) {
            return
        }

        if (event.action != Action.PHYSICAL) {
            return
        }

        event.cancel()
    }

    @EventHandler
    fun onEntityTarget(event: EntityTargetEvent) {
        val target = event.target as? Player ?: return

        if (vanishService.isVanished(target)) {
            event.cancel()
        }
    }


    @EventHandler
    fun onDrop(event: PlayerDropItemEvent) {
        if (vanishService.isVanished(event.player)) {
            event.cancel()
        }
    }

    @EventHandler
    fun onPickupArrow(event: PlayerPickupArrowEvent) {
        if (vanishService.isVanished(event.player)) {
            event.cancel()
        }
    }

    @EventHandler
    fun onRaidTrigger(event: RaidTriggerEvent) {
        if (vanishService.isVanished(event.player)) {
            event.cancel()
        }
    }

    @EventHandler
    fun onBlockForm(event: EntityBlockFormEvent) {
        val entity = event.entity

        if (entity is Player) {
            if (vanishService.isVanished(entity)) {
                event.cancel()
            }
        }
    }

    @EventHandler
    fun onFoodChange(event: FoodLevelChangeEvent) {
        val entity = event.entity

        if (entity is Player) {
            if (vanishService.isVanished(entity)) {
                event.cancel()
            }
        }
    }

    @EventHandler
    fun onXpPickup(event: PlayerPickupExperienceEvent) {
        if (vanishService.isVanished(event.player)) {
            event.cancel()
        }
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        val entity = event.entity

        if (entity is Player)
            if (vanishService.isVanished(entity)) {
                event.cancel()
            }
    }

    @EventHandler
    fun onBucketFill(event: PlayerBucketFillEvent) {
        if (vanishService.isVanished(event.player)) {
            event.cancel()
        }
    }

    @EventHandler
    fun onBucketEmpty(event: PlayerBucketEmptyEvent) {

        if (vanishService.isVanished(event.player)) {
            event.cancel()
        }
    }
}