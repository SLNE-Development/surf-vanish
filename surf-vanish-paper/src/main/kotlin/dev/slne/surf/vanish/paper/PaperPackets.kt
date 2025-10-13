package dev.slne.surf.vanish.paper

import com.github.retrooper.packetevents.protocol.chat.RemoteChatSession
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import com.github.retrooper.packetevents.util.Vector3d
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate.Action
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import dev.slne.surf.vanish.api.player.VanishPlayer
import dev.slne.surf.vanish.paper.util.bukkitPlayer
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import java.util.*

object PaperPackets {
    private val _playerChatSessions = mutableObject2ObjectMapOf<UUID, RemoteChatSession>()

    fun createRemovePlayerPacket(player: VanishPlayer): WrapperPlayServerDestroyEntities =
        WrapperPlayServerDestroyEntities(player.bukkitPlayer.entityId)

    fun createHidePlayerPacket(player: VanishPlayer) = WrapperPlayServerPlayerInfoUpdate(
        Action.UPDATE_LISTED,
        WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
            player.uuid
        ).apply {
            isListed = false
        }
    )

    fun createReappearPlayerPacket(player: VanishPlayer) = WrapperPlayServerPlayerInfoUpdate(
        Action.UPDATE_LISTED,
        WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
            player.uuid
        ).apply {
            isListed = true
        }
    )

    fun createShowPlayerPacket(player: VanishPlayer) = WrapperPlayServerSpawnEntity(
        player.bukkitPlayer.entityId,
        player.bukkitPlayer.uniqueId,
        EntityTypes.PLAYER,
        SpigotConversionUtil.fromBukkitLocation(player.bukkitPlayer.location),
        player.bukkitPlayer.yaw,
        0,
        player.bukkitPlayer.velocity.let {
            Vector3d(
                it.x,
                it.y,
                it.z
            )
        }
    )
}