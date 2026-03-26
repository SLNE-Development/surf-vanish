package dev.slne.surf.vanish.paper.listener

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent
import dev.slne.surf.vanish.paper.util.canVanishSee
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object TabCompleteListener : Listener {
    @EventHandler
    fun onTabComplete(event: AsyncTabCompleteEvent) {
        val player = event.sender as? Player ?: return
        val completions = event.completions

        val iterator = completions.iterator()

        while (iterator.hasNext()) {
            val suggestion = iterator.next()

            val target = Bukkit.getPlayerExact(suggestion) ?: continue

            if (!player.canVanishSee(target)) {
                iterator.remove()
            }
        }
    }
}