package io.github.coho04.ticket.support;

import io.github.coho04.ticket.support.discord.Events;
import io.github.coho04.ticket.support.discord.commands.Settings;
import io.github.coho04.ticket.support.discord.commands.Support;
import io.github.coho04.dcbcore.DCBotBuilder;

public class Main {

    private static MysqlConnection mysqlConnection;
    private static CustomConfig customConfig;

    public static void main(String[] args) {
        customConfig = new CustomConfig();
        DCBotBuilder builder = new DCBotBuilder(args, true);
        builder.registerEvents(new Events());
        builder.registerCommands(new Settings(), new Support());
        builder.build();
        mysqlConnection = new MysqlConnection(customConfig.getMysqlHostname(), customConfig.getMysqlUsername(), customConfig.getMysqlPassword(), customConfig.getMysqlPort());
        System.out.println("Java application started successfully");
    }

    public static MysqlConnection getMysqlConnection() {
        return mysqlConnection;
    }

    public static CustomConfig getCustomConfig() {
        return customConfig;
    }
}
