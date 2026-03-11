package dev.slne.surf.vanish.core.service

import dev.slne.surf.surfapi.core.api.util.requiredService
import dev.slne.surf.vanish.api.player.VanishOfflinePlayer
import dev.slne.surf.vanish.api.player.VanishPlayer
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.util.*

interface VanishService {
    fun vanish(player: VanishPlayer)
    fun reappear(player: VanishPlayer)
    fun isVanished(player: VanishOfflinePlayer): Boolean
    fun all(): ObjectSet<VanishOfflinePlayer>
    fun allOnline(): ObjectSet<VanishPlayer>

    fun previous(player: VanishOfflinePlayer): VanishOfflinePlayer?
    fun next(player: VanishOfflinePlayer): VanishOfflinePlayer?
    fun current(player: VanishOfflinePlayer): VanishOfflinePlayer?

    fun setFlyState(uuid: UUID, canFly: Boolean)
    fun getFlyState(uuid: UUID): Boolean

    fun createAndShowScoreboard(player: VanishPlayer)
    fun hideAndDeleteScoreboard(player: VanishPlayer)

    fun isSpectating(playerUuid: UUID): Boolean
    fun startSpectateMode(player: VanishPlayer)
    fun stopSpectateMode(player: VanishPlayer)

    companion object {
        val INSTANCE = requiredService<VanishService>()
    }
}

val vanishService get() = VanishService.INSTANCE