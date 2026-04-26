package dev.slne.surf.vanish.core.api

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.util.toObjectSet
import dev.slne.surf.vanish.api.SurfVanishApi
import dev.slne.surf.vanish.core.service.vanishService
import net.kyori.adventure.util.Services
import org.bukkit.OfflinePlayer

@AutoService(SurfVanishApi::class)
class SurfVanishApiImpl : SurfVanishApi, Services.Fallback {
    override fun isVanished(player: OfflinePlayer) = vanishService.isVanished(player)
    override fun vanishedPlayers() = vanishService.all()
    override fun onlineVanishedPlayersUuid() =
        vanishService.allOnline().map { it.uniqueId }.toObjectSet()
}