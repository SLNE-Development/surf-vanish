package dev.slne.surf.vanish.paper.listener

import dev.slne.surf.surfapi.bukkit.api.glow.glowingApi
import dev.slne.surf.vanish.core.service.util.PluginMessageChannels
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.util.UUID

class SpectateModeListener : PluginMessageListener {
    override fun onPluginMessageReceived (
        channel: String,
        player: Player,
        message: ByteArray
    ) {
        when(channel) {
            PluginMessageChannels.SPECTATE_MODE_TELEPORTS -> {
                val input = DataInputStream(ByteArrayInputStream(message))
                val uuid = UUID.fromString(input.readUTF())
                val target = UUID.fromString(input.readUTF())

                val player = Bukkit.getPlayer(uuid) ?: return
                val targetPlayer = Bukkit.getPlayer(target) ?: return

                player.teleportAsync(targetPlayer.location.clone().addRotation(0f, 0f))
            }

            PluginMessageChannels.SPECTATE_MODE_GLOW -> {
                val input = DataInputStream(ByteArrayInputStream(message))

                val uuid = UUID.fromString(input.readUTF())
                val target = UUID.fromString(input.readUTF())
                val glow = input.readBoolean()

                val player = Bukkit.getPlayer(uuid) ?: return
                val targetPlayer = Bukkit.getPlayer(target) ?: return

                if(glow) {
                    glowingApi.makeGlowing(targetPlayer, player, NamedTextColor.RED)
                } else {
                    glowingApi.removeGlowing(targetPlayer, player)
                }
            }
        }
    }
}