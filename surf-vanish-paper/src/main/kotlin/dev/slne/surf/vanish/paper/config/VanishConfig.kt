package dev.slne.surf.vanish.paper.config

import dev.slne.surf.surfapi.core.api.config.manager.SpongeConfigManager
import dev.slne.surf.surfapi.core.api.config.surfConfigApi
import dev.slne.surf.vanish.paper.plugin
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class VanishConfig(
    val spoofConnectionMessages: Boolean = true,
    val fakeConnectMessage: String = "<player_name> <yellow>joined",
    val fakeDisconnectMessage: String = "<player_name> <yellow>left",
)

class VanishConfiguration {
    private val configManager: SpongeConfigManager<VanishConfig>

    init {
        surfConfigApi.createSpongeYmlConfig(
            VanishConfig::class.java,
            plugin.dataPath,
            "config.yml"
        )
        configManager = surfConfigApi.getSpongeConfigManagerForConfig(
            VanishConfig::class.java
        )
        reload()
    }

    fun reload() {
        configManager.reloadFromFile()
    }

    val config get() = configManager.config
}
