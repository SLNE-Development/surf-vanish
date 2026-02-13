package dev.slne.surf.vanish.paper

import dev.slne.surf.redis.RedisApi
import dev.slne.surf.redis.sync.map.SyncMap
import java.util.*

val redisLoader = BukkitRedisLoader()
val redisApi get() = redisLoader.redisApi

class BukkitRedisLoader {
    lateinit var redisApi: RedisApi
    lateinit var vanishedPlayers: SyncMap<String, List<UUID>>

    fun connect() {
        redisApi = RedisApi.create()
        vanishedPlayers = redisApi.createSyncMap<String, List<UUID>>("surf-vanish:vanished_players")
        redisApi.freezeAndConnect()
    }

    fun disconnect() {
        redisApi.disconnect()
    }
}