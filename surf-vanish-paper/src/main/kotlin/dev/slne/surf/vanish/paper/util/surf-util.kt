package dev.slne.surf.vanish.paper.util

import dev.slne.surf.surfapi.core.api.messages.builder.SurfComponentBuilder
import net.kyori.adventure.text.minimessage.MiniMessage

fun SurfComponentBuilder.displayKey(key: String) =
    append(MiniMessage.miniMessage().deserialize("<key:key.$key>"))