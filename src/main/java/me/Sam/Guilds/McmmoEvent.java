package me.Sam.Guilds;

import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class McmmoEvent implements Listener {

    GuildManager gm = new GuildManager();

    @EventHandler
    public void onxp(McMMOPlayerXpGainEvent e){
        Player p = e.getPlayer();
        if(gm.hasGuild(p.getUniqueId()) == false){
            return;
        }
        Guild guild = gm.getPlayerGuild(p.getUniqueId());
        if(Guilds.mcmmoboosted.contains(guild)){
            e.setXpGained(e.getXpGained() * 2);
        }
    }
}
