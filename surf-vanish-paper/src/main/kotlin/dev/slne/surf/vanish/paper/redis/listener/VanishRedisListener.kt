package dev.slne.surf.vanish.paper.redis.listener

import dev.slne.surf.redis.event.OnRedisEvent
import dev.slne.surf.vanish.paper.redis.event.PlayerChangeVanishStateRedisEvent

class VanishRedisListener {
    @OnRedisEvent
    fun onPlayerVanishStateChange(event: PlayerChangeVanishStateRedisEvent) {
        
    }
}