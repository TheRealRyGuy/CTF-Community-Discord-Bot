package me.ryguy.ctfbot.util;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import me.ryguy.discordapi.DiscordBot;

public class DiscordUtil {
    public static String getUserTag(long id) {
        if(DiscordBot.getBot().getGateway().getUserById(Snowflake.of(id)).blockOptional().isPresent())
            return DiscordBot.getBot().getGateway().getUserById(Snowflake.of(id)).block().getMention();
        else
            return String.format("<@%s>", id);
    }
    public static String getUserTag(String id) {
        if(DiscordBot.getBot().getGateway().getUserById(Snowflake.of(id)).blockOptional().isPresent())
            return DiscordBot.getBot().getGateway().getUserById(Snowflake.of(id)).block().getMention();
        else
            return String.format("<@%s>", id);
    }
    public static boolean isValidUserMention(String input, Guild guild) {
        String mention = parseMention(input);
        return guild.getMemberById(Snowflake.of(input)).blockOptional().isPresent();
    }
    public static String parseMention(String input) {
        String toCheck = input.replaceAll("\n", "").trim();
        if (toCheck.startsWith("<@") && toCheck.endsWith(">")) { //user or role mentions
            String mention = toCheck.substring(2, toCheck.length() - 1);
            if (mention.startsWith("!") || mention.startsWith("&")) {
                mention = mention.substring(1);
            }
            return mention;
        }else if(toCheck.startsWith("<#") && toCheck.endsWith(">")) { //channels
            return toCheck.substring(2, toCheck.length() - 1);
        }
        return null;
    }
    public static User getUserByMention(String mention) {
        String id = parseMention(mention);
        return DiscordBot.getBot().getGateway().getUserById(Snowflake.of(id)).block();
    }
}
