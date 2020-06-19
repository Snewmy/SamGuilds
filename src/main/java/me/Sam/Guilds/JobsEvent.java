package me.Sam.Guilds;

import com.gamingmesh.jobs.api.JobsPaymentEvent;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class JobsEvent implements Listener {

    GuildManager gm = new GuildManager();

    @EventHandler
    public void onjobsxp(JobsPaymentEvent e){
        OfflinePlayer p = e.getPlayer();
        if(gm.hasGuild(p.getUniqueId()) == false){
            return;
        }
        Guild guild = gm.getPlayerGuild(p.getUniqueId());
        if(Guilds.jobsboosted.contains(guild)){
            e.setAmount(e.getAmount() * 2);
        }
    }
}
