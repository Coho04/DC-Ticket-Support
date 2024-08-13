package io.github.coho04.ticket.support;

import io.github.coho04.mysql.MYSQL;
import io.github.coho04.mysql.entities.Database;
import io.github.coho04.mysql.entities.Table;

public class MysqlConnection {

    private final MYSQL mysql;
    public static final String dbName = "ticket_support_db";
    public static final String tableName = "Support";
    public static final String cmnSupportChannelID = "SupportChannelID";
    public static final String cmnModeratorID = "ModeratorID";
    public static final String cmnGuildID = "GuildID";
    public static final String cmnOwnerID = "OwnerID";

    public MysqlConnection(String hostname, String username, String password, int port) {
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
            table.addColumn(cmnSupportChannelID);
        }
        if (!table.existsColumn(cmnModeratorID)) {
            table.addColumn(cmnModeratorID);
        }
        if (!table.existsColumn(cmnGuildID)) {
            table.addColumn(cmnGuildID);
        }
        if (!table.existsColumn(cmnOwnerID)) {
            table.addColumn(cmnOwnerID);
        }
        System.out.println("MYSQL Finished");
    }

    public MYSQL getMysql() {
        return mysql;
    }
}
