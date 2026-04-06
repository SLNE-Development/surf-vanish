package dev.slne.surf.vanish.paper.util

import dev.slne.surf.vanish.core.service.vanishService
import dev.slne.surf.vanish.paper.plugin
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import java.util.*


val Player.currentTarget get() = vanishService.current(this)

val UUID.onlineName get() = Bukkit.getPlayer(this)?.name
val UUID.bukkitPlayer get() = Bukkit.getPlayer(this)

fun Player.getVanishPriority(): Int {
    return effectivePermissions
        .asSequence()
        .filter { it.value }
        .map { it.permission }
        .filter { it.startsWith(VanishPermissionRegistry.VANISH_PRIORITY) }
        .mapNotNull {
            it.removePrefix("${VanishPermissionRegistry.VANISH_PRIORITY}.")
                .toIntOrNull()
        }
        .maxOrNull() ?: 0
}

fun Player.canVanishSee(target: Player): Boolean {
    if (this.uniqueId == target.uniqueId) return true

    if (this.hasPermission(VanishPermissionRegistry.VANISH_BYPASS)) return true

    val ownPriority = this.getVanishPriority()
    val targetPriority = target.getVanishPriority()

    return ownPriority >= targetPriority
}

@Suppress("DEPRECATION")
fun Player.setMetaVanished(vanished: Boolean) {
    if (vanished) {
        this.setMetadata("vanished", FixedMetadataValue(plugin, true))
    } else {
        this.removeMetadata("vanished", plugin)
    }
}