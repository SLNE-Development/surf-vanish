package dev.slne.surf.vanish.paper.util

import com.destroystokyo.paper.profile.ProfileProperty
import com.github.retrooper.packetevents.protocol.player.TextureProperty
import com.github.retrooper.packetevents.protocol.player.User
import dev.slne.surf.vanish.api.player.VanishPlayer
import dev.slne.surf.vanish.core.service.vanishPlayerService
import dev.slne.surf.vanish.paper.packetEvents
import org.bukkit.Bukkit
import org.bukkit.entity.Player

val VanishPlayer.bukkitPlayer
    get() = Bukkit.getPlayer(this.uuid)
        ?: error("VanishPlayer with uuid ${this.uuid} is not online")
val Player.vanishPlayer
    get() = vanishPlayerService.getPlayer(uniqueId)
        ?: error("VanishPlayer for ${this.name} (${this.uniqueId}) not found")

val Player.packetPlayer: User get() = packetEvents.playerManager.getUser(this)

fun ProfileProperty.toTextureProperty() = TextureProperty(
    this.name,
    this.value,
    this.signature
)