package dev.slne.surf.vanish.paper.util

import dev.slne.surf.surfapi.bukkit.api.permission.PermissionRegistry

object VanishPermissionRegistry : PermissionRegistry() {
    const val BASE = "surf.vanish"
    val VANISH_BYPASS = create("$BASE.bypass")
    val VANISH_COMMAND = create("$BASE.command")
}