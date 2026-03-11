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
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import dev.slne.surf.surfapi.core.api.util.toObjectList
import dev.slne.surf.surfapi.core.api.util.toObjectSet
import dev.slne.surf.tab.api.redis.TabEntryUpdateRedisEvent
import dev.slne.surf.vanish.api.player.VanishOfflinePlayer
import dev.slne.surf.vanish.api.player.VanishPlayer
import dev.slne.surf.vanish.api.redis.VanishStateUpdateRedisEvent
import dev.slne.surf.vanish.core.service.VanishService
import dev.slne.surf.vanish.core.service.vanishPlayerService
import dev.slne.surf.vanish.core.service.vanishService
import dev.slne.surf.vanish.paper.config
import dev.slne.surf.vanish.paper.config.VanishConfiguration
import dev.slne.surf.vanish.paper.hook.MiniPlaceholdersHook
import dev.slne.surf.vanish.paper.plugin
import dev.slne.surf.vanish.paper.redisApi
import dev.slne.surf.vanish.paper.redisLoader
import dev.slne.surf.vanish.paper.util.*
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.util.Services
import org.bukkit.Bukkit
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@OptIn(ObsoleteScoreboardApi::class)
@AutoService(VanishService::class)
class VanishServiceImpl : VanishService, Services.Fallback {
    private val _vanishedPlayers = mutableObjectSetOf<UUID>()
    private val _playerQueues = mutableObject2ObjectMapOf<UUID, AuditableQueue>()
    private val _scoreboards = mutableObject2ObjectMapOf<UUID, SurfScoreboard>()
    private val _spectateModePlayers = mutableObjectSetOf<UUID>()
    private val _playerFlyStates = mutableObject2ObjectMapOf<UUID, Boolean>()

