package dev.slne.surf.vanish.api.event

import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.util.*

class PlayerNickEvent(
    val playerUuid: UUID,
    val name: String
) : Event() {
    val player get() = Bukkit.getPlayer(playerUuid)

    override fun getHandlers() = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
