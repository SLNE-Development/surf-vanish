package dev.slne.surf.vanish.paper.util

import it.unimi.dsi.fastutil.objects.ObjectList
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

data class AuditableQueue(
    private val history: CopyOnWriteArrayList<UUID> = CopyOnWriteArrayList(),
    private val currentIndex: AtomicInteger = AtomicInteger(-1)
) {
    val current: UUID?
        get() = history.getOrNull(currentIndex.get())

    fun next(source: ObjectList<UUID>): UUID? {
        if (source.isEmpty()) return null

        while (true) {
            val index = currentIndex.get()

            if (index < history.lastIndex) {
                if (currentIndex.compareAndSet(index, index + 1)) {
                    return history[index + 1]
                }
                continue
            }

            val next = generateNext(source, history.getOrNull(index))

            history.add(next)

            if (currentIndex.compareAndSet(index, index + 1)) {
                return next
            } else {
                history.removeAt(history.lastIndex)
            }
        }
    }

    private fun generateNext(source: ObjectList<UUID>, current: UUID?): UUID {
        if (source.size == 1) return source[0]
        var candidate: UUID
        do {
            candidate = source[Random.nextInt(source.size)]
        } while (candidate == current)
        return candidate
    }

    fun back(): UUID? {
        while (true) {
            val index = currentIndex.get()
            if (index <= 0) return history.getOrNull(index)

            if (currentIndex.compareAndSet(index, index - 1)) {
                return history[index - 1]
            }
        }
    }
}