    override fun vanish(player: VanishPlayer) {
        _vanishedPlayers.add(player.uuid)
        _playerQueues[player.uuid] = AuditableQueue()

        markVanished(player.uuid)

        if (isSpectating(player.uuid)) {
            createAndShowScoreboard(player)
        }

        val vanishingPlayerPriority = player.bukkitPlayer.getVanishPriority()

        Bukkit.getOnlinePlayers()
            .filterNot { it.uniqueId == player.uuid }.forEach { onlinePlayer ->
                if (!onlinePlayer.hasPermission(VanishPermissionRegistry.VANISH_BYPASS)) {
                    if (onlinePlayer.getVanishPriority() < vanishingPlayerPriority) {
                        onlinePlayer.hidePlayer(plugin, player.bukkitPlayer)

                        if (config.spoofConnectionMessages) {
                            onlinePlayer.sendText {
                                append(
                                    MiniPlaceholdersHook.parse(
                                        player.bukkitPlayer,
                                        config.fakeDisconnectMessage
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
                } else {
                    onlinePlayer.sendText {
                        appendInfoPrefix()
                        variableValue(player.name)
                        info(" ist nun unsichtbar.")
                    }
                }
            }
    }

    override fun reappear(player: VanishPlayer) {
        current(player)?.bukkitPlayer?.let { currentPlayer ->
            glowingApi.removeGlowing(currentPlayer, player.bukkitPlayer)
        }

        markReappeared(player.uuid)

        _vanishedPlayers.remove(player.uuid)
        _playerQueues.remove(player.uuid)

        hideAndDeleteScoreboard(player)

        val reappearingPlayerPriority = player.bukkitPlayer.getVanishPriority()

        Bukkit.getOnlinePlayers()
            .filterNot { it.uniqueId == player.uuid }.forEach { onlinePlayer ->
                if (!onlinePlayer.hasPermission(VanishPermissionRegistry.VANISH_BYPASS)) {
                    if (onlinePlayer.getVanishPriority() < reappearingPlayerPriority) {
                        onlinePlayer.showPlayer(plugin, player.bukkitPlayer)

                        redisApi.publishEvent(
                            TabEntryUpdateRedisEvent(
                                player.uuid
                            )
                        )

                        if (config.spoofConnectionMessages) {
                            onlinePlayer.sendText {
                                append(
                                    MiniPlaceholdersHook.parse(
                                        player.bukkitPlayer,
                                        config.fakeConnectMessage
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
                } else {
                    onlinePlayer.sendText {
                        appendInfoPrefix()
                        variableValue(player.name)
                        info(" ist nun sichtbar.")
                    }
                }
            }
    }

    override fun isVanished(player: VanishOfflinePlayer) = _vanishedPlayers.contains(player.uuid)
    override fun all(): ObjectSet<VanishOfflinePlayer> =
        _vanishedPlayers.map { vanishPlayerService.getOfflinePlayer(it) }.toObjectSet()

    override fun allOnline(): ObjectSet<VanishPlayer> =
        all().mapNotNull { it.bukkitPlayer?.vanishPlayer }.toObjectSet()

    override fun previous(player: VanishOfflinePlayer): VanishOfflinePlayer? {
        return _playerQueues[player.uuid]?.back()?.let {
            vanishPlayerService.getOfflinePlayer(it)
        }
    }

    override fun next(player: VanishOfflinePlayer): VanishOfflinePlayer? {
        current(player)?.bukkitPlayer?.let { currentPlayer ->
            player.bukkitPlayer?.let { self ->
                glowingApi.removeGlowing(currentPlayer, self)
            }
        }

        val spectatorPriority = player.bukkitPlayer?.getVanishPriority() ?: 0

        val next = _playerQueues[player.uuid]?.next(Bukkit.getOnlinePlayers().filterNot {
            it.hasPermission(VanishPermissionRegistry.VANISH_BYPASS) ||
                    it.getVanishPriority() >= spectatorPriority
        }.map { it.uniqueId }.toObjectList())?.let {
            vanishPlayerService.getOfflinePlayer(it)
        }

        next?.bukkitPlayer?.let { nextPlayer ->
            player.bukkitPlayer?.let { self ->
                glowingApi.makeGlowing(nextPlayer, self, VanishConfiguration.GLOW_COLOR)
            }
        }

        return next
    }

    override fun current(player: VanishOfflinePlayer) =
        _playerQueues[player.uuid]?.current?.let {
            vanishPlayerService.getOfflinePlayer(it)
        }

    override fun createAndShowScoreboard(player: VanishPlayer) {
        _scoreboards[player.uuid] = surfBukkitApi.createScoreboard(buildText {
            primary("    SpectateMode    ", TextDecoration.BOLD)
        })
            .addLine(buildText {
                info("Spieler")
            })
            .addUpdatableLine {
                buildText {
                    spacer(
                        player.currentTarget?.bukkitPlayer?.name?.toSmallCaps()
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
                    spacer("${player.currentTarget?.bukkitPlayer?.health?.toInt() ?: "Unbekannt"}".toSmallCaps() + "/" + "${player.currentTarget?.bukkitPlayer?.healthScale?.toInt() ?: "Unbekannt"}".toSmallCaps())
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
                            player.currentTarget?.bukkitPlayer?.location?.distance(
                                player.bukkitPlayer.location
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
                        "${player.currentTarget?.bukkitPlayer?.ping ?: "Unbekannt "}ms".toSmallCaps()
                    )
                }
            }
            .buildAutoUpdatable()

        _scoreboards[player.uuid]?.enable()
        _scoreboards[player.uuid]?.addViewer(player.bukkitPlayer)
    }

    override fun hideAndDeleteScoreboard(player: VanishPlayer) {
        _scoreboards[player.uuid]?.disable()
        _scoreboards.remove(player.uuid)
    }

    override fun setFlyState(uuid: UUID, canFly: Boolean) {
        _playerFlyStates[uuid] = canFly
    }

    override fun getFlyState(uuid: UUID): Boolean {
        return _playerFlyStates[uuid] ?: false
    }

    override fun isSpectating(playerUuid: UUID) = _spectateModePlayers.contains(playerUuid)

    override fun startSpectateMode(player: VanishPlayer) {
        _spectateModePlayers.add(player.uuid)

        createAndShowScoreboard(player)

        player.bukkitPlayer.sendText {
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

    override fun stopSpectateMode(player: VanishPlayer) {
        _spectateModePlayers.remove(player.uuid)

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
                    it.bukkitPlayer.sendActionBar(buildText {
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