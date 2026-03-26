package dev.slne.surf.vanish.core.service

import dev.slne.surf.surfapi.core.api.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectSet
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*

interface VanishService {
    fun vanish(player: Player)
    fun reappear(player: Player)
    fun isVanished(player: OfflinePlayer): Boolean
    fun all(): ObjectSet<OfflinePlayer>
    fun allOnline(): ObjectSet<Player>

    fun previous(player: Player): OfflinePlayer?
    fun next(player: Player): OfflinePlayer?
    fun current(player: Player): OfflinePlayer?

    fun setFlyState(uuid: UUID, canFly: Boolean)
    fun getFlyState(uuid: UUID): Boolean

    fun createAndShowScoreboard(player: Player)
    fun hideAndDeleteScoreboard(player: Player)

    fun isSpectating(playerUuid: UUID): Boolean
    fun startSpectateMode(player: Player)
    fun stopSpectateMode(player: Player)

    companion object {
        val INSTANCE = requiredService<VanishService>()
    }
}

val vanishService get() = VanishService.INSTANCE