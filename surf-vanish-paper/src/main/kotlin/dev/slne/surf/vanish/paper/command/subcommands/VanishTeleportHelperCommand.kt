package dev.slne.surf.vanish.paper.command.subcommands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.vanish.paper.listener.HotkeyListener

fun CommandAPICommand.teleportHelperCommand() = subcommand("teleport-helper") {

    playerExecutor { player, _ ->
        val newState = HotkeyListener.toggleTeleportHelper(player.uniqueId)

        player.sendText {
            appendSuccessPrefix()
            if (newState) {
                success("Der Teleport-Helfer wurde aktiviert.")
            } else {
                success("Der Teleport-Helfer wurde deaktiviert.")
            }
        }
    }
}
