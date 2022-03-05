package de._coho04_.ticket.support;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Commands extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
        if (e.isFromGuild()) {
            String cmd = e.getName();
            if (cmd.equalsIgnoreCase(Main.cmdSupport)) {
                String question = e.getOption(Main.cmdSupportOption).getAsString();
                Main.getBot().getTextChannelById("949715424898056322").sendMessage(question).queue(msg->{
                    msg.createThreadChannel("Ticket Support | " + e.getMember().getUser().getName()).queue(channel->{
                        channel.addThreadMember(e.getUser()).queue();
                        channel.getManager().setLocked(true).queue();
                    });
                });
            } else if (cmd.equalsIgnoreCase(Main.cmdHelp)) {

            }
        } else {
            e.getInteraction().reply("Dieser Command ist nur auf einem Server verfügbar!").queue();
        }
    }
}
