package de.goldendeveloper.ticket.support.listener;

import de.goldendeveloper.mysql.entities.Column;
import de.goldendeveloper.mysql.entities.Table;
import de.goldendeveloper.ticket.support.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.awt.*;
import java.util.HashMap;

public class Commands extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
        if (e.isFromGuild()) {
            String cmd = e.getName();
            if (cmd.equalsIgnoreCase(Main.cmdSupport)) {
                createSupportTicket(e.getOption(Main.cmdSupportOption).getAsString(), e.getUser());
            } else if (cmd.equalsIgnoreCase(Main.cmdHelp)) {
                e.getInteraction().replyEmbeds(createHelpMessage()).queue();
            } else if (cmd.equalsIgnoreCase(Main.cmdSettings)) {
                cmdSettings(e);
            }
        } else {
            e.getInteraction().reply("Dieser Command ist nur auf einem Server verfügbar!").queue();
        }
    }

    private static String createSupportTicket(String question, User user) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("**Support**");
        embed.setColor(Color.GREEN);
        embed.addField("Ticket Support | " + user.getName(),"Grund: " + question, true);
        Main.getDiscord().getBot().getTextChannelById("949715424898056322").sendMessage(question).queue();
        Main.getDiscord().getBot().getTextChannelById("949715424898056322").createThreadChannel("Ticket Support | " + user.getName()).queue(channel -> {
                channel.addThreadMember(user).queue();
                channel.getManager().setLocked(true).queue();
                channel.sendMessageEmbeds(embed.build()).queue(message -> {
                    message.addReaction("Close").queue();
                    message.addReaction("Delete").queue();
                });
        });
        return "Dein Support ticket wurde erstellt!";
    }

    private static MessageEmbed createHelpMessage() {
        EmbedBuilder embed = new EmbedBuilder().setTitle("**Help**");
        for (Command command : Main.getDiscord().getBot().retrieveCommands().complete()) {
            embed.addField(command.getName(), command.getDescription(), false);
        }
        return embed.build();
    }

    private static void cmdSettings(SlashCommandInteractionEvent e) {
        Main.getMysql().connect();
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
                        Main.getDiscord().getBot().getTextChannelById(Main.DcErrorChannel).sendMessageEmbeds(Main.getDiscord().getErrorEmbed("DISCORD GUILD ID", "Command")).queue();
                    }
                } else {
                    Main.getDiscord().getBot().getTextChannelById(Main.DcErrorChannel).sendMessageEmbeds(Main.getDiscord().getErrorEmbed("COLUMN", "Command")).queue();
                }
            } else {
                Main.getDiscord().getBot().getTextChannelById(Main.DcErrorChannel).sendMessageEmbeds(Main.getDiscord().getErrorEmbed("Table", "Command")).queue();
            }
        } else {
            Main.getDiscord().getBot().getTextChannelById(Main.DcErrorChannel).sendMessageEmbeds(Main.getDiscord().getErrorEmbed("Database", "Commands")).queue();
        }
        Main.getMysql().disconnect();
    }
}
