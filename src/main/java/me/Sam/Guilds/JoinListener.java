package me.Sam.Guilds;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener{
	
	GuildManager gm = new GuildManager();
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if(gm.hasPlayerData(p.getUniqueId()) == true) {
			return;
		} else {
			PlayerData playerdata = new PlayerData(p.getUniqueId(), 0, 0, 0);
			Guilds.playerdata.add(playerdata);
		}
	}

}
