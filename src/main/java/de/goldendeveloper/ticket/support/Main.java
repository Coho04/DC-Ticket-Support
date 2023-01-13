package de.goldendeveloper.ticket.support;

import de.goldendeveloper.ticket.support.discord.Discord;

public class Main {

    private static Discord discord;
    private static MysqlConnection mysqlConnection;
    private static Config config;
    private static ServerCommunicator serverCommunicator;

    private static Boolean restart = false;
    private static Boolean deployment = true;

    public static void main(String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("restart")) {
            restart = true;
        }
        if (System.getProperty("os.name").split(" ")[0].equalsIgnoreCase("windows")) {
            deployment = false;
        }
        config = new Config();
        serverCommunicator = new ServerCommunicator(config.getServerHostname(), config.getServerPort());
        mysqlConnection = new MysqlConnection(config.getMysqlHostname(), config.getMysqlUsername(), config.getMysqlPassword(), config.getMysqlPort());
        discord = new Discord(config.getDiscordToken());
    }


    public static MysqlConnection getMysqlConnection() {
        return mysqlConnection;
    }

    public static Discord getDiscord() {
        return discord;
    }

    public static Config getConfig() {
        return config;
    }

    public static Boolean getRestart() {
        return restart;
    }

    public static Boolean getDeployment() {
        return deployment;
    }

    public static ServerCommunicator getServerCommunicator() {
        return serverCommunicator;
    }
}
