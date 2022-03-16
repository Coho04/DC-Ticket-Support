package de.goldendeveloper.ticket.support.listener;

import de.goldendeveloper.mysql.entities.Column;
import de.goldendeveloper.mysql.entities.Database;
import de.goldendeveloper.mysql.entities.Table;
import de.goldendeveloper.ticket.support.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
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
                String question = e.getOption(Main.cmdSupportOption).getAsString();
                Main.getDiscord().getBot().getTextChannelById("949715424898056322").sendMessage(question).queue(msg -> {
                    msg.createThreadChannel("Ticket Support | " + e.getMember().getUser().getName()).queue(channel -> {
                        channel.addThreadMember(e.getUser()).queue();
                        channel.getManager().setLocked(true).queue();
                    });
                });
            } else if (cmd.equalsIgnoreCase(Main.cmdHelp)) {
                EmbedBuilder embed = new EmbedBuilder().setTitle("**Help**");
                for (Command command : Main.getDiscord().getBot().retrieveCommands().complete()) {
                    embed.addField(command.getName(), command.getDescription(), false);
                }
                e.getInteraction().replyEmbeds(embed.build()).queue();
            } else if (cmd.equalsIgnoreCase(Main.cmdSettings)) {
                Main.getMysql().connect();
                if (Main.getMysql().existsDatabase(Main.dbName)) {
                    Database db = Main.getMysql().getDatabase(Main.dbName);
                    if (db.existsTable(Main.tableName)) {
                        Table table = db.getTable(Main.tableName);
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
                            }
                        }
                    } else {
                        MessageEmbed embed = new EmbedBuilder()
                                .setTitle("**ERROR**")
                                .setColor(Color.RED)
                                .addField("Failed to find Table", "On Command", false)
                                .build();
                        Main.getDiscord().getBot().getTextChannelById(Main.DcErrorChannel).sendMessageEmbeds(embed).queue();
                    }
                } else {
                    MessageEmbed embed = new EmbedBuilder()
                            .setTitle("**ERROR**")
                            .setColor(Color.RED)
                            .addField("Failed to find Database", "On Commands", false)
                            .build();
                    Main.getDiscord().getBot().getTextChannelById(Main.DcErrorChannel).sendMessageEmbeds(embed).queue();
                }
                Main.getMysql().disconnect();
            }
        } else {
            e.getInteraction().reply("Dieser Command ist nur auf einem Server verfügbar!").queue();
        }
    }
}
