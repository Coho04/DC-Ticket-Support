package de.goldendeveloper.ticket.support;

import de.goldendeveloper.mysql.entities.Database;
import de.goldendeveloper.mysql.entities.MysqlTypes;
import de.goldendeveloper.mysql.entities.Table;
import de.goldendeveloper.mysql.MYSQL;

public class Main {

    private static MYSQL mysql;
    private static final String token = "OTQ5NzE0NDkxNDE3OTY4Njcx.YiOYiw.CbaI-GfZSAX84Ub9h8xmezfU13Y";

    public static final String DcErrorChannel = "854740410189742150";

    public static String cmdSupport = "support";
    public static String cmdSupportOption = "frage";
    public static String cmdSettings = "settings";
    public static String cmdSettingsActionOption = "action";
    public static String cmdSettingsValueOption = "value";
    public static String cmdHelp = "help";

    public static String dbName = "TicketSupport";
    public static String tableName = "Support";
    public static String cmnSupportChannelID = "SupportChannelID";
    public static String cmnModeratorID = "ModeratorID";
    public static String cmnGuildID = "GuildID";
    public static String cmnOwnerID = "OwnerID";

    private static Discord discord;

    public static void main(String[] args) {
        discord = new Discord(token);
        Main.MysqlFirstStart("", "", "", 3306);
    }

    public static void MysqlFirstStart(String hostname, String username, String password, int port) {
        mysql = new MYSQL(hostname, username, password, port);
        mysql.connect();
        if (!mysql.existsDatabase(dbName)) {
            mysql.createDatabase(dbName);
        }
        Database db = mysql.getDatabase(dbName);
        if (!db.existsTable(tableName)) {
            db.createTable(tableName);
        }
        Table table = db.getTable(tableName);
        if (!table.existsColumn(cmnSupportChannelID)) {
            table.addColumn(cmnSupportChannelID, MysqlTypes.VARCHAR, 50);
        }
        if (!table.existsColumn(cmnModeratorID)) {
            table.addColumn(cmnModeratorID, MysqlTypes.VARCHAR, 50);
        }
        if (!table.existsColumn(cmnGuildID)) {
            table.addColumn(cmnGuildID, MysqlTypes.VARCHAR, 50);
        }
        if (!table.existsColumn(cmnOwnerID)) {
            table.addColumn(cmnOwnerID, MysqlTypes.VARCHAR, 50);
        }
        mysql.disconnect();
    }

    public static MYSQL getMysql() {
        return mysql;
    }

    public static Discord getDiscord() {
        return discord;
    }
}
