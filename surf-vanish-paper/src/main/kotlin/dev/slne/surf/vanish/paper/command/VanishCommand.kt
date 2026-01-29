package dev.slne.surf.vanish.paper.command

import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.vanish.paper.plugin
import dev.slne.surf.vanish.paper.util.VanishPermissionRegistry
import dev.slne.surf.vanish.paper.util.displayKey
import dev.slne.surf.vanish.paper.util.vanishPlayer
import kotlin.system.measureTimeMillis

fun vanishCommand() = commandTree("vanish") {
    withPermission(VanishPermissionRegistry.VANISH_COMMAND)
    playerExecutor { player, _ ->
        val vanishPlayer = player.vanishPlayer

        if (vanishPlayer.isVanished()) {
            vanishPlayer.reappear()
            player.sendText {
                appendSuccessPrefix()
                success("Du bist nun sichtbar.")
            }
        } else {
            vanishPlayer.vanish()

            player.sendText {
                appendNewInfoPrefixedLine()
                spacer("-".repeat(10))

                appendNewInfoPrefixedLine()

                appendNewInfoPrefixedLine()
                info("Du bist nun unsichtbar!")

                appendNewInfoPrefixedLine()

                appendNewInfoPrefixedLine()
                primary("Zurück: ")
                displayKey("sneak")
                spacer(" + ")
                displayKey("swapOffhand")
                
                appendNewInfoPrefixedLine()
                primary("Weiter: ")
                displayKey("swapOffhand")

                appendNewInfoPrefixedLine()
                primary("Teleport: ")
                white("2x ")
                displayKey("sneak")

                appendNewInfoPrefixedLine()

                appendNewInfoPrefixedLine()
                spacer("-".repeat(10))
            }
        }
    }

    literalArgument("reload") {
        withPermission(VanishPermissionRegistry.VANISH_COMMAND_RELOAD)
        anyExecutor { executor, _ ->
            val ms = measureTimeMillis {
                plugin.vanishConfig.reload()
            }

            executor.sendText {
                appendSuccessPrefix()
                success("Die Konfiguration wurde neu geladen (${ms}ms)!")
            }
        }
    }
}