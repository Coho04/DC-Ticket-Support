package de._coho04_.ticket.support.listener;

import de._Coho04_.mysql.entities.Database;
import de._Coho04_.mysql.entities.Row;
import de._Coho04_.mysql.entities.Table;
import de._coho04_.ticket.support.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
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
                    table.insert(new Row(table, table.getDatabase())
                            .with(Main.cmnGuildID, e.getGuild().getId())
                            .with(Main.cmnModeratorID, "null")
                            .with(Main.cmnOwnerID, e.getGuild().getOwnerId())
                            .with(Main.cmnSupportChannelID, "null"));
                }
            } else {
                MessageEmbed embed = new EmbedBuilder()
                        .setTitle("**ERROR**")
                        .setColor(Color.RED)
                        .addField("Failed to find Table", "Guild Join Event", false)
                        .build();
                Main.getBot().getTextChannelById(Main.DcErrorChannel).sendMessageEmbeds(embed).queue();
            }
        } else {
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("**ERROR**")
                    .setColor(Color.RED)
                    .addField("Failed to find Database", "Guild Join Event", false)
                    .build();
            Main.getBot().getTextChannelById(Main.DcErrorChannel).sendMessageEmbeds(embed).queue();
        }
    }
}
