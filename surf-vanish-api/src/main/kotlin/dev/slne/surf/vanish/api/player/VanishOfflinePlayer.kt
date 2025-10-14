package dev.slne.surf.vanish.api.player

import java.util.UUID

interface VanishOfflinePlayer {
    val uuid: UUID

    fun isVanished(): Boolean
}