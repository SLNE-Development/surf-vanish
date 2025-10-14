package dev.slne.surf.vanish.paper.service

import com.google.auto.service.AutoService
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import dev.slne.surf.surfapi.core.api.util.toObjectSet
import dev.slne.surf.vanish.api.player.VanishOfflinePlayer
import dev.slne.surf.vanish.api.player.VanishPlayer
import dev.slne.surf.vanish.core.service.VanishService
import dev.slne.surf.vanish.core.service.vanishPlayerService
import dev.slne.surf.vanish.paper.config
import dev.slne.surf.vanish.paper.plugin
import dev.slne.surf.vanish.paper.util.VanishPermissionRegistry
import dev.slne.surf.vanish.paper.util.bukkitPlayer
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.util.Services
import org.bukkit.Bukkit
import java.util.*

@AutoService(VanishService::class)
class VanishServiceImpl : VanishService, Services.Fallback {
    private val _vanishedPlayers = mutableObjectSetOf<UUID>()

    override fun vanish(player: VanishPlayer) {
        _vanishedPlayers.add(player.uuid)

        Bukkit.getOnlinePlayers()
            .filterNot { it.hasPermission(VanishPermissionRegistry.VANISH_BYPASS) }.forEach {
                it.hidePlayer(
                    plugin,
                    player.bukkitPlayer
                ) //"Das kann man nicht umgehen, das ist Quatsch." ~ Keviro, 14.10.2025 - 11:01:22 Uhr GMT+2

                if (config.spoofConnectionMessages) {
                    it.sendText {
                        append(MiniMessage.miniMessage().deserialize(config.fakeDisconnectMessage))
                    }
                }
            }
    }

    override fun reappear(player: VanishPlayer) {
        _vanishedPlayers.remove(player.uuid)

        Bukkit.getOnlinePlayers()
            .filterNot { it.hasPermission(VanishPermissionRegistry.VANISH_BYPASS) }.forEach {

                it.showPlayer(
                    plugin,
                    player.bukkitPlayer
                ) //"Das kann man nicht umgehen, das ist Quatsch." ~ Keviro, 14.10.2025 - 11:01:22 Uhr GMT+2

                if (config.spoofConnectionMessages) {
                    it.sendText {
                        append(MiniMessage.miniMessage().deserialize(config.fakeConnectMessage))
                    }
                }
            }
    }

    override fun isVanished(player: VanishOfflinePlayer) = _vanishedPlayers.contains(player.uuid)
    override fun all(): ObjectSet<VanishPlayer> =
        _vanishedPlayers.mapNotNull { vanishPlayerService.getPlayer(it) }.toObjectSet()
}