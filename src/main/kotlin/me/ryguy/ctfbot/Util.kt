package me.ryguy.ctfbot

import discord4j.core.`object`.entity.Member
import discord4j.core.`object`.entity.Message
import discord4j.rest.util.Color
import org.jsoup.Jsoup
import java.net.URL

fun Member.isPpmHost(): Boolean {
    return this.roles.map { it.name }.collectList().block()?.contains("PPM Host") ?: false
}

fun URL.parseHtml(): org.jsoup.nodes.Document? {
    return this.readText()
            .let { Jsoup.parse(it) }
}

fun Message?.replyWithFailure(msg: String) {
    this?.channel?.block()?.createMessage { m ->
        m.setEmbed {
            it.setColor(Color.RED)
            it.setDescription(msg)
        }
    }
}

fun Message?.replyWithSuccess(msg: String) {
    this?.channel?.block()?.createMessage { m ->
        m.setEmbed {
            it.setColor(Color.TAHITI_GOLD)
            it.setDescription(msg)
        }
    }
}