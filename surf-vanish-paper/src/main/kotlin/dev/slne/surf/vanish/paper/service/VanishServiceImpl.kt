package dev.slne.surf.vanish.paper.service

import com.google.auto.service.AutoService
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.surfapi.bukkit.api.glow.glowingApi
import dev.slne.surf.surfapi.bukkit.api.scoreboard.ObsoleteScoreboardApi
import dev.slne.surf.surfapi.bukkit.api.scoreboard.SurfScoreboard
import dev.slne.surf.surfapi.bukkit.api.surfBukkitApi
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.minimessage.miniMessage
import dev.slne.surf.surfapi.core.api.util.toObjectList
import dev.slne.surf.surfapi.core.api.util.toObjectSet
import dev.slne.surf.tab.api.redis.TabEntryUpdateRedisEvent
import dev.slne.surf.vanish.api.redis.VanishStateUpdateRedisEvent
import dev.slne.surf.vanish.core.service.VanishService
import dev.slne.surf.vanish.core.service.vanishService
import dev.slne.surf.vanish.paper.config
import dev.slne.surf.vanish.paper.config.VanishConfiguration
import dev.slne.surf.vanish.paper.hook.LuckPermsHook
import dev.slne.surf.vanish.paper.plugin
import dev.slne.surf.vanish.paper.redisApi
import dev.slne.surf.vanish.paper.redisLoader
import dev.slne.surf.vanish.paper.util.AuditableQueue
import dev.slne.surf.vanish.paper.util.canVanishSee
import dev.slne.surf.vanish.paper.util.currentTarget
import dev.slne.surf.vanish.paper.util.displayKey
import io.ktor.util.collections.*
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.util.Services
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@OptIn(ObsoleteScoreboardApi::class)
@AutoService(VanishService::class)
class VanishServiceImpl : VanishService, Services.Fallback {
    private val _vanishedPlayers = ConcurrentSet<UUID>()
    private val _playerQueues = ConcurrentHashMap<UUID, AuditableQueue>()
    private val _scoreboards = ConcurrentHashMap<UUID, SurfScoreboard>()
    private val _spectateModePlayers = ConcurrentSet<UUID>()
    private val _playerFlyStates = ConcurrentHashMap<UUID, Boolean>()

