package me.Sam.Guilds;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.JobProgression;
import com.gmail.nossr50.api.ExperienceAPI;

public class Guild {
	

	String guildname;
	UUID guildmaster;
	int guildpoints;
	Location home;
	ArrayList<UUID> members;
	ArrayList<Player> invited;
	ArrayList<UUID> juniors;
	boolean mcmmoboost;
	boolean jobsboost;
	int mcmmoboostleft;
	int jobsboostleft;

	public Guild(UUID guildmaster, String guildname) {
		this.guildmaster = guildmaster;
		this.guildname = guildname;
		ArrayList<UUID> members = new ArrayList<UUID>();
		ArrayList<Player> invited = new ArrayList<Player>();
		ArrayList<UUID> juniors = new ArrayList<UUID>();
		members.add(guildmaster);
		this.members = members;
		this.invited = invited;
		this.juniors = juniors;
		this.guildpoints = 0;
		this.mcmmoboost = false;
		this.jobsboost = false;
	}


	public int getTimePlayedMinutes(Player p) {
		return p.getTicksLived() / 20 / 60;
	}

	public int getMcmmoPowerLevel(Player p) {
		return ExperienceAPI.getPowerLevel(p);
	}

	public int getJobsPowerLevel(Player p) {
		int i = 0;
		List<JobProgression> jobs = Jobs.getPlayerManager().getJobsPlayer(p).getJobProgression();
		for (JobProgression OneJob : jobs) {
			i += OneJob.getLevel();
		}
		return i;
	}

	public int getPoints() {
		return this.guildpoints;
	}

	public ArrayList<UUID> getMembers() {
		return this.members;
	}

	public String getName() {
		return this.guildname;

	}
	
	public Location getHome() {
		return this.home;
	}
	
	public UUID getGuildMaster() {
		return this.guildmaster;
	}
	

	public void addMember(UUID playername) {
		this.members.add(playername);
	}
	
