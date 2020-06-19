package me.Sam.Guilds;

import org.bukkit.entity.Player;

import me.clip.placeholderapi.external.EZPlaceholderHook;

@SuppressWarnings("deprecation")
public class Placeholder
  extends EZPlaceholderHook
{
	GuildManager gm = new GuildManager();
  
  public Placeholder(Guilds guilds)
  {
    super(guilds, "SamGuilds");
  }
  
  public String onPlaceholderRequest(Player p, String id)
  {
    if (p == null) {
      return "";
    }
    if (id.equalsIgnoreCase("guildname")) {
    	if(gm.hasGuild(p.getUniqueId()) == false) {
    		return "";
    	}
    	
    		return gm.getGuildRankTag(gm.getPlayerGuild(p.getUniqueId()), p.getUniqueId()) + gm.getPlayerGuild(p.getUniqueId()).getName() + " ";
    	
      
    }
    return null;
  }
}
