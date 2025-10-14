package dev.slne.surf.vanish.paper.config

import dev.slne.surf.surfapi.core.api.config.manager.SpongeConfigManager
import dev.slne.surf.surfapi.core.api.config.surfConfigApi
import dev.slne.surf.vanish.paper.plugin
import net.kyori.adventure.text.format.NamedTextColor
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class VanishConfig(
    val spoofConnectionMessages: Boolean = true,
    val fakeConnectMessage: String = "<dark_gray>[<green>+<dark_gray>] <luckperms_prefix><player_name>",
    val fakeDisconnectMessage: String = "<dark_gray>[<red>-<dark_gray>] <luckperms_prefix><player_name>",
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

    companion object {
        val GLOW_COLOR: NamedTextColor = NamedTextColor.GREEN
    }
}
