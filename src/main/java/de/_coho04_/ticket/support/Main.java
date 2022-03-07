package de._coho04_.ticket.support;

import de._Coho04_.mysql.MYSQL;
import de._Coho04_.mysql.entities.Database;
import de._Coho04_.mysql.entities.MysqlTypes;
import de._Coho04_.mysql.entities.Table;
import de._coho04_.ticket.support.listener.Commands;
import de._coho04_.ticket.support.listener.Ready;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Main {

    private static JDA bot;
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

    public static void main(String[] args) {
        BotCreate();
    }

    public static void BotCreate() {
        try {
            bot = JDABuilder.createDefault(token)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.ROLE_TAGS, CacheFlag.EMOTE, CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS)
                    .enableIntents(GatewayIntent.GUILD_MESSAGE_REACTIONS,
                            GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_EMOJIS,
                            GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_PRESENCES,
                            GatewayIntent.GUILD_BANS, GatewayIntent.DIRECT_MESSAGE_REACTIONS,
                            GatewayIntent.GUILD_INVITES, GatewayIntent.DIRECT_MESSAGE_TYPING,
                            GatewayIntent.GUILD_MESSAGE_TYPING, GatewayIntent.GUILD_VOICE_STATES,
                            GatewayIntent.GUILD_WEBHOOKS, GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_MESSAGE_TYPING)
                    .addEventListeners(
                            new Commands(),
                            new Ready()
                    )
                    .setAutoReconnect(true)
                    .build();
            bot.upsertCommand(cmdSupport, "Erstellt ein Support Ticket für dich!").addOption(OptionType.STRING, cmdSupportOption, "Stell deine Frage", true).queue();
            bot.upsertCommand(cmdSettings, "Stellt den Discord Ticket-Support Bot ein!")
                    .addOption(OptionType.STRING, cmdSettingsActionOption, "Waehle deine Action", true)
                    .addOption(OptionType.STRING, cmdSettingsValueOption, "Den Wert den du Festlegen möchtest!", true)
                    .queue();
            bot.upsertCommand(cmdHelp, "Zeigt dir eine Liste aller möglichen Befehle an!").queue();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public static JDA getBot() {
        return bot;
    }
}
