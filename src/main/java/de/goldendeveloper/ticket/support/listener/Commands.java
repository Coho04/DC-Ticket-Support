package de.goldendeveloper.ticket.support.listener;

import de.goldendeveloper.mysql.entities.Column;
import de.goldendeveloper.mysql.entities.Database;
import de.goldendeveloper.mysql.entities.Table;
import de.goldendeveloper.ticket.support.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.HashMap;

public class Commands extends ListenerAdapter {

    private static final String closeTicketEmoji = "\uD83D\uDD10";

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
        if (e.isFromGuild()) {
            String cmd = e.getName();
            if (cmd.equalsIgnoreCase(Main.cmdSupport)) {
                e.getInteraction().reply(createSupportTicket(e, e.getUser())).queue();
            } else if (cmd.equalsIgnoreCase(Main.cmdHelp)) {
                e.getInteraction().replyEmbeds(createHelpMessage()).queue();
            } else if (cmd.equalsIgnoreCase(Main.cmdSettings)) {
                cmdSettings(e);
            }
        } else {
            e.getInteraction().reply("Dieser Command ist nur auf einem Server verfügbar!").queue();
        }
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent e) {
        if (e.isFromGuild()) {
            if (e.isFromThread()) {
                if (!e.getUser().isBot()) {
                    Emote emote = e.getJDA().getEmoteById("957226297556336670");
                    if (e.getReactionEmote().isEmote()) {
                        if (e.getReactionEmote().getEmote().getImageUrl().equals(emote.getImageUrl())) {
                            e.getThreadChannel().delete().queue();
                        }
                    } else if (e.getReactionEmote().isEmoji()) {
                        if (e.getReactionEmote().getEmoji().equalsIgnoreCase(closeTicketEmoji)) {
                            e.getThreadChannel().getManager().setArchived(true).queue();
                        }
                    }
                }
            }
        }
    }

    private static String createSupportTicket(SlashCommandInteractionEvent e, User user) {
        if (Main.getMysql().existsDatabase(Main.dbName)) {
            Database db = Main.getMysql().getDatabase(Main.dbName);
            if (db.existsTable(Main.tableName)) {
                Table table = db.getTable(Main.tableName);
                if (table.hasColumn(Main.cmnModeratorID)) {
                    if (table.hasColumn(Main.cmnGuildID)) {
                        if (table.getColumn(Main.cmnGuildID).getAll().contains(e.getGuild().getId())) {
                            HashMap<String, Object> row = table.getRow(table.getColumn(Main.cmnGuildID), e.getGuild().getId());
                            String roleID = row.get(Main.cmnModeratorID).toString();
                            Role role = e.getJDA().getRoleById(roleID);
                            Emote emote = e.getJDA().getEmoteById("957226297556336670");
                            if (role != null && emote != null) {
                                EmbedBuilder embed = new EmbedBuilder();
                                embed.setTitle("**Support**");
                                embed.setColor(Color.GREEN);
                                embed.setFooter("Golden-Developer", e.getJDA().getSelfUser().getAvatarUrl());
                                embed.setThumbnail(e.getUser().getAvatarUrl());
                                embed.setTimestamp(LocalDateTime.now());
                                embed.addField("Ticket Support | " + user.getName(), "Offene Frage: " + e.getOption(Main.cmdSupportOption).getAsString(), true);
                                embed.addField("", emote.getAsMention() + " Zum löschen des Tickets", false);
                                embed.addField("", ":closed_lock_with_key:  Zum Archivieren des Tickets", false);
                                embed.addField("", role.getAsMention(), true);

                                TextChannel SupportChannel = e.getJDA().getTextChannelById(row.get(Main.cmnSupportChannelID).toString());
                                if (SupportChannel != null) {
                                    SupportChannel.sendMessageEmbeds(embed.build()).queue();
                                    if (e.getGuild().getThreadChannelsByName("Ticket Support | " + user.getName(), true).isEmpty()) {
                                        SupportChannel.createThreadChannel("Ticket Support | " + user.getName()).queue(channel -> {
                                            channel.addThreadMember(user).queue();
                                            channel.getManager().setLocked(true).queue();
                                            channel.sendMessageEmbeds(embed.build()).queue(message -> {
                                                message.addReaction(closeTicketEmoji).queue();
                                                message.addReaction(emote).queue();
                                            });
                                        });
                                    } else {
                                        ThreadChannel channelSupport = e.getGuild().getThreadChannelsByName("Ticket Support | " + user.getName(), true).get(0);
                                        if (channelSupport != null) {
                                            channelSupport.getManager().setArchived(false).queue();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return "Dein Support Ticket wurde erstellt!";
    }

    private static MessageEmbed createHelpMessage() {
        EmbedBuilder embed = new EmbedBuilder().setTitle("**Help**");
        for (Command command : Main.getDiscord().getBot().retrieveCommands().complete()) {
            embed.addField(command.getName(), command.getDescription(), false);
        }
        return embed.build();
    }

    private static void cmdSettings(SlashCommandInteractionEvent e) {
        if (Main.getMysql().existsDatabase(Main.dbName)) {
            if (Main.getMysql().getDatabase(Main.dbName).existsTable(Main.tableName)) {
                Table table = Main.getMysql().getDatabase(Main.dbName).getTable(Main.tableName);
                if (table.hasColumn(Main.cmnGuildID)) {
                    if (table.getColumn(Main.cmnGuildID).getAll().contains(e.getGuild().getId())) {
                        HashMap<String, Object> map = table.getRow(table.getColumn(Main.cmnGuildID), e.getGuild().getId());
                        int id = Integer.parseInt(map.get("id").toString());
                        String subName = e.getSubcommandName();
                        if (subName != null) {
                            switch (subName) {
                                case "support" -> {
                                    TextChannel valueChannel = e.getOption(Main.cmdSettingsSubSupportOptionChannel).getAsTextChannel();
                                    if (valueChannel != null) {
                                        Column clm = table.getColumn(Main.cmnSupportChannelID);
                                        clm.set(valueChannel.getId(), id);
                                        e.getInteraction().reply("Der Support Channel wurde erfolgreich auf " + valueChannel.getAsMention() + " gesetzt!").queue();
                                    }
                                }
                                case "moderator" -> {
                                    Role valueRole = e.getOption(Main.cmdSettingsSubModeratorOptionRole).getAsRole();
                                    Column clm = table.getColumn(Main.cmnModeratorID);
                                    clm.set(valueRole.getId(), id);
                                    e.getInteraction().reply("Der Moderator Role wurde erfolgreich auf " + valueRole.getAsMention() + " gesetzt!").queue();
                                }
                            }
                        }
                    } else {
                        e.getInteraction().reply("ERROR: Bitte den Discord Bot neu einladen!").queue();
                        e.getGuild().leave().queue();
                        Main.getDiscord().sendErrorMessage("Failed to find DISCORD GUILD ID in Command");
                    }
                } else {
                    Main.getDiscord().sendErrorMessage("Failed to find COLUMN in Command");
                }
            } else {
                Main.getDiscord().sendErrorMessage("Failed to find Table in Command");
            }
        } else {
            Main.getDiscord().sendErrorMessage("Failed to find Database on Commands");
        }
    }
}
