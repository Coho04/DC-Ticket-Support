package de.goldendeveloper.ticket.support;

import de.goldendeveloper.dcbcore.DCBotBuilder;
import de.goldendeveloper.dcbcore.interfaces.CommandInterface;
import de.goldendeveloper.ticket.support.discord.Events;
import de.goldendeveloper.ticket.support.discord.commands.Settings;
import de.goldendeveloper.ticket.support.discord.commands.Support;

import java.util.LinkedList;

public class Main {

    private static MysqlConnection mysqlConnection;

    public static void main(String[] args) {
        CustomConfig config = new CustomConfig();
        DCBotBuilder builder = new DCBotBuilder(args, true);
        builder.registerEvents(new Events());
        LinkedList<CommandInterface> commands = new LinkedList<>();
        commands.add(new Settings());
        commands.add(new Support());
        builder.registerCommands(commands);
        builder.build();
        mysqlConnection = new MysqlConnection(config.getMysqlHostname(), config.getMysqlUsername(), config.getMysqlPassword(), config.getMysqlPort());
    }

    public static MysqlConnection getMysqlConnection() {
        return mysqlConnection;
    }
}
