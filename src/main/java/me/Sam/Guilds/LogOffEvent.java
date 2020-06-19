package me.Sam.Guilds;

import java.io.IOException;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LogOffEvent implements Listener{
	
	Guilds guilds;
	GuildManager gm = new GuildManager();
	
	
	public LogOffEvent(Guilds guilds) {
		this.guilds = guilds;
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		
		Player p = e.getPlayer();
		if(Guilds.guildchatting.contains(p)) {
			Guilds.guildchatting.remove(p);
		}
		if(gm.hasGuild(p.getUniqueId()) == true) {
			Guild guild = gm.getPlayerGuild(p.getUniqueId());
			guild.getOnlineMembers().remove(p);
			if(guild.getOnlineJuniors().contains(p)) {
				guild.getOnlineJuniors().remove(p);
			}
		}
		ConfigurationSection players = guilds.playerstatsconfig.getConfigurationSection("Players");
		players.set(gm.getPlayerUUID(p.getName()).toString() + ".minutesplayed", this.guilds.getTimePlayedMinutes(p));
		players.set(gm.getPlayerUUID(p.getName()).toString() + ".mcmmopowerlevel", this.guilds.getMcmmoPowerLevel(p));
		players.set(gm.getPlayerUUID(p.getName()).toString() + ".jobspowerlevel", this.guilds.getJobsPowerLevel(p));
		try {
			guilds.playerstatsconfig.save(this.guilds.playerstatsfile);
			
		} catch(IOException exception) {
			exception.printStackTrace();
		}
	}
	
}