	public ArrayList<Player> getInvited(){
		return this.invited;
	}
	public void invitePlayer(Player p) {
		this.invited.add(p);
	}
	public ArrayList<UUID> getJuniors(){
		return this.juniors;
	}
	public void addJunior(UUID playername) {
		this.juniors.add(playername);
	}
	public ArrayList<Player> getOnlineMembers(){
		ArrayList<Player> onlinemembers = new ArrayList<Player>();
		for(UUID member : this.members) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				if(!onlinemembers.contains(p)) {
				  if(p.getUniqueId().equals(member)) {
					  onlinemembers.add(p);
				}
				}
			}
		}
		return onlinemembers;
	}
	public ArrayList<Player> getOnlineJuniors(){
		ArrayList<Player> onlinejuniors = new ArrayList<Player>();
		for(UUID junior : this.juniors) {
			for(Player p : Bukkit.getOnlinePlayers()) {
				if(!onlinejuniors.contains(p)) {
					  if(p.getUniqueId().equals(junior)) {
						  onlinejuniors.add(p);
					}
					}
			}
		}
		return onlinejuniors;
	}
	public void leave(Player p) {
		if(this.juniors.contains(p.getUniqueId())) {
			this.juniors.remove(p.getUniqueId());
		}
		if(this.members.contains(p.getUniqueId())) {
			this.members.remove(p.getUniqueId());
		}
		for(Player members : getAllOnline()) {
			members.sendMessage(Utils.Chat("&7[&3Guilds&7] &3" + p.getName() + " &chas left the guild."));
		}
		
	}
	public void kick(UUID kickedplayername, Player kicker) {
		if(this.juniors.contains(kickedplayername)) {
			this.juniors.remove(kickedplayername);
		}
		if(this.members.contains(kickedplayername)) {
			this.members.remove(kickedplayername);
		}
		for(Player members : getAllOnline()) {
			OfflinePlayer offlinep = Bukkit.getOfflinePlayer(kickedplayername);
			members.sendMessage(Utils.Chat("&7[&3Guilds&7] &3" + offlinep.getName() + " &chas been kicked from the guild by &3" + kicker.getName()));
		}
		
		
	}
	
	public void promote(UUID membername) {
		if(this.guildmaster.equals(membername)) {
			Player guildmaster = Bukkit.getPlayer(this.guildmaster);
			guildmaster.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou cannot promote the guild master!"));
			return;
		}
		if(this.juniors.contains(membername)) {
			Player guildmaster = Bukkit.getPlayer(this.guildmaster);
			guildmaster.sendMessage(Utils.Chat("&7[&3Guilds&7] &cThat player is already a junior."));
			return;
		}
		this.juniors.add(membername);
		this.members.remove(membername);
		for(Player member : getAllOnline()) {
			OfflinePlayer player = Bukkit.getOfflinePlayer(membername);
			member.sendMessage(Utils.Chat("&7[&3Guilds&7] &3" + player.getName() + " &ahas been promoted to Junior!"));
		}
	}
	public void demote(UUID juniorname) {
		if(!this.juniors.contains(juniorname)) {
			Player guildmaster = Bukkit.getPlayer(this.guildmaster);
			guildmaster.sendMessage(Utils.Chat("&7[&3Guilds&7] &cThat player is not a junior."));
			return;
		}
		if(this.guildmaster.equals(juniorname)) {
			Player guildmaster = Bukkit.getPlayer(this.guildmaster);
			guildmaster.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou cannot demote the guild master!"));
			return;
		}
		this.juniors.remove(juniorname);
		this.members.add(juniorname);
		for(Player member : getAllOnline()) {
			OfflinePlayer player = Bukkit.getOfflinePlayer(juniorname);
			member.sendMessage(Utils.Chat("&7[&3Guilds&7] &3" +  player.getName() + " &chas been demoted to member."));
		}
	}
	public void msgOnlinePlayers(String message) {
		for(Player member : getAllOnline()) {

			member.sendMessage(message);
		}
	}

	public void sendGuildChat(Player messagefrom, String message){
		for(Player member : getAllOnline()){
			if(Guilds.instance.hasCmi() == true){
				if(Guilds.instance.cmi.getPlayerManager().getUser(member).isIgnoring(messagefrom.getUniqueId()) == false) {
					member.sendMessage(message);
				}
			} else {
				member.sendMessage(message);
			}
		}
	}
	
	public void setPoints(int points) {
		this.guildpoints = points;
	}
	
	public void setHome(Location loc) {
		this.home = loc;
	}
	
	public String getRank(UUID playername) {
		if(this.guildmaster.equals(playername)) {
			return "Master";
		}
		for(UUID junior : this.juniors) {
			if(junior.equals(playername)) {
				return "Junior";
			}
		}
		return "Member";
	}
	public boolean isKickable(UUID playername) {
		if(this.guildmaster.equals(playername)) {
			return false;
		}
		for(UUID junior : this.juniors) {
			if(junior.equals(playername)) {
				return false;
			}
		}
		return true;
	}
	
	public ArrayList<String> getOfflineMembers(){
		ArrayList<String> offlinemembers = new ArrayList<String>();
		for(UUID member : this.members) {
			OfflinePlayer offlinep = Bukkit.getOfflinePlayer(member);
			offlinemembers.add(offlinep.getName());
			for(Player p : this.getOnlineMembers()) {
				offlinemembers.remove(p.getName());
			}
		}
		for(UUID junior : this.juniors) {
			OfflinePlayer offlinep = Bukkit.getOfflinePlayer(junior);
			offlinemembers.add(offlinep.getName());
			for(Player p : this.getOnlineJuniors()) {
				offlinemembers.remove(p.getName());
			}
		}
		return offlinemembers;
	}
	
	public ArrayList<Player> getAllOnline(){
		ArrayList<Player> allonline = new ArrayList<Player>();
		for(Player p : this.getOnlineJuniors()) {
			allonline.add(p);
		}
		for(Player p : this.getOnlineMembers()) {
			allonline.add(p);
		}
		return allonline;
	}
	
	public boolean hasHome() {
		if(this.home == null) {
			return false;
		}
		return true;
	}
	public boolean isMcmmoboost(){
		return this.mcmmoboost;
	}
	public boolean isJobsboost(){
		return this.jobsboost;
	}
	public void mcmmoboostOff(){
		this.mcmmoboost = false;
	}
	public void jobsboostOff(){
		this.jobsboost = false;
	}
	public void mcmmoboostOn(){
		this.mcmmoboost = true;
	}
	public void jobsboostOn(){
		this.jobsboost = true;
	}
	public int getMcmmoboostleft(){
		return this.mcmmoboostleft;
	}
	public int getJobsboostleft(){
		return this.jobsboostleft;
	}
	public void setJobsboostleft(int i){
		this.jobsboostleft = i;
	}
	public void setMcmmoboostleft(int i){
		this.mcmmoboostleft = i;
	}
	public void setGuildmaster(UUID player){
		this.guildmaster = player;
	}
}
