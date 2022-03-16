package de.goldendeveloper.ticket.support;

import de.goldendeveloper.mysql.entities.Database;
import de.goldendeveloper.mysql.entities.MysqlTypes;
import de.goldendeveloper.mysql.entities.Table;
import de.goldendeveloper.mysql.MYSQL;

public class Main {

    private static MYSQL mysql;
    private static final String token = "OTQ5NzE0NDkxNDE3OTY4Njcx.YiOYiw.CbaI-GfZSAX84Ub9h8xmezfU13Y";

    public static final String DcErrorChannel = "854740410189742150";

    public static final String cmdSupport = "support";
    public static final String cmdSupportOption = "frage";
    public static final String cmdSettings = "settings";
    public static final String cmdSettingsSubSupport = "support";
    public static final String cmdSettingsSubSupportOptionChannel = "channel";
    public static final String cmdSettingsSubModerator = "moderator";
    public static final String cmdSettingsSubModeratorOptionRole = "role";
    public static final String cmdHelp = "help";

    public static final String dbName = "TicketSupport";
    public static final String tableName = "Support";
    public static final String cmnSupportChannelID = "SupportChannelID";
    public static final String cmnModeratorID = "ModeratorID";
    public static final String cmnGuildID = "GuildID";
    public static final String cmnOwnerID = "OwnerID";

    private static Discord discord;

    public static void main(String[] args) {
        discord = new Discord(token);
        Main.MysqlFirstStart("138.201.202.3", "DC-Ticket-Support", "2tEW7yRe8!t2[S90", 3306);
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
