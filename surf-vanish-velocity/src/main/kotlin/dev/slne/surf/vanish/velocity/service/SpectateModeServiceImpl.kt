package dev.slne.surf.vanish.velocity.service

import com.google.auto.service.AutoService

import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import dev.slne.surf.vanish.core.service.SpectateModeService
import dev.slne.surf.vanish.core.service.util.PluginMessageChannels
import dev.slne.surf.vanish.core.service.util.VanishPermissionRegistry
import dev.slne.surf.vanish.velocity.plugin
import dev.slne.surf.vanish.velocity.util.displayKey
import dev.slne.surf.vanish.velocity.util.toPluginChannel

import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.text.Component
import net.kyori.adventure.util.Services

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

import kotlin.jvm.optionals.getOrNull

@AutoService(SpectateModeService::class)
class SpectateModeServiceImpl : SpectateModeService, Services.Fallback {
    val spectateModePlayers = mutableObjectSetOf<UUID>()
    val previousPlayers = mutableObject2ObjectMapOf<UUID, ArrayDeque<UUID>>()
    val currentSpectating = mutableObject2ObjectMapOf<UUID, UUID>()

    override fun startJob() {
        plugin.proxy.scheduler.buildTask(plugin, Runnable {
            for (uuid in this.spectateModePlayers) {
                val player = plugin.proxy.getPlayer(uuid).getOrNull() ?: continue
                val server = player.currentServer.getOrNull() ?: continue
                val cachedCurrentlySpectating = this.currentSpectating[uuid]
                val currentSpectatingPlayer = when(cachedCurrentlySpectating == null) {
                    true -> null
                    false -> plugin.proxy.getPlayer(cachedCurrentlySpectating).getOrNull()
                }

                player.sendActionBar {
                    buildText {
                        primary("Zurück: ".toSmallCaps())
                        displayKey("swapOffhand")
                        spacer(" + ")
                        displayKey("sneak")
                        spacer(" - ")

                        info(currentSpectatingPlayer?.username ?: "Kein Spieler ausgewählt")
                        spacer(" | ")
                        info(currentSpectatingPlayer?.currentServer?.getOrNull()?.serverInfo?.name ?: "Server unbekannt")
                        spacer(" | ")
                        info(currentSpectatingPlayer?.clientBrand ?: "Client unbekannt")

                        spacer(" - ")
                        primary("Weiter: ".toSmallCaps())
                        displayKey("swapOffhand")
                    }
                }
            }
        }).repeat(20L, TimeUnit.MILLISECONDS).schedule()
    }

    override fun isSpectating(uuid: UUID): Boolean {
        return spectateModePlayers.contains(uuid)
    }

    override fun setSpectating(uuid: UUID, spectating: Boolean) {
        if (spectating) {
            spectateModePlayers.add(uuid)
        } else {
            spectateModePlayers.remove(uuid)
            currentSpectating[uuid] = null
            previousPlayers[uuid] = null

            val player = plugin.proxy.getPlayer(uuid).getOrNull() ?: return

            player.sendActionBar(Component.empty())
        }
    }

    override fun getSpectators(): ObjectSet<UUID> {
        return spectateModePlayers
    }

    override fun hasSpectator(uuid: UUID): Boolean {
        return currentSpectating.any { it.value == uuid }
    }

    override fun nextPlayer(uuid: UUID): UUID? {
        val player = plugin.proxy.getPlayer(uuid).getOrNull() ?: return null
        val playerServerName = player.currentServer.getOrNull()?.serverInfo?.name ?: return null

        val candidates = plugin.proxy.allPlayers
            .filter { it.uniqueId != uuid }
            .filter { it.currentServer.getOrNull()?.serverInfo?.name == playerServerName }
            .filter { !it.hasPermission(VanishPermissionRegistry.SPECTATE_MODE_BYPASS) }

        if (candidates.isEmpty()) {
            player.sendMessage {
                buildText {
                    appendPrefix()
                    error("Es wurden keine weiteren Spieler gefunden, die du beobachten kannst.".toSmallCaps())
                }
            }
            return null
        }

        val current = currentSpectating[uuid]
        val next = if (current == null) {
            candidates.randomOrNull()
        } else {
            candidates
                .filter { it.uniqueId != current }
                .randomOrNull()
        }

        if (next == null) {
            player.sendMessage {
                buildText {
                    appendPrefix()
                    error("Es wurden keine weiteren Spieler gefunden, die du beobachten kannst.".toSmallCaps())
                }
            }
            return null
        }

        currentSpectating[uuid] = next.uniqueId
        previousPlayers.computeIfAbsent(uuid) { ArrayDeque() }.addLast(next.uniqueId)
        pushTeleport(uuid, next.uniqueId)
        current?.let {
            pushGlow(player.uniqueId, current, false)
        }
        pushGlow(uuid, next.uniqueId, true)

        return next.uniqueId
    }


    override fun previousPlayer(uuid: UUID): UUID? {
        val player = plugin.proxy.getPlayer(uuid).getOrNull() ?: return null
        val history = previousPlayers[uuid]

        if(history == null || history.isEmpty()) {
            player.sendMessage {
                buildText {
                    appendPrefix()
                    error("Es wurden keine weiteren Spieler gefunden, die du beobachten kannst.".toSmallCaps())
                }
            }
            return null
        }

        if (history.size < 2) {
            player.sendMessage {
                buildText {
                    appendPrefix()
                    error("Es wurde kein vorheriger Spieler gefunden, den du beobachten kannst.".toSmallCaps())
                }
            }
            return null
        }

        val current = currentSpectating[player.uniqueId]


        current?.let {
            pushGlow(player.uniqueId, current, false)
        }

        history.removeLast()
        val previousPlayer = history.removeLast()
        currentSpectating[player.uniqueId] = previousPlayer

        pushTeleport(player.uniqueId, previousPlayer)
        pushGlow(player.uniqueId, previousPlayer, true)

        return previousPlayer
    }

    fun pushTeleport(uuid: UUID, target: UUID) {
        val player = plugin.proxy.getPlayer(uuid).getOrNull() ?: return
        val server = player.currentServer.getOrNull() ?: return

        val outputStream = ByteArrayOutputStream()
        val dataOutput = DataOutputStream(outputStream)

        dataOutput.writeUTF(uuid.toString())
        dataOutput.writeUTF(target.toString())

        server.sendPluginMessage(PluginMessageChannels.SPECTATE_MODE_TELEPORTS.toPluginChannel(), outputStream.toByteArray())
    }

    fun pushGlow(uuid: UUID, target: UUID, glow: Boolean) {
        val player = plugin.proxy.getPlayer(uuid).getOrNull() ?: return
        val server = player.currentServer.getOrNull() ?: return

        val outputStream = ByteArrayOutputStream()
        val dataOutput = DataOutputStream(outputStream)

        dataOutput.writeUTF(uuid.toString())
        dataOutput.writeUTF(target.toString())
        dataOutput.writeBoolean(glow)

        server.sendPluginMessage(PluginMessageChannels.SPECTATE_MODE_GLOW.toPluginChannel(), outputStream.toByteArray())
    }
}