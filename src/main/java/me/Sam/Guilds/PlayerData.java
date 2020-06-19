package me.Sam.Guilds;

import java.util.UUID;

public class PlayerData {
	
	int mcmmolevel;
	int jobslevel;
	int timeplayedminutes;
	UUID player;
	
	
	public PlayerData(UUID playername, int mcmmolevel, int jobslevel, int timeplayedminutes) {
		this.mcmmolevel = mcmmolevel;
		this.jobslevel = jobslevel;
		this.timeplayedminutes = timeplayedminutes;
		this.player = playername;
	}
	
	public int getMcmmoLevel() {
		return this.mcmmolevel;
	}
	public int getJobsLevel() {
		return this.jobslevel;
	}
	public int getMinutesPlayed() {
		return this.timeplayedminutes;
	}
	public UUID getPlayerName() {
		return this.player;
	}
	public void setMcmmoLevel(int level) {
		this.mcmmolevel = level;
	}
	public void setJobsLevel(int level) {
		this.jobslevel = level;
	}
	public void setMinutesPlayed(int minutesplayed) {
		this.timeplayedminutes = minutesplayed;
	}
	public void setPlayerName(UUID name) {
		this.player = name;
	}

}
