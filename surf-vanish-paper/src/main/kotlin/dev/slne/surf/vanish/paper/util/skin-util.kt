package dev.slne.surf.vanish.paper.util

import com.github.benmanes.caffeine.cache.Caffeine
import com.google.gson.JsonParser
import com.sksamuel.aedile.core.asLoadingCache
import com.sksamuel.aedile.core.expireAfterWrite
import dev.slne.surf.api.core.service.PlayerLookupService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.time.Duration.Companion.hours

data class SkinData(
    val value: String,
    val signature: String
)

private val client = OkHttpClient()

private val skinCache =
    Caffeine.newBuilder().expireAfterWrite(1.hours).asLoadingCache<String, SkinData?> {
        fetchSkin(it)
    }

suspend fun retrieveSkin(userName: String) = skinCache.get(userName)

private suspend fun fetchSkin(userName: String) = withContext(Dispatchers.IO) {
    val uuid = PlayerLookupService.getUuid(userName) ?: return@withContext null

    return@withContext runCatching {
        client.newCall(
            Request.Builder()
                .url("https://sessionserver.mojang.com/session/minecraft/profile/$uuid?unsigned=false")
                .build()
        ).execute().use {
            if (!it.isSuccessful) {
                return@withContext null
            }

            val properties =
                JsonParser.parseString(it.body.string()).asJsonObject["properties"].asJsonArray[0].asJsonObject

            SkinData(
                value = properties["value"].asString,
                signature = properties["signature"].asString
            )
        }
    }.getOrNull()
}