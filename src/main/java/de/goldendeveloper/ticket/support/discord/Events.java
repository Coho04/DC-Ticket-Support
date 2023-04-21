package de.goldendeveloper.ticket.support.discord;

import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import de.goldendeveloper.mysql.entities.Database;
import de.goldendeveloper.mysql.entities.RowBuilder;
import de.goldendeveloper.mysql.entities.SearchResult;
import de.goldendeveloper.mysql.entities.Table;
import de.goldendeveloper.ticket.support.MysqlConnection;
import de.goldendeveloper.ticket.support.Main;
import de.goldendeveloper.ticket.support.ServerCommunicator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Events extends ListenerAdapter {

    @Override
    public void onGuildJoin(GuildJoinEvent e) {
        Main.getServerCommunicator().addServer(e.getGuild().getId());
        e.getJDA().getPresence().setActivity(Activity.playing("/help | " + e.getJDA().getGuilds().size() + " Servern"));
        if (Main.getMysqlConnection().getMysql().existsDatabase(MysqlConnection.dbName)) {
            Database db = Main.getMysqlConnection().getMysql().getDatabase(MysqlConnection.dbName);
            if (db.existsTable(MysqlConnection.tableName)) {
                Table table = db.getTable(MysqlConnection.tableName);
                List<String> guilds = table.getColumn(MysqlConnection.cmnGuildID).getAll().getAsString();
                if (!guilds.contains(e.getGuild().getId())) {
                    table.insert(new RowBuilder()
                            .with(table.getColumn(MysqlConnection.cmnGuildID), e.getGuild().getId())
                            .with(table.getColumn(MysqlConnection.cmnModeratorID), "null")
                            .with(table.getColumn(MysqlConnection.cmnOwnerID), e.getGuild().getOwnerId())
                            .with(table.getColumn(MysqlConnection.cmnSupportChannelID), "null")
                            .build()
                    );
                }
            } else {
                Main.getDiscord().sendErrorMessage("Failed to find Table in onJoin");
            }
        } else {
            Main.getDiscord().sendErrorMessage("Failed to find Database in onJoin");
        }
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent e) {
        Main.getServerCommunicator().removeServer(e.getGuild().getId());
        e.getJDA().getPresence().setActivity(Activity.playing("/help | " + e.getJDA().getGuilds().size() + " Servern"));
    }


    private static final String closeTicketEmoji = "\uD83D\uDD10";

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
        if (e.isFromGuild()) {
            String cmd = e.getName();
            if (cmd.equalsIgnoreCase(Discord.cmdSupport)) {
                e.getInteraction().reply(createSupportTicket(e, e.getUser())).queue();
            } else if (cmd.equalsIgnoreCase(Discord.cmdHelp)) {
                e.getInteraction().replyEmbeds(createHelpMessage()).queue();
            } else if (cmd.equalsIgnoreCase(Discord.cmdSettings)) {
                cmdSettings(e);
            }
        } else {
            e.getInteraction().reply("Dieser Command ist nur auf einem Server verfügbar!").queue();
        }
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent e) {
        if (e.isFromGuild()) {
            if (e.isFromThread()) {
                if (!e.getUser().isBot()) {
                    RichCustomEmoji emote = e.getJDA().getEmojiById("957226297556336670");
                    if (e.getReaction().getEmoji().asCustom().getImageUrl().equals(emote.getImageUrl())) {
                        e.getChannel().asThreadChannel().delete().queue();
                    }
                    if (e.getEmoji().equals(Emoji.fromUnicode(closeTicketEmoji))) {
                        e.getChannel().asThreadChannel().getManager().setArchived(true).queue();
                    }
                }
            }
        }
    }

    @Override
    public void onShutdown(@NotNull ShutdownEvent e) {
        if (Main.getDeployment()) {
            WebhookEmbedBuilder embed = new WebhookEmbedBuilder();
            embed.setAuthor(new WebhookEmbed.EmbedAuthor(Main.getDiscord().getBot().getSelfUser().getName(), Main.getDiscord().getBot().getSelfUser().getAvatarUrl(), "https://Golden-Developer.de"));
            embed.addField(new WebhookEmbed.EmbedField(false, "[Status]", "Offline"));
            embed.addField(new WebhookEmbed.EmbedField(false, "Gestoppt als", Main.getDiscord().getBot().getSelfUser().getName()));
            embed.addField(new WebhookEmbed.EmbedField(false, "Server", Integer.toString(Main.getDiscord().getBot().getGuilds().size())));
            embed.addField(new WebhookEmbed.EmbedField(false, "Status", "\uD83D\uDD34 Offline"));
            embed.addField(new WebhookEmbed.EmbedField(false, "Version", Main.getConfig().getProjektVersion()));
            embed.setFooter(new WebhookEmbed.EmbedFooter("@Golden-Developer", Main.getDiscord().getBot().getSelfUser().getAvatarUrl()));
            embed.setTimestamp(new Date().toInstant());
            embed.setColor(0xFF0000);
            new WebhookClientBuilder(Main.getConfig().getDiscordWebhook()).build().send(embed.build()).thenRun(() -> System.exit(0));
        }
    }

    private static String createSupportTicket(SlashCommandInteractionEvent e, User user) {
        if (Main.getMysqlConnection().getMysql().existsDatabase(MysqlConnection.dbName)) {
            Database db = Main.getMysqlConnection().getMysql().getDatabase(MysqlConnection.dbName);
            if (db.existsTable(MysqlConnection.tableName)) {
                Table table = db.getTable(MysqlConnection.tableName);
                if (table.hasColumn(MysqlConnection.cmnModeratorID)) {
                    if (table.hasColumn(MysqlConnection.cmnGuildID)) {
                        if (table.getColumn(MysqlConnection.cmnGuildID).getAll().getAsString().contains(e.getGuild().getId())) {
                            HashMap<String, SearchResult> row = table.getRow(table.getColumn(MysqlConnection.cmnGuildID), e.getGuild().getId()).getData();
                            String roleID = row.get(MysqlConnection.cmnModeratorID).getAsString();
                            Role role = e.getJDA().getRoleById(roleID);
                            Emoji emote = e.getJDA().getEmojiById("957226297556336670");
                            if (role != null && emote != null) {
                                EmbedBuilder embed = new EmbedBuilder();
                                embed.setTitle("**Support**");
                                embed.setColor(Color.GREEN);
                                embed.setFooter("Golden-Developer", e.getJDA().getSelfUser().getAvatarUrl());
                                embed.setThumbnail(e.getUser().getAvatarUrl());
                                embed.setTimestamp(LocalDateTime.now());
                                embed.addField("Ticket Support | " + user.getName(), "Offene Frage: " + e.getOption(Discord.cmdSupportOption).getAsString(), true);
                                embed.addField("", emote.getAsReactionCode() + " Zum löschen des Tickets", false);
                                embed.addField("", ":closed_lock_with_key:  Zum Archivieren des Tickets", false);
                                embed.addField("", role.getAsMention(), true);

                                TextChannel SupportChannel = e.getJDA().getTextChannelById(row.get(MysqlConnection.cmnSupportChannelID).getAsLong());
                                if (SupportChannel != null) {
                                    SupportChannel.sendMessageEmbeds(embed.build()).queue();
                                    if (e.getGuild().getThreadChannelsByName("Ticket Support | " + user.getName(), true).isEmpty()) {
                                        SupportChannel.createThreadChannel("Ticket Support | " + user.getName()).queue(channel -> {
                                            channel.addThreadMember(user).queue();
                                            channel.getManager().setLocked(true).queue();
                                            channel.sendMessageEmbeds(embed.build()).queue(message -> {
                                                message.addReaction(Emoji.fromUnicode(closeTicketEmoji)).queue();
                                                message.addReaction(emote).queue();
                                            });
                                        });
                                    } else {
                                        ThreadChannel channelSupport = e.getGuild().getThreadChannelsByName("Ticket Support | " + user.getName(), true).get(0);
                                        if (channelSupport != null) {
                                            channelSupport.getManager().setArchived(false).queue();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return "Dein Support Ticket wurde erstellt!";
    }

    private static MessageEmbed createHelpMessage() {
        EmbedBuilder embed = new EmbedBuilder().setTitle("**Help**");
        for (Command command : Main.getDiscord().getBot().retrieveCommands().complete()) {
            embed.addField(command.getName(), command.getDescription(), false);
        }
        return embed.build();
    }

    private static void cmdSettings(SlashCommandInteractionEvent e) {
        if (Main.getMysqlConnection().getMysql().existsDatabase(MysqlConnection.dbName)) {
            if (Main.getMysqlConnection().getMysql().getDatabase(MysqlConnection.dbName).existsTable(MysqlConnection.tableName)) {
                Table table = Main.getMysqlConnection().getMysql().getDatabase(MysqlConnection.dbName).getTable(MysqlConnection.tableName);
                if (table.hasColumn(MysqlConnection.cmnGuildID)) {
                    if (table.getColumn(MysqlConnection.cmnGuildID).getAll().getAsString().contains(e.getGuild().getId())) {
                        String subName = e.getSubcommandName();
                        if (subName != null) {
                            switch (subName) {
                                case "support" -> {
                                    TextChannel valueChannel = e.getOption(Discord.cmdSettingsSubSupportOptionChannel).getAsChannel().asTextChannel();
                                    if (valueChannel != null) {
                                        table.getRow(table.getColumn(MysqlConnection.cmnGuildID), e.getGuild().getId()).set(table.getColumn(MysqlConnection.cmnSupportChannelID), valueChannel.getId());
                                        e.getInteraction().reply("Der Support Channel wurde erfolgreich auf " + valueChannel.getAsMention() + " gesetzt!").queue();
                                    }
                                }
                                case "moderator" -> {
                                    Role valueRole = e.getOption(Discord.cmdSettingsSubModeratorOptionRole).getAsRole();
                                    table.getRow(table.getColumn(MysqlConnection.cmnGuildID), e.getGuild().getId()).set(table.getColumn(MysqlConnection.cmnModeratorID), valueRole.getId());
                                    e.getInteraction().reply("Der Moderator Role wurde erfolgreich auf " + valueRole.getAsMention() + " gesetzt!").queue();
                                }
                            }
                        }
                    } else {
                        e.getInteraction().reply("ERROR: Bitte den Discord Bot neu einladen!").queue();
                        e.getGuild().leave().queue();
                        Main.getDiscord().sendErrorMessage("Failed to find DISCORD GUILD ID in Command");
                    }
                } else {
                    Main.getDiscord().sendErrorMessage("Failed to find COLUMN in Command");
                }
            } else {
                Main.getDiscord().sendErrorMessage("Failed to find Table in Command");
            }
        } else {
            Main.getDiscord().sendErrorMessage("Failed to find Database on Commands");
        }
    }
}