    override fun vanish(player: Player) {
        _vanishedPlayers.add(player.uniqueId)
        _playerQueues[player.uniqueId] = AuditableQueue()

        markVanished(player.uniqueId)

        if (isSpectating(player.uniqueId)) {
            createAndShowScoreboard(player)
        }

        Bukkit.getOnlinePlayers()
            .filterNot { it.uniqueId == player.uniqueId }
            .forEach { onlinePlayer ->

                if (!onlinePlayer.canVanishSee(player)) {
                    onlinePlayer.hidePlayer(plugin, player)

                    if (config.spoofConnectionMessages) {
                        onlinePlayer.sendText {
                            append(
                                miniMessage.deserialize(
                                    "<gray>[<red>-<gray>] ${
                                        LuckPermsHook.getPrefix(
                                            player
                                        )
                                    } ${player.name}"
                                )
                            )
                        }
                    }
                } else {
                    onlinePlayer.sendText {
                        appendInfoPrefix()
                        variableValue(player.name)
                        info(" ist nun unsichtbar.")
                    }
                }
            }
    }

    override fun reappear(player: Player) {
        current(player)?.player?.let { currentPlayer ->
            glowingApi.removeGlowing(currentPlayer, player)
        }

        markReappeared(player.uniqueId)

        _vanishedPlayers.remove(player.uniqueId)
        _playerQueues.remove(player.uniqueId)

        hideAndDeleteScoreboard(player)

        Bukkit.getOnlinePlayers()
            .filterNot { it.uniqueId == player.uniqueId }
            .forEach { onlinePlayer ->

                if (!onlinePlayer.canVanishSee(player)) {
                    onlinePlayer.showPlayer(plugin, player)

                    redisApi.publishEvent(
                        TabEntryUpdateRedisEvent(player.uniqueId)
                    )

                    if (config.spoofConnectionMessages) {
                        onlinePlayer.sendText {
                            append(
                                miniMessage.deserialize(
                                    "<gray>[<green>+<gray>] ${
                                        LuckPermsHook.getPrefix(
                                            player
                                        )
                                    } ${player.name}"
                                )
                            )
                        }
                    }
                } else {
                    onlinePlayer.sendText {
                        appendInfoPrefix()
                        variableValue(player.name)
                        info(" ist nun sichtbar.")
                    }
                }
            }
    }

    override fun isVanished(player: OfflinePlayer) = _vanishedPlayers.contains(player.uniqueId)
    override fun all(): ObjectSet<OfflinePlayer> =
        _vanishedPlayers.map { Bukkit.getOfflinePlayer(it) }.toObjectSet()

    override fun allOnline(): ObjectSet<Player> =
        all().mapNotNull { it.player }.toObjectSet()

    override fun previous(player: Player): OfflinePlayer? {
        return _playerQueues[player.uniqueId]?.back()?.let {
            Bukkit.getOfflinePlayer(it)
        }
    }

    override fun next(player: Player): OfflinePlayer? {
        current(player)?.player?.let { currentPlayer ->
            glowingApi.removeGlowing(currentPlayer, player)
        }

        val next = _playerQueues[player.uniqueId]?.next(
            Bukkit.getOnlinePlayers()
                .filter { player.canVanishSee(it) }
                .map { it.uniqueId }
                .toObjectList()
        )?.let {
            Bukkit.getOfflinePlayer(it)
        }

        next?.player?.let { nextPlayer ->
            glowingApi.makeGlowing(nextPlayer, player, VanishConfiguration.GLOW_COLOR)
        }

        return next
    }

    override fun current(player: Player) =
        _playerQueues[player.uniqueId]?.current?.let {
            Bukkit.getOfflinePlayer(it)
        }

    override fun createAndShowScoreboard(player: Player) {
        _scoreboards[player.uniqueId] = surfBukkitApi.createScoreboard(buildText {
            primary("    SpectateMode    ", TextDecoration.BOLD)
        })
            .addLine(buildText {
                info("Spieler")
            })
            .addUpdatableLine {
                buildText {
                    spacer(
                        player.currentTarget?.player?.name?.toSmallCaps()
                            ?: "Kein Spieler".toSmallCaps()
                    )
                }
            }
            .addEmptyLine()
            .addLine(buildText {
                info("Leben")
            })
            .addUpdatableLine {
                buildText {
                    spacer("${player.currentTarget?.player?.health?.toInt() ?: "Unbekannt"}".toSmallCaps() + "/" + "${player.currentTarget?.player?.healthScale?.toInt() ?: "Unbekannt"}".toSmallCaps())
                }
            }
            .addEmptyLine()
            .addLine(buildText {
                info("Entfernung")
            })
            .addUpdatableLine {
                buildText {
                    spacer(
                        "${
                            player.currentTarget?.player?.location?.distanceSquared(
                                player.location
                            )?.toInt() ?: "Unbekannt"
                        } Blöcke".toSmallCaps()
                    )
                }
            }
            .addEmptyLine()
            .addLine(buildText {
                info("Ping")
            })
            .addUpdatableLine {
                buildText {
                    spacer(
                        "${player.currentTarget?.player?.ping ?: "Unbekannt "}ms".toSmallCaps()
                    )
                }
            }
            .buildAutoUpdatable()

        _scoreboards[player.uniqueId]?.enable()
        _scoreboards[player.uniqueId]?.addViewer(player)
    }

    override fun hideAndDeleteScoreboard(player: Player) {
        _scoreboards[player.uniqueId]?.disable()
        _scoreboards.remove(player.uniqueId)
    }

    override fun setFlyState(uuid: UUID, canFly: Boolean) {
        _playerFlyStates[uuid] = canFly
    }

    override fun getFlyState(uuid: UUID): Boolean {
        return _playerFlyStates[uuid] ?: false
    }

    override fun isSpectating(playerUuid: UUID) = _spectateModePlayers.contains(playerUuid)

    override fun startSpectateMode(player: Player) {
        _spectateModePlayers.add(player.uniqueId)

        createAndShowScoreboard(player)

        player.sendText {
            appendNewInfoPrefixedLine()
            darkSpacer("-".repeat(25))

            appendNewInfoPrefixedLine()
            spacer("Spectate-Mode Steuerung:".toSmallCaps())

            appendNewInfoPrefixedLine()
            appendNewInfoPrefixedLine()
            note("Zurück: ")
            displayKey("sneak")
            spacer(" + ")
            displayKey("swapOffhand")

            appendNewInfoPrefixedLine()
            note("Weiter: ")
            displayKey("swapOffhand")

            appendNewInfoPrefixedLine()
            note("Teleport: ")
            white("2x ")
            displayKey("sneak")

            appendNewInfoPrefixedLine()

            appendNewInfoPrefixedLine()
            darkSpacer("-".repeat(25))
        }
    }

    override fun stopSpectateMode(player: Player) {
        _spectateModePlayers.remove(player.uniqueId)

        hideAndDeleteScoreboard(player)
    }

    companion object {
        private lateinit var actionbarTask: ScheduledTask

        fun startTask() {
            if (::actionbarTask.isInitialized && !actionbarTask.isCancelled) {
                return
            }

            actionbarTask = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, {
                vanishService.allOnline().forEach {
                    it.sendActionBar(buildText {
                        note("Du bist für andere Spieler unsichtbar.")
                    })
                }
            }, 0L, 1, TimeUnit.SECONDS)
        }

        fun stopTask() {
            if (::actionbarTask.isInitialized && !actionbarTask.isCancelled) {
                actionbarTask.cancel()
            }
        }
    }
}

fun markVanished(player: UUID) {
    redisLoader.vanishedPlayers.put(
        SurfServer.current().name,
        (redisLoader.vanishedPlayers[SurfServer.current().name] ?: mutableListOf()) + player
    )
    redisApi.publishEvent(VanishStateUpdateRedisEvent(player, true))
}

fun markReappeared(player: UUID) {
    redisLoader.vanishedPlayers.put(
        SurfServer.current().name,
        (redisLoader.vanishedPlayers[SurfServer.current().name]
            ?: mutableListOf()).filterNot { it == player }
    )
    redisApi.publishEvent(VanishStateUpdateRedisEvent(player, false))
}