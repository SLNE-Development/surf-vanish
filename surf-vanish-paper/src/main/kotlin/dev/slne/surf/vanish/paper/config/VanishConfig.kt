package dev.slne.surf.vanish.paper.config

import dev.slne.surf.api.core.config.SurfConfigApi
import dev.slne.surf.api.core.config.manager.SpongeConfigManager
import dev.slne.surf.vanish.paper.plugin
import net.kyori.adventure.text.format.NamedTextColor
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class VanishConfig(
    val spoofConnectionMessages: Boolean = true
)

class VanishConfiguration {
    private val configManager: SpongeConfigManager<VanishConfig>

    init {
        SurfConfigApi.createSpongeYmlConfig(
            VanishConfig::class.java,
            plugin.dataPath,
            "config.yml"
        )
        configManager = SurfConfigApi.getSpongeConfigManagerForConfig(
            VanishConfig::class.java
        )
        reload()
    }

    fun reload() {
        configManager.reloadFromFile()
    }

    val config get() = configManager.config

    companion object {
        val GLOW_COLOR: NamedTextColor = NamedTextColor.GREEN
    }
}
