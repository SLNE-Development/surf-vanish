package dev.slne.surf.vanish.paper.service

import com.google.auto.service.AutoService
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import dev.slne.surf.vanish.api.player.VanishOfflinePlayer
import dev.slne.surf.vanish.api.player.VanishPlayer
import dev.slne.surf.vanish.core.service.VanishService
import dev.slne.surf.vanish.paper.PaperPackets
import dev.slne.surf.vanish.paper.config
import dev.slne.surf.vanish.paper.util.VanishPermissionRegistry
import dev.slne.surf.vanish.paper.util.packetPlayer
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.util.Services
import org.bukkit.Bukkit
import java.util.*

@AutoService(VanishService::class)
class VanishServiceImpl : VanishService, Services.Fallback {
    private val _vanishedPlayers = mutableObjectSetOf<UUID>()

    override fun vanish(player: VanishPlayer) {
        val removePacket = PaperPackets.createRemovePlayerPacket(player)
        val hidePacket = PaperPackets.createHidePlayerPacket(player)

        _vanishedPlayers.add(player.uuid)

        Bukkit.getOnlinePlayers()
            .filterNot { it.hasPermission(VanishPermissionRegistry.VANISH_BYPASS) }.forEach {
                it.packetPlayer.sendPacket(removePacket)
                it.packetPlayer.sendPacket(hidePacket)

                if (config.spoofConnectionMessages) {
                    it.sendText {
                        append(MiniMessage.miniMessage().deserialize(config.fakeDisconnectMessage))
                    }
                }
            }
    }

    override fun reappear(player: VanishPlayer) {
        val showPacket = PaperPackets.createShowPlayerPacket(player)
        val reappearPacket = PaperPackets.createReappearPlayerPacket(player)

        _vanishedPlayers.remove(player.uuid)

        Bukkit.getOnlinePlayers()
            .filterNot { it.hasPermission(VanishPermissionRegistry.VANISH_BYPASS) }.forEach {
                it.packetPlayer.sendPacket(reappearPacket)
                it.packetPlayer.sendPacket(showPacket)

                if (config.spoofConnectionMessages) {
                    it.sendText {
                        append(MiniMessage.miniMessage().deserialize(config.fakeConnectMessage))
                    }
                }
            }
    }

    override fun isVanished(player: VanishOfflinePlayer) = _vanishedPlayers.contains(player.uuid)
}