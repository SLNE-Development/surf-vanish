package dev.slne.surf.vanish.core.player

import dev.slne.surf.vanish.api.player.VanishPlayer
import dev.slne.surf.vanish.core.service.vanishService
import java.util.*

data class VanishPlayerImpl(
    override val name: String,
    override val uuid: UUID
) : VanishPlayer {
    override fun vanish() = vanishService.vanish(this)
    override fun reappear() = vanishService.reappear(this)
    override fun isVanished() = vanishService.isVanished(this)
}