package de.goldendeveloper.ticket.support;

import de.goldendeveloper.ticket.support.listener.Commands;
import de.goldendeveloper.ticket.support.listener.OnJoin;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.awt.*;

public class Discord {

    private JDA bot;

    public Discord(String token) {
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
                            new OnJoin()
                    )
                    .setAutoReconnect(true)
                    .build().awaitReady();
            bot.upsertCommand(Main.cmdSettings, "Stellt den Discord Ticket-Support Bot ein!").addSubcommands(
                            new SubcommandData(Main.cmdSettingsSubSupport, "Setze den Support Channel für das Ticket System").addOption(OptionType.CHANNEL, Main.cmdSettingsSubSupportOptionChannel, "Der Support Channel für das Ticket System", true),
                            new SubcommandData(Main.cmdSettingsSubModerator, "Setzt die Moderator Role für das Ticket System").addOption(OptionType.ROLE, Main.cmdSettingsSubModeratorOptionRole, "Die Moderatoren Rolle für das Ticket System", true)
                    ).queue();
            bot.upsertCommand(Main.cmdSupport, "Erstellt ein Support Ticket für dich!").addOption(OptionType.STRING, Main.cmdSupportOption, "Stell deine Frage", true).queue();
            bot.upsertCommand(Main.cmdHelp, "Zeigt dir eine Liste aller möglichen Befehle an!").queue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MessageEmbed getErrorEmbed(String ERROR, String onClass) {
        return new EmbedBuilder()
                .setTitle("**ERROR**")
                .setColor(Color.RED)
                .addField("Failed to find " + ERROR, "On " + onClass, false)
                .build();
    }

    public JDA getBot() {
        return bot;
    }
}
