package me.Sam.Guilds;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class Chat implements Listener{
	
	Guilds guilds;
	GuildManager gm = new GuildManager();
	public Chat(Guilds guilds) {
		this.guilds = guilds;
	}

	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		if(!Guilds.guildchatting.contains(p)) {
			return;
		}
		e.setCancelled(true);
		Guild playersguild = gm.getPlayerGuild(p.getUniqueId());
		playersguild.sendGuildChat(p, Utils.Chat("&7[&eGC&7] &e&l" + playersguild.getRank(p.getUniqueId()) + " &7" + p.getName() + " &3⋙&r " + e.getMessage()));
		sendCommandSpyMessage(p, playersguild, e.getMessage());
	}

	public void sendCommandSpyMessage(Player sender, Guild guild, String message) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (Guilds.guildspying.contains(p.getUniqueId())) {
				if (gm.isInGuild(guild, p.getUniqueId()) == false) {
					p.sendMessage(Utils.Chat("&7[&2&lGuild Spy&7] &f" + guild.getName() + " &2" + sender.getName() + " &7: &a" + message));
				}
			}
		}
	}
	
}
