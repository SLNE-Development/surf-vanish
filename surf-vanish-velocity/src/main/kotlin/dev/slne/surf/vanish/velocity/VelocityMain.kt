package dev.slne.surf.vanish.velocity

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListenerPriority
import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.proxy.ProxyServer
import dev.slne.surf.vanish.core.service.spectateModeService
import dev.slne.surf.vanish.core.service.util.PluginMessageChannels
import dev.slne.surf.vanish.velocity.command.vanish.VanishCommand
import dev.slne.surf.vanish.velocity.listener.PlayerConnectionListener
import dev.slne.surf.vanish.velocity.listener.PlayerPacketListener
import dev.slne.surf.vanish.velocity.listener.SilentChestPacketListener
import dev.slne.surf.vanish.velocity.util.toPluginChannel
import org.slf4j.Logger
import kotlin.jvm.optionals.getOrNull

class VelocityMain @Inject constructor(
    val proxy: ProxyServer,
    val logger: Logger,
    suspendingPluginContainer: SuspendingPluginContainer
) {
    init {
        suspendingPluginContainer.initialize(this)
    }

    @Subscribe
    fun onInitialization(event: ProxyInitializeEvent) {
        INSTANCE = this
        proxy.channelRegistrar.register(PluginMessageChannels.VANISH_UPDATES.toPluginChannel())
        proxy.channelRegistrar.register(PluginMessageChannels.SPECTATE_MODE_TELEPORTS.toPluginChannel())
        proxy.channelRegistrar.register(PluginMessageChannels.SPECTATE_MODE_GLOW.toPluginChannel())
        proxy.channelRegistrar.register(PluginMessageChannels.SILENT_CHEST.toPluginChannel())
        proxy.eventManager.register(this, PlayerConnectionListener())

        PacketEvents.getAPI().eventManager.registerListener(PlayerPacketListener(), PacketListenerPriority.NORMAL)
        PacketEvents.getAPI().eventManager.registerListener(SilentChestPacketListener(), PacketListenerPriority.NORMAL)

        VanishCommand("vanish").register()

        spectateModeService.startJob()
    }

    companion object {
        lateinit var INSTANCE: VelocityMain
            private set
    }
}

val plugin get() = VelocityMain.INSTANCE
val container get() = plugin.proxy.pluginManager.getPlugin("surf-vanish-velocity").getOrNull() ?: error("The providing plugin container is not available. Got the plugin ID changed?")