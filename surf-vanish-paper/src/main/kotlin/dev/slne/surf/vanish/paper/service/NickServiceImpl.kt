package dev.slne.surf.vanish.paper.service

import com.destroystokyo.paper.profile.ProfileProperty
import com.google.auto.service.AutoService
import dev.slne.surf.api.core.luckperms.LuckPermsAccess
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.minimessage.miniMessage
import dev.slne.surf.api.core.util.mutableObject2ObjectMapOf
import dev.slne.surf.api.paper.util.getPrefixedName
import dev.slne.surf.vanish.api.event.PlayerNickEvent
import dev.slne.surf.vanish.api.event.PlayerUnNickEvent
import dev.slne.surf.vanish.core.service.NickService
import net.kyori.adventure.util.Services
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

@AutoService(NickService::class)
class NickServiceImpl : NickService, Services.Fallback {
    private val nickedPlayers = mutableObject2ObjectMapOf<UUID, String>()
    private val oldTextures = mutableObject2ObjectMapOf<UUID, ProfileProperty>()

    override fun isNicked(player: Player) = player.uniqueId in nickedPlayers

    override fun nick(player: Player, nickname: String) {
        val textures =
            Bukkit.getOfflinePlayer(nickname).playerProfile.properties.find { it.name == "textures" }
        oldTextures[player.uniqueId] =
            player.playerProfile.properties.find { it.name == "textures" }
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

        player.displayName(player.getPrefixedName())
        PlayerUnNickEvent(player.uniqueId).callEvent()
    }
}