package dev.slne.surf.vanish.api.redis

import dev.slne.surf.redis.event.RedisEvent
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class VanishStateUpdateRedisEvent(
    val player: @Contextual UUID,
    val vanished: Boolean
) : RedisEvent()
