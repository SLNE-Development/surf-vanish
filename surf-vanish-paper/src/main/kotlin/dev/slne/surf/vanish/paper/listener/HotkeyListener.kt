package dev.slne.surf.vanish.paper.listener

import com.github.retrooper.packetevents.event.PacketListenerAbstract
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.protocol.player.DiggingAction
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerInput
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import dev.slne.surf.vanish.core.service.vanishService
import dev.slne.surf.vanish.paper.util.bukkitPlayer
import dev.slne.surf.vanish.paper.util.vanishPlayer
import org.bukkit.entity.Player
import java.util.*

object HotkeyListener : PacketListenerAbstract() {
    private val _lastSneaks = mutableObject2ObjectMapOf<UUID, Long>()

    override fun onPacketReceive(event: PacketReceiveEvent) {
        val player = event.getPlayer<Player>() ?: return
        val vanishPlayer = player.vanishPlayer

        if (!vanishService.isSpectating(player.uniqueId)) {
            return
        }

        when (event.packetType) {
            PacketType.Play.Client.PLAYER_INPUT -> {
                val packet = WrapperPlayClientPlayerInput(event)

                if (packet.isShift) {
                    val lastSneak = _lastSneaks[player.uniqueId]
                    val now = System.currentTimeMillis()

                    if (lastSneak != null && now - lastSneak < 500) {
                        val current = vanishService.current(vanishPlayer) ?: run {
                            player.sendText {
                                appendErrorPrefix()
                                error("Du schaust gerade niemandem zu.")
                            }
                            return
                        }

                        val vanishTarget = current.bukkitPlayer ?: run {
                            player.sendText {
                                appendErrorPrefix()
                                error("Der Spieler, dem du zuschaust, ist nicht mehr online.")
                            }
                            return
                        }

                        player.teleportAsync(vanishTarget.location)
                        player.sendText {
                            appendSuccessPrefix()
                            success("Du bist nun wieder bei")
                            appendSpace()
                            variableValue(vanishTarget.name)
                            success(".")

                        }
                    } else {
                        _lastSneaks[player.uniqueId] = now
                    }
                }
            }

            PacketType.Play.Client.PLAYER_DIGGING -> {
                val packet = WrapperPlayClientPlayerDigging(event)

                if (packet.action == DiggingAction.SWAP_ITEM_WITH_OFFHAND) {
                    val sneakCacheResult = _lastSneaks[player.uniqueId]

                    if (sneakCacheResult != null && System.currentTimeMillis() - sneakCacheResult < 1000) {
                        val previous = vanishService.previous(vanishPlayer) ?: run {
                            player.sendText {
                                appendErrorPrefix()
                                error("Es wurde kein Spieler gefunden, den du zuvor angeschaut hast.")
                            }
                            return
                        }

                        val vanishTarget = previous.bukkitPlayer ?: run {
                            player.sendText {
                                appendErrorPrefix()
                                error("Der Spieler, dem du zuschauen möchtest, ist nicht mehr online.")
                            }
                            return
                        }

                        player.teleportAsync(vanishTarget.location)
                        player.sendText {
                            appendSuccessPrefix()
                            success("Du schaust nun wieder")
                            appendSpace()
                            variableValue(vanishTarget.name)
                            appendSpace()
                            success("zu.")
                        }
                        return
                    }

                    val next = vanishService.next(vanishPlayer) ?: run {
                        player.sendText {
                            appendErrorPrefix()
                            error("Es wurde kein weiterer Spieler gefunden, dem du zuschauen könntest.")
                        }
                        return
                    }

                    val vanishTarget = next.bukkitPlayer ?: run {
                        player.sendText {
                            appendErrorPrefix()
                            error("Der Spieler, dem du zuschauen möchtest, ist nicht mehr online.")
                        }
                        return
                    }

                    player.teleportAsync(vanishTarget.location)
                    player.sendText {
                        appendSuccessPrefix()
                        success("Du schaust nun")
                        appendSpace()
                        variableValue(vanishTarget.name)
                        appendSpace()
                        success("zu.")
                    }
                }
            }
        }
    }
}