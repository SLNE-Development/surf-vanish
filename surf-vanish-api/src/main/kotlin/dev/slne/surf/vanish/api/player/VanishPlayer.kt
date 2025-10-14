package dev.slne.surf.vanish.api.player

interface VanishPlayer : VanishOfflinePlayer {
    val name: String

    fun vanish()
    fun reappear()

    val currentTarget: VanishOfflinePlayer?
}