package dev.slne.surf.vanish.paper.util

import dev.slne.surf.surfapi.bukkit.api.permission.PermissionRegistry

object VanishPermissionRegistry : PermissionRegistry() {
    const val BASE = "surf.vanish"
    val VANISH_BYPASS = create("$BASE.bypass")
    val VANISH_COMMAND = create("$BASE.command")
    val VANISH_NOCK_BACK_TP = create("$BASE.no-back-teleport")
    val VANISH_COMMAND_RELOAD = create("$VANISH_COMMAND.reload")

    val VANISH_SAVE_FLY_STATE = create("$BASE.save-fly-state")

    val VANISH_PRIORITY = create("$BASE.priority")
}