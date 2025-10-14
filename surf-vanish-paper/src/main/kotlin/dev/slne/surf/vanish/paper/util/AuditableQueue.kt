package dev.slne.surf.vanish.paper.util

import it.unimi.dsi.fastutil.objects.ObjectList
import java.util.*

data class AuditableQueue(
    private var history: MutableList<UUID> = mutableListOf(),
    private var currentIndex: Int = -1
) {
    val current: UUID?
        get() = history.getOrNull(currentIndex)

    fun next(source: ObjectList<UUID>): UUID? {
        if (source.isEmpty()) {
            return null
        }

        if (currentIndex < history.lastIndex) {
            currentIndex++
            return history[currentIndex]
        }

        val next = generateNext(source)

        history.add(next)
        currentIndex++
        return next
    }

    private fun generateNext(source: ObjectList<UUID>): UUID {
        if (source.size == 1) return source[0]
        var candidate: UUID
        do {
            candidate = source.random()
        } while (candidate == current)
        return candidate
    }

    fun back(): UUID? {
        if (currentIndex > 0) {
            currentIndex--
            return history[currentIndex]
        }
        return current
    }
}
