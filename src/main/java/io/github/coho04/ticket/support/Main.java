package io.github.coho04.ticket.support;

import io.github.coho04.ticket.support.discord.Events;
import io.github.coho04.ticket.support.discord.commands.Settings;
import io.github.coho04.ticket.support.discord.commands.Support;
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
