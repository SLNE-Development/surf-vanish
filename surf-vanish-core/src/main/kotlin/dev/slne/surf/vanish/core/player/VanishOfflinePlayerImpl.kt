package dev.slne.surf.vanish.core.player

import dev.slne.surf.vanish.api.player.VanishOfflinePlayer
import dev.slne.surf.vanish.core.service.vanishService
import java.util.*

data class VanishOfflinePlayerImpl(
    override val uuid: UUID
) : VanishOfflinePlayer {
    override fun isVanished() = vanishService.isVanished(this)
}