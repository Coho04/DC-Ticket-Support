package de.goldendeveloper.ticket.support.listener;

import de.goldendeveloper.mysql.entities.Database;
import de.goldendeveloper.mysql.entities.Row;
import de.goldendeveloper.mysql.entities.Table;
import de.goldendeveloper.ticket.support.Main;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;

public class onJoin extends ListenerAdapter {

    @Override
    public void onGuildJoin(GuildJoinEvent e) {
        Main.getMysql().connect();
        if (Main.getMysql().existsDatabase(Main.dbName)) {
            Database db = Main.getMysql().getDatabase(Main.dbName);
            if (db.existsTable(Main.tableName)) {
                Table table = db.getTable(Main.tableName);
                List<Object> guilds = table.getColumn(Main.cmnGuildID).getAll();
                if (!guilds.contains(e.getGuild().getId())) {
                    table.insert(new Row(table, table.getDatabase()).with(Main.cmnGuildID, e.getGuild().getId()).with(Main.cmnModeratorID, "null").with(Main.cmnOwnerID, e.getGuild().getOwnerId()).with(Main.cmnSupportChannelID, "null"));
                }
            } else {
                Main.getDiscord().getBot().getTextChannelById(Main.DcErrorChannel).sendMessageEmbeds(Main.getDiscord().getErrorEmbed("Table", "onJoin")).queue();
            }
        } else {
            Main.getDiscord().getBot().getTextChannelById(Main.DcErrorChannel).sendMessageEmbeds(Main.getDiscord().getErrorEmbed("Database", "onJoin")).queue();
        }
    }
}
