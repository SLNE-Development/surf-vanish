package dev.slne.surf.vanish.api

import dev.slne.surf.surfapi.core.api.util.requiredService
import dev.slne.surf.vanish.api.player.VanishOfflinePlayer
import dev.slne.surf.vanish.api.player.VanishPlayer
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.util.*

interface SurfVanishApi {
    fun vanish(player: VanishPlayer)
    fun reappear(player: VanishPlayer)

    fun isVanished(player: VanishOfflinePlayer): Boolean
    fun vanishedPlayers(): ObjectSet<VanishPlayer>

    fun getPlayer(name: String): VanishPlayer?
    fun getPlayer(uuid: UUID): VanishPlayer?
    fun getOfflinePlayer(uuid: UUID): VanishOfflinePlayer

    companion object {
        val INSTANCE = requiredService<SurfVanishApi>()
    }
}

val surfVanishApi get() = SurfVanishApi.INSTANCE