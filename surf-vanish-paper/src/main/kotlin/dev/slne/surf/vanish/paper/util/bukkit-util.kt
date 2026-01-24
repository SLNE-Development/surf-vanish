package dev.slne.surf.vanish.paper.util

import dev.slne.surf.vanish.api.player.VanishOfflinePlayer
import dev.slne.surf.vanish.api.player.VanishPlayer
import dev.slne.surf.vanish.core.service.vanishPlayerService
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

val VanishPlayer.bukkitPlayer
    get() = Bukkit.getPlayer(this.uuid)
        ?: error("VanishPlayer with uuid ${this.uuid} is not online")
val Player.vanishPlayer
    get() = vanishPlayerService.getPlayer(uniqueId)
        ?: error("VanishPlayer for ${this.name} (${this.uniqueId}) not found")
val VanishOfflinePlayer.bukkitPlayer get() = Bukkit.getPlayer(this.uuid)

val UUID.onlineName get() = Bukkit.getPlayer(this)?.name
val UUID.bukkitPlayer get() = Bukkit.getPlayer(this)
val UUID.vanishPlayer get() = vanishPlayerService.getPlayer(this)

/**
 * Gets the vanish priority of a player based on their permissions.
 * Returns the highest priority number from permissions in format: surf.vanish.priority.<number>
 * Returns 0 if no priority permission is found.
 */
fun Player.getVanishPriority(): Int {
    return effectivePermissions
        .asSequence()
        .filter { it.value } // Only consider granted permissions
        .map { it.permission }
        .filter { it.startsWith(VanishPermissionRegistry.VANISH_PRIORITY) }
        .mapNotNull { 
            it.removePrefix("${VanishPermissionRegistry.VANISH_PRIORITY}.")
                .toIntOrNull() 
        }
        .maxOrNull() ?: 0
}