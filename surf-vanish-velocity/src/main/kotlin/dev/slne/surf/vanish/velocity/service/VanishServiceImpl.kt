package dev.slne.surf.vanish.velocity.service

import com.google.auto.service.AutoService

import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import dev.slne.surf.vanish.core.service.VanishService
import dev.slne.surf.vanish.core.service.spectateModeService
import dev.slne.surf.vanish.core.service.util.PluginMessageChannels
import dev.slne.surf.vanish.core.service.util.VanishPermissionRegistry
import dev.slne.surf.vanish.velocity.plugin
import dev.slne.surf.vanish.velocity.util.hasPermission
import dev.slne.surf.vanish.velocity.util.toPluginChannel
import it.unimi.dsi.fastutil.objects.ObjectSet

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@AutoService(VanishService::class)
class VanishServiceImpl : VanishService {
    val vanishedPlayers = mutableObjectSetOf<UUID>()

    override fun setVanished(uuid: UUID, vanished: Boolean) {
        if (vanished) {
            vanishedPlayers.add(uuid)
            spectateModeService.setSpectating(uuid, true)
        } else {
            vanishedPlayers.remove(uuid)
            spectateModeService.setSpectating(uuid, false)
        }

        this.pushUpdate(uuid, vanished)
    }

    override fun isVanished(uuid: UUID): Boolean {
        return vanishedPlayers.contains(uuid)
    }

    override fun canSeeVanished(uuid: UUID): Boolean {
        return uuid.hasPermission(VanishPermissionRegistry.VANISH_BYPASS)
    }

    override fun getVanishedPlayers(): ObjectSet<UUID> {
        return vanishedPlayers
    }

    fun pushUpdate(uuid: UUID, vanished: Boolean) {
        val player = plugin.proxy.getPlayer(uuid).getOrNull() ?: return
        val server = player.currentServer.getOrNull() ?: return

        val outputStream = ByteArrayOutputStream()
        val dataOutput = DataOutputStream(outputStream)

        dataOutput.writeUTF(uuid.toString())
        dataOutput.writeBoolean(vanished)

        server.sendPluginMessage(PluginMessageChannels.VANISH_UPDATES.toPluginChannel(), outputStream.toByteArray())
    }
}