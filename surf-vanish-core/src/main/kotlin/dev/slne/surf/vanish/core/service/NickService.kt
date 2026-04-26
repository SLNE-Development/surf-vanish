package dev.slne.surf.vanish.core.service

import dev.slne.surf.api.core.util.requiredService
import org.bukkit.entity.Player

private val service = requiredService<NickService>()

interface NickService {
    fun isNicked(player: Player): Boolean

    suspend fun nick(player: Player, nickname: String)
    fun unnick(player: Player)

    companion object : NickService by service
}