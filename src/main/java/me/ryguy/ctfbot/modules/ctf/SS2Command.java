package me.ryguy.ctfbot.modules.ctf;

import discord4j.core.object.entity.Message;
import discord4j.rest.util.Color;
import me.ryguy.ctfbot.module.ModuleCommand;
import me.ryguy.ctfbot.module.Modules;
import me.ryguy.ctfbot.util.EmbedBuilder;
import me.ryguy.ctfbot.util.SSHelper;
import me.ryguy.discordapi.command.Command;
import reactor.core.publisher.Mono;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

@ModuleCommand(module = Modules.CTF)
public class SS2Command extends Command {
    public SS2Command() {
        super("spreadsheet2", "ss2");
    }

    @Override
    public Mono<Void> execute(Message message, String alais, String[] args) {
        if (args.length != 1) {
            message.getChannel().block().createEmbed(e -> {
                e.setColor(Color.RED);
                e.setDescription(":x: You need some arguments! \nPossible arguments: `upcoming, now, past`");
            }).block();
            return Mono.empty();
        }
        if (!args[0].equalsIgnoreCase("upcoming") && !args[0].equalsIgnoreCase("now") && !args[0].equalsIgnoreCase("past")) {
            message.getChannel().block().createEmbed(e -> {
                e.setColor(Color.RED);
                e.setDescription(String.format(":x: Invalid argument `%s`! \nPossible arguments: `upcoming, now, past`", args[0]));
            }).block();
            return Mono.empty();
        }
        Message msg = message.getChannel().block().createEmbed(e -> {
            e.setColor(Color.TAHITI_GOLD);
            e.setDescription(":arrows_counterclockwise: Fetching Match data!");
        }).block();
        try {
            SSHelper ss = new SSHelper("1CrQOxzaXC6iSjwZwQvu6DNIYsCDg-uQ4x5UiaWLHzxg", "Upcoming Matches (Server 2)!C10:T60");
            List<SSHelper.Match> matches = ss.getMatches();
            switch (args[0].toLowerCase()) {
                case "upcoming":
                    matches = matches.parallelStream().filter(match -> match.getBegin().getTime() >= Date.from(Instant.now()).getTime()).collect(Collectors.toList());
                    break;
                case "now":
                    Instant now = Instant.now();
                    SSHelper.Match current = matches.parallelStream().filter(match -> match.getBegin().getTime() <= (now.getEpochSecond() * 1000) && match.getEnd().getTime() >= (now.getEpochSecond() * 1000)).findFirst().orElse(null);
                    if (current == null) {
                        msg.edit(m -> {
                            m.setEmbed(em -> {
                                em.setColor(Color.GREEN);
                                em.setDescription("Match Server should currently be empty!");
                            });
                        }).block();
                        return Mono.empty();
                    } else {
                        matches.clear();
                        matches.add(current);
                    }
                    break;
                case "past":
                    matches = matches.parallelStream().filter(match -> match.getBegin().getTime() <= Date.from(Instant.now()).getTime()).collect(Collectors.toList());
                    matches.sort((o1, o2) -> {
                        if (o1.begin.toInstant().isAfter(o2.begin.toInstant())) return -1;
                        if (o1.begin.toInstant().isBefore(o2.begin.toInstant())) return 1;
                        return 0;
                    });
                    break;
            }
            EmbedBuilder builder = new EmbedBuilder();
            SimpleDateFormat date = new SimpleDateFormat("MMMM d");
            date.setTimeZone(TimeZone.getTimeZone("EST"));

            SimpleDateFormat time = new SimpleDateFormat("h:mma");
            time.setTimeZone(TimeZone.getTimeZone("EST"));
            int fields = 0;
            for (SSHelper.Match match : matches) {
                if (fields != 0 && fields % 25 == 0) { //covers too many matches
                    msg.edit(m -> {
                        m.setEmbed(em -> {
                            em.setTitle(":white_check_mark: Success!");
                            em.setColor(Color.GREEN);
                            if (builder.getFields().isEmpty()) {
                                em.setDescription("There are no upcoming matches, someone host :(");
                            } else {
                                for (EmbedBuilder.Field f : builder.getFields()) {
                                    em.addField(f.getTitle(), f.getValue(), f.isInline());
                                }
                            }
                        });
                    }).block();
                    return Mono.empty();
                } else {
                    builder.addField(new EmbedBuilder.Field(match.getName(), match.getDay() + ", " + date.format(match.getBegin()) + "\n" + time.format(match.getBegin()) + " - " + time.format(match.getEnd()) + " EST", false));
                    fields++;
                }
            }
            msg.edit(m -> {
                m.setEmbed(em -> {
                    em.setTitle(":white_check_mark: Success!");
                    em.setColor(Color.GREEN);
                    if (builder.getFields().isEmpty()) {
                        em.setDescription("There are no upcoming matches, someone host :(");
                    } else {
                        for (EmbedBuilder.Field f : builder.getFields()) {
                            em.addField(f.getTitle(), f.getValue(), f.isInline());
                        }
                    }
                });
            }).block();
            return Mono.empty();
        } catch (Exception e) {
            if (e instanceof NullPointerException) {
                msg.edit(m -> {
                    m.setEmbed(em -> {
                        em.setDescription(":x: You have been rate limited and there is no match data cached!\nPlease wait one minute before using an SS command again");
                        em.setColor(Color.RED);
                    });
                }).block();
                e.printStackTrace();
            } else {
                msg.edit(m -> {
                    m.setEmbed(em -> {
                        em.setDescription(":x: Error grabbing match info!");
                        em.setColor(Color.RED);
                    });
                }).block();
                e.printStackTrace();
            }
        }
        return Mono.empty();
    }
}
