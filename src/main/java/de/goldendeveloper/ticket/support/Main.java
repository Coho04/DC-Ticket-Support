package de.goldendeveloper.ticket.support;

import de.goldendeveloper.ticket.support.discord.Discord;
import io.sentry.ITransaction;
import io.sentry.Sentry;
import io.sentry.SpanStatus;

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

        Sentry(config.getSentryDNS());
        ITransaction transaction = Sentry.startTransaction("Application()", "task");
        try {
            Application();
        } catch (Exception e) {
            transaction.setThrowable(e);
            transaction.setStatus(SpanStatus.INTERNAL_ERROR);
        } finally {
            transaction.finish();
        }
    }

    public static void Application() {
        serverCommunicator = new ServerCommunicator(config.getServerHostname(), config.getServerPort());
        mysqlConnection = new MysqlConnection(config.getMysqlHostname(), config.getMysqlUsername(), config.getMysqlPassword(), config.getMysqlPort());
        discord = new Discord(config.getDiscordToken());
    }

    public static void Sentry(String dns) {
        Sentry.init(options -> {
            options.setDsn(dns);
            options.setTracesSampleRate(1.0);
            options.setEnvironment(Main.getDeployment() ? "Production" : "localhost");
        });
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
