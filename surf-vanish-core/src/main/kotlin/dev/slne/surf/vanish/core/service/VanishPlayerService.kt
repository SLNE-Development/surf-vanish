package dev.slne.surf.vanish.core.service

import dev.slne.surf.surfapi.core.api.util.requiredService
import dev.slne.surf.vanish.api.player.VanishOfflinePlayer
import dev.slne.surf.vanish.api.player.VanishPlayer
import java.util.UUID

interface VanishPlayerService {
    fun getPlayer(name: String): VanishPlayer?
    fun getPlayer(uuid: UUID): VanishPlayer?

    fun getOfflinePlayer(uuid: UUID): VanishOfflinePlayer

    companion object {
        val INSTANCE = requiredService<VanishPlayerService>()
    }
}

val vanishPlayerService get() = VanishPlayerService.INSTANCE