package me.ryguy.ctfbot.modules.poll;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import me.ryguy.ctfbot.CTFDiscordBot;
import me.ryguy.ctfbot.module.ModuleListener;
import me.ryguy.ctfbot.module.Modules;
import me.ryguy.discordapi.listeners.DiscordEvent;
import me.ryguy.discordapi.listeners.Listener;

@ModuleListener(module = Modules.POLLS)
public class PollListeners implements Listener {
    @DiscordEvent
    public void onReactAdd(ReactionAddEvent event) {
        if (Poll.getPoll(event.getMessage().block()) == null) {
            return;
        }
        if (event.getUser().block().isBot()) {
            return;
        }
        if (!event.getEmoji().asUnicodeEmoji().isPresent()) {
            return;
        }

        Poll e = Poll.getPoll(event.getMessage().block());
        boolean shouldHandle = false;
        for (Poll.Option o : e.getOptions()) {
            if (event.getEmoji().asUnicodeEmoji().get().getRaw().equalsIgnoreCase(o.getEmoji())) {
                if (o.getPlayers().contains(event.getUser().block())) return;
                o.addPlayer(event.getUser().block());
                shouldHandle = true;
                if (!e.isShowVotes()) {
                    e.getMessage().removeReaction(event.getEmoji(), event.getUserId()).block();
                }
            }
        }
        if (shouldHandle) {
            e.handleReaction();
        }
        CTFDiscordBot.save();
    }

    @DiscordEvent
    public void onReactRemove(ReactionRemoveEvent event) {
        if (Poll.getPoll(event.getMessage().block()) == null) {
            return;
        }
        if (event.getUser().block().isBot()) {
            return;
        }
        if (!event.getEmoji().asUnicodeEmoji().isPresent()) {
            return;
        }

        Poll e = Poll.getPoll(event.getMessage().block());
        boolean shouldHandle = false;
        for (Poll.Option o : e.getOptions()) {
            if (event.getEmoji().asUnicodeEmoji().get().getRaw().equalsIgnoreCase(o.getEmoji())) {
                if (o.getPlayers().contains(event.getUser().block())) return;
                o.removePlayer(event.getUser().block());
                shouldHandle = true;
            }
        }
        if (shouldHandle) {
            e.handleReaction();
        }
        CTFDiscordBot.save();
    }
}
