package dev.slne.surf.vanish.paper

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.PacketEventsAPI
import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.surfapi.bukkit.api.event.register
import dev.slne.surf.vanish.paper.command.vanishCommand
import dev.slne.surf.vanish.paper.config.VanishConfiguration
import dev.slne.surf.vanish.paper.listener.ConnectionListener
import org.bukkit.plugin.java.JavaPlugin

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)

class PaperMain : SuspendingJavaPlugin() {
    override fun onEnable() {
        vanishCommand()

        ConnectionListener.register()
    }

    override fun onDisable() {
        super.onDisable()
    }

    val vanishConfig = VanishConfiguration()
}

val config get() = plugin.vanishConfig.config
val packetEvents: PacketEventsAPI<*> by lazy { PacketEvents.getAPI() }