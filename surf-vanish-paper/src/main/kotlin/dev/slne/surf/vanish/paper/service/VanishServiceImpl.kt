package dev.slne.surf.vanish.paper.service

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.minimessage.miniMessage
import dev.slne.surf.api.core.util.toObjectList
import dev.slne.surf.api.core.util.toObjectSet
import dev.slne.surf.api.paper.glow.SurfGlowingApi
import dev.slne.surf.api.paper.scoreboard.SurfScoreboard
import dev.slne.surf.api.paper.scoreboard.SurfScoreboardApi
import dev.slne.surf.vanish.api.event.PlayerReappearEvent
import dev.slne.surf.vanish.api.event.PlayerVanishEvent
import dev.slne.surf.vanish.core.redisLoader
import dev.slne.surf.vanish.core.service.VanishService
import dev.slne.surf.vanish.core.service.vanishService
import dev.slne.surf.vanish.paper.config
import dev.slne.surf.vanish.paper.config.VanishConfiguration
import dev.slne.surf.vanish.paper.hook.LuckPermsHook
import dev.slne.surf.vanish.paper.plugin
import dev.slne.surf.vanish.paper.util.*
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.util.Services
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

@AutoService(VanishService::class)
class VanishServiceImpl : VanishService, Services.Fallback {
    private val _playerQueues = ConcurrentHashMap<UUID, AuditableQueue>()
    private val _scoreboards = ConcurrentHashMap<UUID, SurfScoreboard>()
    private val _spectateModePlayers = ConcurrentHashMap.newKeySet<UUID>()
    private val _playerFlyStates = ConcurrentHashMap<UUID, Boolean>()

    override fun vanish(player: Player) {
        if (redisLoader.vanishedPlayers.add(player.uniqueId).not()) {
            return
        }

        _playerQueues[player.uniqueId] = AuditableQueue()

        player.setMetaVanished(true)

        if (isSpectating(player.uniqueId)) {
            createAndShowScoreboard(player)
        }

        val prefix = LuckPermsHook.getPrefix(player)

        val spoofMessage =
            if (config.spoofConnectionMessages) {
                miniMessage.deserialize(
                    "<dark_gray>[<red>-<dark_gray>] ${prefix}${player.name}"
                )
            } else {
                null
            }

        val visibleMessage = buildText {
            appendInfoPrefix()
            variableValue(player.name)
            info(" ist nun unsichtbar.")
        }

        Bukkit.getOnlinePlayers().forEach { onlinePlayer ->
            if (onlinePlayer.uniqueId == player.uniqueId) {
                return@forEach
            }

            if (!onlinePlayer.canVanishSee(player)) {
                if (onlinePlayer.canSee(player)) {
                    onlinePlayer.hidePlayer(plugin, player)
                }

                spoofMessage?.let(onlinePlayer::sendMessage)
            } else {
                onlinePlayer.sendMessage(visibleMessage)
            }
        }

        PlayerVanishEvent(player.uniqueId).callEvent()
    }

    override fun reappear(player: Player) {
        if (redisLoader.vanishedPlayers.remove(player.uniqueId).not()) {
            return
        }

        current(player)?.player?.let { currentPlayer ->
            SurfGlowingApi.removeGlowing(currentPlayer, player)
        }

        player.sendActionBar(Component.empty())
        player.setMetaVanished(false)

        _playerQueues.remove(player.uniqueId)

        hideAndDeleteScoreboard(player)

        val prefix = LuckPermsHook.getPrefix(player)

        val spoofMessage =
            if (config.spoofConnectionMessages) {
                miniMessage.deserialize(
                    "<dark_gray>[<green>+<dark_gray>] ${prefix}${player.name}"
                )
            } else {
                null
            }

        val visibleMessage = buildText {
            appendInfoPrefix()
            variableValue(player.name)
            info(" ist nun sichtbar.")
        }

        Bukkit.getOnlinePlayers().forEach { onlinePlayer ->
            if (onlinePlayer.uniqueId == player.uniqueId) {
                return@forEach
            }

            if (!onlinePlayer.canVanishSee(player)) {
                if (!onlinePlayer.canSee(player)) {
                    onlinePlayer.showPlayer(plugin, player)
                }

                spoofMessage?.let(onlinePlayer::sendMessage)
            } else {
                onlinePlayer.sendMessage(visibleMessage)
            }
        }

        PlayerReappearEvent(player.uniqueId).callEvent()
    }

    override fun isVanished(playerUuid: UUID) = redisLoader.vanishedPlayers.contains(playerUuid)
    override fun all(): ObjectSet<OfflinePlayer> =
        redisLoader.vanishedPlayers.snapshot().map { Bukkit.getOfflinePlayer(it) }.toObjectSet()

    override fun allOnline(): ObjectSet<Player> =
        redisLoader.vanishedPlayers.snapshot().mapNotNull { Bukkit.getPlayer(it) }.toObjectSet()

    override fun previous(player: Player): OfflinePlayer? {
        return _playerQueues[player.uniqueId]?.back()?.let {
            Bukkit.getOfflinePlayer(it)
        }
    }

    override fun next(player: Player): OfflinePlayer? {
        current(player)?.player?.let { currentPlayer ->
            SurfGlowingApi.removeGlowing(currentPlayer, player)
        }

        val next = _playerQueues[player.uniqueId]?.next(
            Bukkit.getOnlinePlayers()
                .filter { it.uniqueId != player.uniqueId && player.canVanishSee(it) }
                .map { it.uniqueId }
                .toObjectList()
        )?.let {
            Bukkit.getOfflinePlayer(it)
        }

        next?.player?.let { nextPlayer ->
            SurfGlowingApi.makeGlowing(nextPlayer, player, VanishConfiguration.GLOW_COLOR)
        }

        return next
    }

    override fun current(player: Player) =
        _playerQueues[player.uniqueId]?.current?.let {
            Bukkit.getOfflinePlayer(it)
        }

    override fun createAndShowScoreboard(player: Player) {
        _scoreboards[player.uniqueId] = SurfScoreboardApi.createScoreboard(buildText {
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
                            player.currentTarget?.player?.location?.distance(
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