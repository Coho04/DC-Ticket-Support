package de.goldendeveloper.ticket.support;

import de.goldendeveloper.ticket.support.discord.Events;
import de.goldendeveloper.ticket.support.discord.commands.Settings;
import de.goldendeveloper.ticket.support.discord.commands.Support;
import io.github.coho04.dcbcore.DCBotBuilder;

public class Main {

    private static MysqlConnection mysqlConnection;

    public static void main(String[] args) {
        CustomConfig config = new CustomConfig();
        DCBotBuilder builder = new DCBotBuilder(args, true);
        builder.registerEvents(new Events());
        builder.registerCommands(new Settings(), new Support());
        builder.build();
        mysqlConnection = new MysqlConnection(config.getMysqlHostname(), config.getMysqlUsername(), config.getMysqlPassword(), config.getMysqlPort());
    }

    public static MysqlConnection getMysqlConnection() {
        return mysqlConnection;
    }
}
