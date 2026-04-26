package dev.slne.surf.vanish.paper.command

import dev.jorel.commandapi.kotlindsl.*
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.vanish.core.service.NickService
import dev.slne.surf.vanish.paper.util.VanishPermissionRegistry


private val validNameRegex = Regex("^[a-zA-Z0-9_]{3,16}$")

fun nickCommand() = commandTree("nick") {
    withPermission(VanishPermissionRegistry.NICK_COMMAND)

    literalArgument("#unnick") {
        playerExecutor { player, _ ->
            if (!NickService.isNicked(player)) {
                player.sendText {
                    appendErrorPrefix()
                    error("Du bist nicht genickt.")
                }
                return@playerExecutor
            }

            NickService.unnick(player)
        }
    }

    stringArgument("nick") {
        playerExecutorSuspend { player, arguments ->
            val nick: String by arguments

            if (!validNameRegex.matches(nick)) {
                player.sendText {
                    appendErrorPrefix()
                    error("Der Nickname ist ungültig. Er muss 3-16 Zeichen lang sein und darf nur Buchstaben, Zahlen und Unterstriche enthalten.")
                }
                return@playerExecutorSuspend
            }

            NickService.nick(player, nick)

            player.sendText {
                appendSuccessPrefix()
                success("Du bist nun ")
                variableValue(nick)
                success(".")
            }
        }
    }
}