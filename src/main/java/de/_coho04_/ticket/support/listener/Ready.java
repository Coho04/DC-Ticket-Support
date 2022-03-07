package de._coho04_.ticket.support.listener;

import de._coho04_.ticket.support.Main;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Ready extends ListenerAdapter {

    @Override
    public void onReady(ReadyEvent e) {
        Main.MysqlFirstStart("", "", "", 3306);
    }
}
