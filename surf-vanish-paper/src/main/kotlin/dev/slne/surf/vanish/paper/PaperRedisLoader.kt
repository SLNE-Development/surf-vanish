package dev.slne.surf.vanish.paper

import dev.slne.surf.redis.RedisApi

val redisLoader = BukkitRedisLoader()
val redisApi get() = redisLoader.redisApi

class BukkitRedisLoader {
    lateinit var redisApi: RedisApi

    fun connect() {
        redisApi = RedisApi.create()
        redisApi.freezeAndConnect()
    }

    fun disconnect() {
        redisApi.disconnect()
    }
}