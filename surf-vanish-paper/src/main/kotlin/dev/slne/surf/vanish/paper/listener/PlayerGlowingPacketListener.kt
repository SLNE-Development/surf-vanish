package dev.slne.surf.vanish.paper.listener

import com.github.retrooper.packetevents.event.PacketListener
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import dev.slne.surf.vanish.core.service.spectateModeService
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import kotlin.experimental.and

class PlayerGlowingPacketListener : PacketListener {
    override fun onPacketSend(event: PacketSendEvent) {
        if(event.packetType != PacketType.Play.Server.ENTITY_METADATA) {
            return
        }

        val player = event.getPlayer<Player>() ?: return

        if(!spectateModeService.isSpectating(player.uniqueId)) {
            return
        }

        val packet = WrapperPlayServerEntityMetadata(event)
        val target = Bukkit.getOnlinePlayers().firstOrNull { it.entityId == packet.entityId } ?: return
        val entityMetaData = packet.entityMetadata.filter { it.index == 0 }
            .firstOrNull { it.type == EntityDataTypes.BYTE } ?: return

        event.markForReEncode(true)

        val flags = entityMetaData.value as? Byte ?: return
        val updatedFlags = flags and 0x40.toByte()

        entityMetaData.value = updatedFlags
    }
}