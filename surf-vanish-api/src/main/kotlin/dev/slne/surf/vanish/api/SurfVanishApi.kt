package dev.slne.surf.vanish.api

import dev.slne.surf.api.core.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectSet
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*

interface SurfVanishApi {
    fun vanish(player: Player)
    fun reappear(player: Player)

    fun isVanished(player: OfflinePlayer): Boolean
    fun vanishedPlayers(): ObjectSet<OfflinePlayer>
    fun onlineVanishedPlayersUuid(): ObjectSet<UUID>

    companion object {
        val INSTANCE = requiredService<SurfVanishApi>()
    }
}

val surfVanishApi get() = SurfVanishApi.INSTANCE