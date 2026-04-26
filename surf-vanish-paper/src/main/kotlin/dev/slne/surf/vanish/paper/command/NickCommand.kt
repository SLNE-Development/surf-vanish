package dev.slne.surf.vanish.paper.command

import dev.jorel.commandapi.kotlindsl.*
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.vanish.core.service.NickService
import dev.slne.surf.vanish.paper.util.VanishPermissionRegistry

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

            if (nick.length !in 3..16) {
                player.sendText {
                    appendErrorPrefix()
                    error("Der Nickname muss zwischen 3 und 16 Zeichen lang sein.")
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