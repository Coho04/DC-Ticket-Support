package de.goldendeveloper.ticket.support;

import de.goldendeveloper.mysql.MYSQL;
import de.goldendeveloper.mysql.entities.Database;
import de.goldendeveloper.mysql.entities.MysqlTypes;
import de.goldendeveloper.mysql.entities.Table;

public class CreateMysql {

    private final MYSQL mysql;
    public static final String dbName = "TicketSupport";
    public static final String tableName = "Support";
    public static final String cmnSupportChannelID = "SupportChannelID";
    public static final String cmnModeratorID = "ModeratorID";
    public static final String cmnGuildID = "GuildID";
    public static final String cmnOwnerID = "OwnerID";

    public CreateMysql(String hostname, String username, String password, int port) {
        mysql = new MYSQL(hostname, username, password, port);
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
        System.out.println("MYSQL Finished");
    }

    public MYSQL getMysql() {
        return mysql;
    }
}
