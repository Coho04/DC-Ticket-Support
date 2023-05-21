package de.goldendeveloper.ticket.support.discord;

import de.goldendeveloper.mysql.entities.Database;
import de.goldendeveloper.mysql.entities.RowBuilder;
import de.goldendeveloper.mysql.entities.Table;
import de.goldendeveloper.ticket.support.MysqlConnection;
import de.goldendeveloper.ticket.support.Main;
import io.sentry.Sentry;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Events extends ListenerAdapter {

    public static final String closeTicketEmoji = "\uD83D\uDD10";

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent e) {
        if (Main.getMysqlConnection().getMysql().existsDatabase(MysqlConnection.dbName)) {
            Database db = Main.getMysqlConnection().getMysql().getDatabase(MysqlConnection.dbName);
            if (db.existsTable(MysqlConnection.tableName)) {
                Table table = db.getTable(MysqlConnection.tableName);
                List<String> guilds = table.getColumn(MysqlConnection.cmnGuildID).getAll().getAsString();
                if (!guilds.contains(e.getGuild().getId())) {
                    table.insert(new RowBuilder()
                            .with(table.getColumn(MysqlConnection.cmnGuildID), e.getGuild().getId())
                            .with(table.getColumn(MysqlConnection.cmnModeratorID), "null")
                            .with(table.getColumn(MysqlConnection.cmnOwnerID), e.getGuild().getOwnerId())
                            .with(table.getColumn(MysqlConnection.cmnSupportChannelID), "null")
                            .build()
                    );
                }
            } else {
                Sentry.captureMessage("Failed to find Table in onJoin");
            }
        } else {
            Sentry.captureMessage("Failed to find Database in onJoin");
        }
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent e) {
        if (e.isFromGuild() && e.isFromThread() && !e.getUser().isBot()) {
            RichCustomEmoji emote = e.getJDA().getEmojiById("957226297556336670");
            if (e.getReaction().getEmoji().asCustom().getImageUrl().equals(emote.getImageUrl())) {
                e.getChannel().asThreadChannel().delete().queue();
            }
            if (e.getEmoji().equals(Emoji.fromUnicode(closeTicketEmoji))) {
                e.getChannel().asThreadChannel().getManager().setArchived(true).queue();
            }
        }
    }
}
