package dev.slne.surf.vanish.core.service

import dev.slne.surf.surfapi.core.api.util.requiredService
import dev.slne.surf.vanish.api.player.VanishOfflinePlayer
import dev.slne.surf.vanish.api.player.VanishPlayer

interface VanishService {
    fun vanish(player: VanishPlayer)
    fun reappear(player: VanishPlayer)
    fun isVanished(player: VanishOfflinePlayer): Boolean

    companion object {
        val INSTANCE = requiredService<VanishService>()
    }
}

val vanishService get() = VanishService.INSTANCE