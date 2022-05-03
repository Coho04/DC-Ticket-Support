package de.goldendeveloper.ticket.support.listener;

import de.goldendeveloper.mysql.entities.Database;
import de.goldendeveloper.mysql.entities.RowBuilder;
import de.goldendeveloper.mysql.entities.Table;
import de.goldendeveloper.ticket.support.CreateMysql;
import de.goldendeveloper.ticket.support.Main;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;

public class OnJoin extends ListenerAdapter {

    @Override
    public void onGuildJoin(GuildJoinEvent e) {
        if (Main.getCreateMysql().getMysql().existsDatabase(CreateMysql.dbName)) {
            Database db = Main.getCreateMysql().getMysql().getDatabase(CreateMysql.dbName);
            if (db.existsTable(CreateMysql.tableName)) {
                Table table = db.getTable(CreateMysql.tableName);
                List<Object> guilds = table.getColumn(CreateMysql.cmnGuildID).getAll();
                if (!guilds.contains(e.getGuild().getId())) {
                    table.insert(new RowBuilder()
                            .with(table.getColumn(CreateMysql.cmnGuildID), e.getGuild().getId())
                            .with(table.getColumn(CreateMysql.cmnModeratorID), "null")
                            .with(table.getColumn(CreateMysql.cmnOwnerID), e.getGuild().getOwnerId())
                            .with(table.getColumn(CreateMysql.cmnSupportChannelID), "null")
                            .build()
                    );
                }
            } else {
                Main.getDiscord().sendErrorMessage("Failed to find Table in onJoin");
            }
        } else {
            Main.getDiscord().sendErrorMessage("Failed to find Database in onJoin");
        }
    }
}
