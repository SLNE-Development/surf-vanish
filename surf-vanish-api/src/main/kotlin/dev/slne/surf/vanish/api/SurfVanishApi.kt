package dev.slne.surf.vanish.api

import dev.slne.surf.api.core.util.requiredService
import it.unimi.dsi.fastutil.objects.ObjectSet
import org.bukkit.OfflinePlayer
import java.util.*

private val api = requiredService<SurfVanishApi>()

interface SurfVanishApi {
    fun isVanished(player: OfflinePlayer): Boolean
    fun vanishedPlayers(): ObjectSet<OfflinePlayer>
    fun onlineVanishedPlayersUuid(): ObjectSet<UUID>

    companion object : SurfVanishApi by api
}

@Deprecated("Use SurfVanishApi directly instead of surfVanishApi", ReplaceWith("SurfVanishApi"))
val surfVanishApi get() = api