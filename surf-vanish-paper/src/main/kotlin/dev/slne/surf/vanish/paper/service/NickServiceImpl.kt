package dev.slne.surf.vanish.paper.service

import com.destroystokyo.paper.profile.ProfileProperty
import com.google.auto.service.AutoService
import dev.slne.surf.api.core.luckperms.LuckPermsAccess
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.minimessage.miniMessage
import dev.slne.surf.api.paper.util.getPrefixedName
import dev.slne.surf.vanish.api.event.PlayerNickEvent
import dev.slne.surf.vanish.api.event.PlayerUnNickEvent
import dev.slne.surf.vanish.core.service.NickService
import dev.slne.surf.vanish.paper.util.retrieveSkin
import net.kyori.adventure.util.Services
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@AutoService(NickService::class)
class NickServiceImpl : NickService, Services.Fallback {
    private val nickedPlayers = ConcurrentHashMap<UUID, String>()
    private val oldTextures = ConcurrentHashMap<UUID, ProfileProperty>()

    override fun isNicked(player: Player) = nickedPlayers.containsKey(player.uniqueId)

    override suspend fun nick(player: Player, nickname: String) {
        val textures = retrieveSkin(nickname)

        oldTextures[player.uniqueId] =
            player.playerProfile.properties.find { it.name == "textures" } ?: return
        nickedPlayers[player.uniqueId] = nickname

        player.playerProfile = player.playerProfile.apply {
            setProperty(
                ProfileProperty(
                    "textures",
                    textures?.value ?: "",
                    textures?.signature ?: ""
                )
            )
        }

        player.displayName(buildText {
            append(miniMessage.deserialize("${LuckPermsAccess.luckperms.groupManager.getGroup("default")?.cachedData?.metaData?.prefix ?: ""}$nickname"))
        })

        PlayerNickEvent(player.uniqueId, nickname).callEvent()
    }

    override fun unnick(player: Player) {
        player.playerProfile = player.playerProfile.apply {
            oldTextures[player.uniqueId]?.let { setProperty(it) }
        }

        nickedPlayers.remove(player.uniqueId)
        oldTextures.remove(player.uniqueId)

        player.displayName(player.getPrefixedName())
        PlayerUnNickEvent(player.uniqueId).callEvent()
    }
}