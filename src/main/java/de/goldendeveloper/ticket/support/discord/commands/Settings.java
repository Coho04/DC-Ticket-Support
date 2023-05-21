package de.goldendeveloper.ticket.support.discord.commands;

import de.goldendeveloper.dcbcore.DCBot;
import de.goldendeveloper.dcbcore.interfaces.CommandInterface;
import de.goldendeveloper.mysql.entities.Table;
import de.goldendeveloper.ticket.support.Main;
import de.goldendeveloper.ticket.support.MysqlConnection;
import io.sentry.Sentry;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Settings implements CommandInterface {

    public static final String cmdSettingsSubSupport = "support";
    public static final String cmdSettingsSubSupportOptionChannel = "channel";
    public static final String cmdSettingsSubModerator = "moderator";
    public static final String cmdSettingsSubModeratorOptionRole = "role";

    @Override
    public CommandData commandData() {
        return Commands.slash("settings", "Zeigt die Einstellungen des Servers an!").setGuildOnly(true)
                .addSubcommands(
                        new SubcommandData(cmdSettingsSubSupport, "Setze den Support Channel für das Ticket System").addOption(OptionType.CHANNEL, cmdSettingsSubSupportOptionChannel, "Der Support Channel für das Ticket System", true),
                        new SubcommandData(cmdSettingsSubModerator, "Setzt die Moderator Role für das Ticket System").addOption(OptionType.ROLE, cmdSettingsSubModeratorOptionRole, "Die Moderatoren Rolle für das Ticket System", true)
                );
    }

    @Override
    public void runSlashCommand(SlashCommandInteractionEvent e, DCBot dcBot) {
        if (Main.getMysqlConnection().getMysql().existsDatabase(MysqlConnection.dbName)) {
            if (Main.getMysqlConnection().getMysql().getDatabase(MysqlConnection.dbName).existsTable(MysqlConnection.tableName)) {
                Table table = Main.getMysqlConnection().getMysql().getDatabase(MysqlConnection.dbName).getTable(MysqlConnection.tableName);
                if (table.hasColumn(MysqlConnection.cmnGuildID)) {
                    if (table.getColumn(MysqlConnection.cmnGuildID).getAll().getAsString().contains(e.getGuild().getId())) {
                        String subName = e.getSubcommandName();
                        if (subName != null) {
                            switch (subName) {
                                case cmdSettingsSubSupport -> {
                                    TextChannel valueChannel = e.getOption(cmdSettingsSubSupportOptionChannel).getAsChannel().asTextChannel();
                                    if (valueChannel != null) {
                                        table.getRow(table.getColumn(MysqlConnection.cmnGuildID), e.getGuild().getId()).set(table.getColumn(MysqlConnection.cmnSupportChannelID), valueChannel.getId());
                                        e.getInteraction().reply("Der Support Channel wurde erfolgreich auf " + valueChannel.getAsMention() + " gesetzt!").queue();
                                    }
                                }
                                case cmdSettingsSubModerator -> {
                                    Role valueRole = e.getOption(cmdSettingsSubModeratorOptionRole).getAsRole();
                                    table.getRow(table.getColumn(MysqlConnection.cmnGuildID), e.getGuild().getId()).set(table.getColumn(MysqlConnection.cmnModeratorID), valueRole.getId());
                                    e.getInteraction().reply("Der Moderator Role wurde erfolgreich auf " + valueRole.getAsMention() + " gesetzt!").queue();
                                }
                            }
                        }
                    } else {
                        e.getInteraction().reply("ERROR: Bitte den Discord Bot neu einladen!").queue();
                        e.getGuild().leave().queue();
                        Sentry.captureMessage("Failed to find DISCORD GUILD ID in Command");
                    }
                } else {
                    Sentry.captureMessage("Failed to find COLUMN in Command");
                }
            } else {
                Sentry.captureMessage("Failed to find Table in Command");
            }
        } else {
            Sentry.captureMessage("Failed to find Database in Command");
        }
    }
}
