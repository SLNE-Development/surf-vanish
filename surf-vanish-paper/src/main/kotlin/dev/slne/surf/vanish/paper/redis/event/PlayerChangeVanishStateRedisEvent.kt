package dev.slne.surf.vanish.paper.redis.event

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class PlayerChangeVanishStateRedisEvent(
    val playerUuid: @Contextual UUID,
    val vanished: Boolean
)
