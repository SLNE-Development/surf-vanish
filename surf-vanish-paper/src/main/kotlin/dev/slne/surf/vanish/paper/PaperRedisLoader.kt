package dev.slne.surf.vanish.paper

import dev.slne.surf.redis.RedisApi
import dev.slne.surf.redis.sync.set.SyncSet
import java.util.*

val redisLoader = BukkitRedisLoader()
val redisApi get() = redisLoader.redisApi

class BukkitRedisLoader {
    lateinit var redisApi: RedisApi
    lateinit var vanishedPlayers: SyncSet<UUID>

    fun connect() {
        redisApi = RedisApi.create()
        vanishedPlayers = redisApi.createSyncSet<UUID>("vanished_players")
        redisApi.freezeAndConnect()
    }

    fun disconnect() {
        redisApi.disconnect()
    }
}