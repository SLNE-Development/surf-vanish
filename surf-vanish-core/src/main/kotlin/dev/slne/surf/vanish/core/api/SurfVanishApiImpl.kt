package dev.slne.surf.vanish.core.api

import com.google.auto.service.AutoService
import dev.slne.surf.vanish.api.SurfVanishApi
import dev.slne.surf.vanish.api.player.VanishOfflinePlayer
import dev.slne.surf.vanish.api.player.VanishPlayer
import dev.slne.surf.vanish.core.service.vanishPlayerService
import dev.slne.surf.vanish.core.service.vanishService
import net.kyori.adventure.util.Services
import java.util.*

@AutoService(SurfVanishApi::class)
class SurfVanishApiImpl : SurfVanishApi, Services.Fallback {
    override fun vanish(player: VanishPlayer) = vanishService.vanish(player)
    override fun reappear(player: VanishPlayer) = vanishService.reappear(player)
    override fun isVanished(player: VanishOfflinePlayer) = vanishService.isVanished(player)
    override fun vanishedPlayers() = vanishService.all()

    override fun getPlayer(name: String) = vanishPlayerService.getPlayer(name)
    override fun getPlayer(uuid: UUID) = vanishPlayerService.getPlayer(uuid)
    override fun getOfflinePlayer(uuid: UUID) = vanishPlayerService.getOfflinePlayer(uuid)
}