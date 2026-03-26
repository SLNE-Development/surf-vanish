package dev.slne.surf.vanish.paper.hook

import net.luckperms.api.LuckPermsProvider
import org.bukkit.entity.Player

object LuckPermsHook {
    private val luckPerms by lazy {
        LuckPermsProvider.get()
    }

    fun getPrefix(player: Player) =
        luckPerms.userManager.getUser(player.uniqueId)?.cachedData?.metaData?.prefix ?: ""
}