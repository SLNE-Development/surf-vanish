package dev.slne.surf.vanish.paper.service

import com.google.auto.service.AutoService
import dev.slne.surf.vanish.core.player.VanishOfflinePlayerImpl
import dev.slne.surf.vanish.core.player.VanishPlayerImpl
import dev.slne.surf.vanish.core.service.VanishPlayerService
import net.kyori.adventure.util.Services
import org.bukkit.Bukkit
import java.util.*

@AutoService(VanishPlayerService::class)
class VanishPlayerServiceImpl : VanishPlayerService, Services.Fallback {
    override fun getPlayer(name: String) = Bukkit.getPlayer(name)?.let {
        VanishPlayerImpl(it.name, it.uniqueId)
    }

    override fun getPlayer(uuid: UUID) = Bukkit.getPlayer(uuid)?.let {
        VanishPlayerImpl(it.name, it.uniqueId)
    }

    override fun getOfflinePlayer(uuid: UUID) = VanishOfflinePlayerImpl(uuid)
}