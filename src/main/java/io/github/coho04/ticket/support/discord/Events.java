package io.github.coho04.ticket.support.discord;

import io.github.coho04.ticket.support.Main;
import io.github.coho04.ticket.support.MysqlConnection;
import io.github.coho04.mysql.entities.Database;
import io.github.coho04.mysql.entities.RowBuilder;
import io.github.coho04.mysql.entities.Table;
import io.sentry.Sentry;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Events extends ListenerAdapter {

    public static final String closeTicketEmoji = "\uD83D\uDD10";

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent e) {
        if (Main.getMysqlConnection().getMysql().existsDatabase(Main.getCustomConfig().getMysqlDatabase())) {
            Database db = Main.getMysqlConnection().getMysql().getDatabase(Main.getCustomConfig().getMysqlDatabase());
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
                } else {
                    System.out.println("Guild Id already exists in Table");
                }
            } else {
                System.out.println("Failed to find Table in onJoin");
                Sentry.captureMessage("Failed to find Table in onJoin");
            }
        } else {
            System.out.println("Failed to find Database in onJoin");
            Sentry.captureMessage("Failed to find Database in onJoin");
        }
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent e) {
        if (e.isFromGuild() && e.isFromThread() && !e.getUser().isBot()) {
            if (e.getReaction().getEmoji().equals(e.getJDA().getEmojiById("957226297556336670"))) {
                e.getChannel().asThreadChannel().delete().queue();
            }
            if (e.getEmoji().equals(Emoji.fromUnicode(closeTicketEmoji))) {
                e.getChannel().asThreadChannel().getManager().setArchived(true).queue();
            }
        }
    }
}
