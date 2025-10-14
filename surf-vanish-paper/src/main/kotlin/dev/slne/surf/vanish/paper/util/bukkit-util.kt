package dev.slne.surf.vanish.paper.util

import dev.slne.surf.vanish.api.player.VanishOfflinePlayer
import dev.slne.surf.vanish.api.player.VanishPlayer
import dev.slne.surf.vanish.core.service.vanishPlayerService
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

val VanishPlayer.bukkitPlayer
    get() = Bukkit.getPlayer(this.uuid)
        ?: error("VanishPlayer with uuid ${this.uuid} is not online")
val Player.vanishPlayer
    get() = vanishPlayerService.getPlayer(uniqueId)
        ?: error("VanishPlayer for ${this.name} (${this.uniqueId}) not found")
val VanishOfflinePlayer.bukkitPlayer get() = Bukkit.getPlayer(this.uuid)

val UUID.onlineName get() = Bukkit.getPlayer(this)?.name
val UUID.bukkitPlayer get() = Bukkit.getPlayer(this)
val UUID.vanishPlayer get() = vanishPlayerService.getPlayer(this)