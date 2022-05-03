package de.goldendeveloper.ticket.support;

public class Main {

    private static Discord discord;
    private static CreateMysql createMysql;
    private static Config config;

    public static void main(String[] args) {
        config = new Config();
        createMysql = new CreateMysql(config.getMysqlHostname(), config.getMysqlUsername(), config.getMysqlPassword(), config.getMysqlPort());
        discord = new Discord(config.getDiscordToken());
    }

    public static CreateMysql getCreateMysql() {
        return createMysql;
    }

    public static Discord getDiscord() {
        return discord;
    }

    public static Config getConfig() {
        return config;
    }
}
