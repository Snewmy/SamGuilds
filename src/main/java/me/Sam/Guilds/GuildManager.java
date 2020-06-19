package me.Sam.Guilds;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class GuildManager {
	
	
	FileConfiguration playerstats = Guilds.instance.playerstatsconfig;
	
	public GuildManager() {
		
	}
	
	public Guild getPlayerGuild(UUID playername) {
		for(Guild guild : Guilds.guilds) {
			if(hasPlayer(guild, playername) == true) {
				return guild;
			}
		}
		
		return null;
		
		
	}

	public boolean hasPlayer(Guild guild, UUID playername) {
		for(UUID membername : guild.getMembers()) {
			if(membername.equals(playername)) {
				return true;
			}
		}
		for(UUID juniorname : guild.getJuniors()) {
			if(juniorname.equals(playername)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isJunior(Guild guild, UUID playername) {
		for(UUID juniorname : guild.getJuniors()) {
			if(juniorname.equals(playername)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasGuild(UUID playername) {
		for(Guild guild : Guilds.guilds) {
			if(hasPlayer(guild, playername) == true) {
				return true;
			}
		}
		return false;
	}
	
	public void notifyPlayerInvite(Guild guild, Player invited, Player inviter) {
		for(Player online : guild.getAllOnline()) {
			online.sendMessage(Utils.Chat("&7[&3Guilds&7] &3" + invited.getName() + " &ahas been invited to the guild by &3" + inviter.getName() + "&a."));
		}
	}
	
	public Guild getGuild(String guildname) {
		for(Guild guild : Guilds.guilds) {
			String coloredname = Utils.Chat(guild.getName());
			String strippedguildname = ChatColor.stripColor(coloredname);
			String coloredsuggestedname = Utils.Chat(guildname);
			String strippedsuggestedname = ChatColor.stripColor(coloredsuggestedname);
			if(strippedguildname.equalsIgnoreCase(strippedsuggestedname)) {
				return guild;
			}
		}
		return null;
	}
	public void notifyPlayerJoin(Guild guild, Player joiner) {
		for(Player online : guild.getAllOnline()) {
			online.sendMessage(Utils.Chat("&7[&3Guilds&7] &3" + joiner.getName() + " &ahas joined the guild!"));
		}
	}
	
	public boolean hasKickPermission(Guild guild, UUID playername) {
		if(guild.getGuildMaster().equals(playername)) {
			return true;
		}
		for(UUID juniorname : guild.getJuniors()) {
			if(juniorname.equals(playername)) {
				return true;
			}
		}
		return false;
	}
	public boolean isInGuild(Guild guild, UUID playername) {
		
		for(UUID membername : guild.getMembers()) {
			if(membername.equals(playername)) {
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("unused")
	public int getMemberAmount(Guild guild) {
		int memberamount = 0;
		for(UUID member : guild.getMembers()) {
			memberamount++;
		}
		return memberamount;
	}
	
	public void updateData(Guild guild) {
		if(guild.getAllOnline().isEmpty() == true) {
			return;
		}
		for(Player player : guild.getAllOnline()) {
				PlayerData playerdata = getPlayersData(player.getUniqueId());
				playerdata.setJobsLevel(Guilds.instance.getJobsPowerLevel(player));
				playerdata.setMcmmoLevel(Guilds.instance.getMcmmoPowerLevel(player));
				playerdata.setMinutesPlayed(Guilds.instance.getTimePlayedMinutes(player));	
		}
	}
	
	public boolean hasPlayerData(UUID name) {
		for(PlayerData playerdata : Guilds.playerdata) {
			if(playerdata.getPlayerName().equals(name)) {
				return true;
			}
		}
		return false;
	}
	public PlayerData getPlayersData(UUID name) {
		for(PlayerData playerdata : Guilds.playerdata) {
			if(playerdata.getPlayerName().equals(name)) {
				return playerdata;
			}
		}
		return null;
	}
	
	public void calculatePoints(Guild guild) {
		
		int totalmcmmo = 0;
		int totaljobs = 0;
		int totalplaytimeminutes = 0;
		for(UUID member : guild.getMembers()) {
			if(hasPlayerData(member) == true) {
				PlayerData playerdata = getPlayersData(member);
				totalmcmmo = totalmcmmo + playerdata.getMcmmoLevel();
				totaljobs = totaljobs + playerdata.getJobsLevel();
				totalplaytimeminutes = totalplaytimeminutes + playerdata.getMinutesPlayed();
			} else {
				PlayerData playerdata = new PlayerData(member, 0, 0, 0);
				Guilds.playerdata.add(playerdata);
			}
		}
		for(UUID junior : guild.getJuniors()) {
			if(hasPlayerData(junior) == true) {
				PlayerData playerdata = getPlayersData(junior);
				totalmcmmo = totalmcmmo + playerdata.getMcmmoLevel();
				totaljobs = totaljobs + playerdata.getJobsLevel();
				totalplaytimeminutes = totalplaytimeminutes + playerdata.getMinutesPlayed();
			} else {
				PlayerData playerdata = new PlayerData(junior, 0, 0, 0);
				Guilds.playerdata.add(playerdata);
			}
		}
		if(hasPlayerData(guild.getGuildMaster()) == true){
			PlayerData playerdata = getPlayersData(guild.getGuildMaster());
			totalmcmmo = totalmcmmo + playerdata.getMcmmoLevel();
			totaljobs = totaljobs + playerdata.getJobsLevel();
			totalplaytimeminutes = totalplaytimeminutes + playerdata.getMinutesPlayed();
		} else {
			PlayerData playerdata = new PlayerData(guild.getGuildMaster(), 0, 0, 0);
			Guilds.playerdata.add(playerdata);
		}
		int points = totaljobs + (totalmcmmo / 100) + (totalplaytimeminutes / 100);
		guild.setPoints(points);
	}

	
	@SuppressWarnings("deprecation")
	public UUID getPlayerUUID(String playername) {
		OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(playername);
		return offlineplayer.getUniqueId();
	}
	
	public boolean hasHome(Guild guild) {
		if(guild.getHome() == null) {
			return false;
		}
		return true;
	}
	public String getGuildRankTag(Guild guild, UUID player){
		if(guild.getGuildMaster().equals(player)){
			return "**";
		}
		for(UUID junior : guild.getJuniors()){
			if(junior.equals(player)){
				return "*";
			}
		}
		return "";
	}
}
