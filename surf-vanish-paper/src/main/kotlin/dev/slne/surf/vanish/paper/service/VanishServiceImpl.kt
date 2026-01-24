package dev.slne.surf.vanish.paper.service

import com.google.auto.service.AutoService
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
import dev.slne.surf.vanish.core.service.VanishService
import dev.slne.surf.vanish.core.service.vanishPlayerService
import dev.slne.surf.vanish.core.service.vanishService
import dev.slne.surf.vanish.paper.config
import dev.slne.surf.vanish.paper.config.VanishConfiguration
import dev.slne.surf.vanish.paper.hook.MiniPlaceholdersHook
import dev.slne.surf.vanish.paper.plugin
import dev.slne.surf.vanish.paper.redisApi
import dev.slne.surf.vanish.paper.util.*
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.util.Services
import org.bukkit.Bukkit
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ObsoleteScoreboardApi::class)
@AutoService(VanishService::class)
class VanishServiceImpl : VanishService, Services.Fallback {
    private val _vanishedPlayers = mutableObjectSetOf<UUID>()
    private val _playerQueues = mutableObject2ObjectMapOf<UUID, AuditableQueue>()
    private val _scoreboards = mutableObject2ObjectMapOf<UUID, SurfScoreboard>()

    override fun vanish(player: VanishPlayer) {
        _vanishedPlayers.add(player.uuid)
        _playerQueues[player.uuid] = AuditableQueue()

        createAndShowScoreboard(player)

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
                            ?: "Unbekannt".toSmallCaps()
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

    companion object {
        private lateinit var actionbarTask: ScheduledTask

        fun startTask() {
            if (::actionbarTask.isInitialized && !actionbarTask.isCancelled) {
                return
            }

            actionbarTask = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, {
                vanishService.allOnline().forEach {
                    it.bukkitPlayer.sendActionBar(buildText {
                        info("Zurück: ")
                        displayKey("sneak")
                        info(" + ")
                        displayKey("swapOffhand")
                        darkSpacer(" - ")
                        primary("Du bist unsichtbar!")
                        darkSpacer(" - ")
                        info(" Weiter: ")
                        displayKey("swapOffhand")
                        darkSpacer(" - ")
                        info("Teleport: 2x ")
                        displayKey("sneak")
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