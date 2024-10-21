package io.github.coho04.ticket.support.discord.commands;

import io.github.coho04.ticket.support.Main;
import io.github.coho04.ticket.support.MysqlConnection;
import io.github.coho04.ticket.support.discord.Events;
import io.github.coho04.dcbcore.DCBot;
import io.github.coho04.dcbcore.interfaces.CommandInterface;
import io.github.coho04.mysql.entities.Database;
import io.github.coho04.mysql.entities.SearchResult;
import io.github.coho04.mysql.entities.Table;
import io.sentry.Sentry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public class Support implements CommandInterface {

    public static final String cmdSupport = "support";
    public static final String cmdSupportOption = "frage";

    @Override
    public CommandData commandData() {
        return Commands.slash(cmdSupport, "Erstelle ein Support Ticket!")
                .setGuildOnly(true)
                .addOption(OptionType.STRING, cmdSupportOption, "Stell deine Frage", true);
    }

    @Override
    public void runSlashCommand(SlashCommandInteractionEvent e, DCBot dcBot) {
        if (!Main.getMysqlConnection().getMysql().existsDatabase(Main.getCustomConfig().getMysqlDatabase())) {
            throw createException("Database " + Main.getCustomConfig().getMysqlDatabase() + " does not exist");
        }
        Database db = Main.getMysqlConnection().getMysql().getDatabase(Main.getCustomConfig().getMysqlDatabase());
        if (!db.existsTable(MysqlConnection.tableName)) {
            throw createException("Table " + MysqlConnection.tableName + " does not exist");
        }
        Table table = db.getTable(MysqlConnection.tableName);
        if (!table.hasColumn(MysqlConnection.cmnModeratorID) && !table.hasColumn(MysqlConnection.cmnGuildID)) {
            throw createException("Column " + MysqlConnection.cmnGuildID + " or " + MysqlConnection.cmnModeratorID + " does not exist");
        }
        if (table.getColumn(MysqlConnection.cmnGuildID).getAll().getAsString().contains(e.getGuild().getId())) {
            HashMap<String, SearchResult> row = table.getRow(table.getColumn(MysqlConnection.cmnGuildID), e.getGuild().getId()).getData();
            Role role = e.getJDA().getRoleById(row.get(MysqlConnection.cmnModeratorID).getAsString());
            RichCustomEmoji emote = e.getJDA().getEmojiById("957226297556336670");
            if (role == null || emote == null) {
                return;
            }
            TextChannel supportChannel = e.getJDA().getTextChannelById(row.get(MysqlConnection.cmnSupportChannelID).getAsLong());
            if (supportChannel == null) {
                return;
            }
            EmbedBuilder embed = createEmbed(e, role, emote);
            supportChannel.sendMessageEmbeds(embed.build()).queue();
            List<ThreadChannel> channels = e.getGuild().getThreadChannelsByName("Ticket Support | " + e.getUser().getName(), true);
            if (channels.isEmpty()) {
                supportChannel.createThreadChannel("Ticket Support | " + e.getUser().getName()).queue(channel -> {
                    channel.addThreadMember(e.getUser()).queue();
                    channel.getManager().setLocked(true).queue();
                    channel.sendMessageEmbeds(embed.build()).queue(message -> {
                        message.addReaction(Emoji.fromUnicode(Events.closeTicketEmoji)).queue();
                        message.addReaction(emote).queue();
                    });
                });
            } else {
                ThreadChannel channelSupport = channels.get(0);
                if (channelSupport == null) {
                    return;
                }
                channelSupport.getManager().setArchived(false).queue();
            }
            e.getInteraction().reply("Dein Support Ticket wurde erstellt!").queue();
        } else {
            e.reply("Es wurde kein Support Channel gefunden! Bitte Leite diesen Fehler an das Server Team weiter").queue();
        }

    }

    public EmbedBuilder createEmbed(SlashCommandInteractionEvent e, Role role, RichCustomEmoji emote) {
        return new EmbedBuilder()
                .setTitle("**Support**")
                .setColor(Color.GREEN)
                .setFooter("TicketSupport", e.getJDA().getSelfUser().getAvatarUrl())
                .setThumbnail(e.getUser().getAvatarUrl())
                .setTimestamp(LocalDateTime.now())
                .addField("Ticket Support | " + e.getUser().getName(), "Offene Frage: " + e.getOption(cmdSupportOption).getAsString(), true)
                .addField("", emote.getAsMention() + " Zum löschen des Tickets", false)
                .addField("", ":closed_lock_with_key:  Zum Archivieren des Tickets", false)
                .addField("", role.getAsMention(), true);
    }


    private RuntimeException createException(String message) {
        RuntimeException exception = new RuntimeException(message);
        Sentry.captureException(exception);
        throw exception;
    }

}
