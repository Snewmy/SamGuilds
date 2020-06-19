package me.Sam.Guilds;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import com.Zrips.CMI.CMI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.JobProgression;
import com.gmail.nossr50.api.ExperienceAPI;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.scheduler.BukkitScheduler;


public class Guilds extends JavaPlugin{
	
	public static Guilds instance;
	public static ArrayList<Guild> guilds = new ArrayList<Guild>();
	public File playerstatsfile = new File(getDataFolder(), "playerstats.yml");
	public FileConfiguration playerstatsconfig = YamlConfiguration.loadConfiguration(this.playerstatsfile);
	public File guildsfile = new File(getDataFolder(), "guilds.yml");
	public FileConfiguration guildsconfig = YamlConfiguration.loadConfiguration(this.guildsfile);
	public File guildspyersfile = new File(getDataFolder(), "guildspyers.yml");
	public FileConfiguration guildspyersconfig = YamlConfiguration.loadConfiguration(guildspyersfile);
	GuildManager gm;
	public static ArrayList<Player> guildchatting = new ArrayList<Player>();
	public final int mcmmoduration = 20 * 30 * 30;
	public TreeMap<Integer, Guild> guildranking;
	public FileConfiguration config = this.getConfig();
	public static Economy econ = null;
	public ArrayList<Inventory> inventories;
	public static ArrayList<PlayerData> playerdata = new ArrayList<PlayerData>();
	public static CMI cmi = null;
	public static ArrayList<Guild> jobsboosted = new ArrayList<>();
	public static ArrayList<Guild> mcmmoboosted = new ArrayList<>();
	public static ArrayList<UUID> guildspying = new ArrayList<>();
	
	@SuppressWarnings("deprecation")
	public void onEnable() {
		instance = this;
		if(hasCmi()){
			Guilds.cmi = (CMI)getServer().getPluginManager().getPlugin("CMI");
		}
		getServer().getLogger().info("Sams guild plugin enabled.");
		if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
		      new Placeholder(this).hook();
		    } else {
		      throw new RuntimeException("Could not find PlaceholderAPI!! Plugin can not work without it!");
		    }
		if (!setupEconomy())
	    {
	      Bukkit.getServer().getLogger().severe("Disabled due to no vault dependency found");
	      Bukkit.getServer().getPluginManager().disablePlugin(this);
	    }
		getServer().getPluginManager().registerEvents(new LogOffEvent(this), this);
		getServer().getPluginManager().registerEvents(new Chat(this), this);
		getServer().getPluginManager().registerEvents(new GUIListener(), this);
		getServer().getPluginManager().registerEvents(new JoinListener(), this);
		getServer().getPluginManager().registerEvents(new McmmoEvent(), this);
		getServer().getPluginManager().registerEvents(new JobsEvent(), this);
		getCommand("g").setExecutor(new ChatCommand());
		getCommand("guildspy").setExecutor(new GuildSpyCommand());
		configChecks();
		this.config.options().copyDefaults();
		saveDefaultConfig();
		loadPlayerData();
		loadGuilds();
		loadGuildSpyers();
		scheduler();
	}
	
	public void onDisable() {
		savePlayerData();
		saveGuildData();
		saveGuildSpyers();
	}
	 

	
	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("guild")) {

			if (sender instanceof Player) {
				GuildManager gm = new GuildManager();
				Player p = (Player) sender;
				if (args.length < 1) {
					if (gm.hasGuild(p.getUniqueId()) == false) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou do not have a guild. /Guild help"));
						return false;
					}

					Guild guild = gm.getPlayerGuild(p.getUniqueId());
					OfflinePlayer offlinep = Bukkit.getOfflinePlayer(guild.getGuildMaster());

					p.sendMessage(Utils.Chat("                          &7[&a&l" + guild.getName() + "&7]        "));
					p.sendMessage(Utils.Chat("&7[&3GuildRanking&7] &e⋙ &f" + getGuildRanking(guild) + "#"));
					p.sendMessage(Utils.Chat("&7[&3GuildMaster&7] &e⋙ &f&l" + offlinep.getName()));
					p.sendMessage(Utils.Chat("&7[&3GuildPoints&7] &e⋙ &f&l" + guild.getPoints() + " &7(Based off Mcmmo,Jobs,TimePlayed)"));
					p.sendMessage(Utils.Chat("&7[&aJobsBoostMinutes&7] &e⋙ &f&l" + (guild.getJobsboostleft() / 60) + " Minutes " + (guild.getJobsboostleft() % 60) + " Seconds"));
					p.sendMessage(Utils.Chat("&7[&cMcmmoBoostMinutes&7] &e⋙ &f&l" + (guild.getMcmmoboostleft() / 60) + " Minutes " + (guild.getMcmmoboostleft() % 60) + " Seconds"));
					p.sendMessage(Utils.Chat("&7[&3OnlineJuniors&7] &e⋙ " + convertPlayerArray(guild.getOnlineJuniors())));
					p.sendMessage(Utils.Chat("&7[&3OnlineMembers&7] &e⋙ " + convertPlayerArray(guild.getOnlineMembers())));
					p.sendMessage(Utils.Chat("&7[&3OfflineMembers&7] &e⋙ " + convertStringArray(guild.getOfflineMembers())));

					return false;
				}
				if (args[0].equalsIgnoreCase("create")) {
					if (args.length == 1) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cIncorrect syntax. /guild help"));
						return false;
					}
					String guildname = args[1];
					if (alreadyInGuild(p) == true) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou are already in a guild."));
						return false;
					}
					if (ColorStripped(guildname).length() > 16) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cThat name is too long! Max 16 characters."));
						return false;
					}
					if (nameTaken(guildname) == true) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cThis name has already been taken."));
						return false;
					}
					if (hasCreateMoney(p) == false) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou do not have the &a" + this.config.getInt("Guildprice") + "$ &crequired to create a guild."));
						return false;
					}

					Guild newguild = new Guild(p.getUniqueId(), guildname);
					Guilds.guilds.add(newguild);
					p.sendMessage(Utils.Chat("&7[&3Guilds&7] &aYour new guild &7" + newguild.getName() + " &ahas been created!"));
					econ.withdrawPlayer(p, this.config.getInt("Guildprice"));
				} else if (args[0].equalsIgnoreCase("invite")) {
					if (args.length == 1) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cIncorrect syntax. /guild help"));
						return false;
					}
					String playerinvited = args[1];
					if (gm.hasGuild(p.getUniqueId()) == false) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou are not in a guild."));
						return false;
					}
					Guild guild = gm.getPlayerGuild(p.getUniqueId());
					if (gm.hasKickPermission(gm.getPlayerGuild(p.getUniqueId()), p.getUniqueId()) == false) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou do not have permission to invite to the guild."));
						return false;
					}
					if (isValidPlayer(playerinvited) == false) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cThat player is not online or it is not a valid player."));
						return false;
					}
					Player receiver = Bukkit.getPlayer(playerinvited);
					guild.invitePlayer(receiver);
					receiver.sendMessage(Utils.Chat("&7[&3Guilds&7] &aYou have been invited to the &3" + guild.getName() + " &aguild by &3" + p.getName() + "&a."));
					receiver.sendMessage(Utils.Chat("&7[&3Guilds&7] &aPlease type &3/guild join " + guild.getName() + " &ato accept."));
					p.sendMessage(Utils.Chat("&7[&3Guilds&7] &aPlayer successfully invited."));
					gm.notifyPlayerInvite(guild, receiver, p);


				} else if (args[0].equalsIgnoreCase("join")) {
					if (args.length == 1) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cIncorrect syntax. /guild help"));
						return false;
					}
					String guildname = args[1];
					if (gm.hasGuild(p.getUniqueId()) == true) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou are already in a guild. Please leave it first."));
						return false;
					}
					if (isValidGuild(guildname) == false) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cThat is not a valid guild."));
						return false;
					}
					Guild guild = gm.getGuild(guildname);
					if (!guild.invited.contains(p)) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou have not been invited to this guild."));
						return false;
					}
					guild.invited.remove(p);
					guild.addMember(p.getUniqueId());
					gm.notifyPlayerJoin(guild, p);
					p.sendMessage(Utils.Chat("&7[&3Guilds&7] &aYou have joined the guild &3" + guild.getName() + "&a!"));

				} else if (args[0].equalsIgnoreCase("leave")) {
					if (gm.hasGuild(p.getUniqueId()) == false) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou are not in a guild."));
						return false;
					}
					if (gm.getPlayerGuild(p.getUniqueId()).getGuildMaster().equals(p.getUniqueId())) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou cant leave your own guild! /Guild disband"));
						return false;
					}
					p.sendMessage(Utils.Chat("&7[&3Guilds&7] &aYou have left the guild."));
					Guild guild = gm.getPlayerGuild(p.getUniqueId());
					guild.leave(p);


				} else if (args[0].equalsIgnoreCase("kick")) {
					if (args.length == 1) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cIncorrect syntax. /guild help"));
						return false;
					}
					if (gm.hasGuild(p.getUniqueId()) == false) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou are not in a guild."));
						return false;
					}
					if (gm.hasKickPermission(gm.getPlayerGuild(p.getUniqueId()), p.getUniqueId()) == false) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou do not have permission to kick from the guild."));
						return false;
					}
					String kickedplayer = args[1];
					OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(kickedplayer);
					if (gm.isInGuild(gm.getPlayerGuild(p.getUniqueId()), offlineplayer.getUniqueId()) == false) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cThat player is not in the guild."));
						return false;
					}

					Guild guild = gm.getPlayerGuild(p.getUniqueId());
					if (guild.isKickable(offlineplayer.getUniqueId()) == false) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou may not kick that player."));
					}
					guild.kick(offlineplayer.getUniqueId(), p);
					if (isValidPlayer(kickedplayer) == true) {
						Player kicked = Bukkit.getPlayer(kickedplayer);
						kicked.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou have been kicked from the guild."));
						Guilds.guildchatting.remove(kicked);
					}
				} else if (args[0].equalsIgnoreCase("promote")) {
					if (args.length == 1) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cIncorrect syntax. /guild help"));
						return false;
					}
					try {
						if (gm.hasGuild(p.getUniqueId()) == false) {
							p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou are not in a guild."));
							return false;
						}
						String promotedname = args[1];
						OfflinePlayer offlinep = Bukkit.getOfflinePlayer(promotedname);
						if (gm.isInGuild(gm.getPlayerGuild(p.getUniqueId()), offlinep.getUniqueId()) == false) {
							p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cThat player is not in the guild."));
							return false;
						}


						Guild guild = gm.getPlayerGuild(p.getUniqueId());
						if (!guild.getGuildMaster().equals(p.getUniqueId())) {
							p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou are not the guild master."));
							return false;
						}
						guild.promote(offlinep.getUniqueId());
					} catch (NullPointerException npe) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cThat player is not in your guild."));
					}


				} else if (args[0].equalsIgnoreCase("demote")) {
					if (args.length == 1) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cIncorrect syntax. /guild help"));
						return false;
					}
					if (gm.hasGuild(p.getUniqueId()) == false) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou are not in a guild."));
						return false;
					}
					try {
						String demotedname = args[1];
						OfflinePlayer offlinep = Bukkit.getOfflinePlayer(demotedname);
					} catch (NullPointerException e1){
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cThat is not a valid playername."));
					}
					Guild mastersguild = gm.getPlayerGuild(p.getUniqueId());
					String demotedname = args[1];
					OfflinePlayer offlinep = Bukkit.getOfflinePlayer(demotedname);
					if (gm.isInGuild(mastersguild, offlinep.getUniqueId()) == false) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cThat player is not in the guild."));
						return false;
					}

					Guild guild = gm.getPlayerGuild(p.getUniqueId());
					if (!guild.getGuildMaster().equals(p.getUniqueId())) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou are not the guild master."));
						return false;
					}
					guild.demote(offlinep.getUniqueId());
				} else if (args[0].equalsIgnoreCase("disband")) {
					if (gm.hasGuild(p.getUniqueId()) == false) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou are not in a guild."));
						return false;
					}
					Guild guild = gm.getPlayerGuild(p.getUniqueId());
					if (!guild.getGuildMaster().equals(p.getUniqueId())) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou are not the guild master."));
						return false;
					}
					guild.msgOnlinePlayers(Utils.Chat("&7[&3Guilds&7] &cYour guild has been disbanded by the guild master."));
					for (Player member : guild.getOnlineMembers()) {
						if (Guilds.guildchatting.contains(member)) {
							Guilds.guildchatting.remove(member);
						}
					}
					ConfigurationSection guilds = this.guildsconfig.getConfigurationSection("Guilds");
					guilds.set(guild.getName(), null);
					try {
						this.guildsconfig.save(this.guildsfile);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					Guilds.guilds.remove(guild);

				} else if (args[0].equalsIgnoreCase("help")) {
					p.sendMessage(Utils.Chat("             &7[&aGuild Help&7]"));
					p.sendMessage(Utils.Chat("&3/Guild &7Displays your guilds information"));
					p.sendMessage(Utils.Chat("&3/Guild create (guildname) &7Creates a guild &fCost: " + this.config.getInt("Guildprice")));
					p.sendMessage(Utils.Chat("&3/Guild disband &7Disbands your guild"));
					p.sendMessage(Utils.Chat("&3/Guild invite (playername) &7Invites someone to your guild. (Only Juniors and Master can)"));
					p.sendMessage(Utils.Chat("&3/Guild kick (playername) &7Kicks a player from the guild"));
					p.sendMessage(Utils.Chat("&3/Guild join (guildname) &7Joins the guild if you've been invited"));
					p.sendMessage(Utils.Chat("&3/Guild leave &7Leaves your guild"));
					p.sendMessage(Utils.Chat("&3/Guild promote (name) &7Promotes a member to Junior"));
					p.sendMessage(Utils.Chat("&3/Guild demote (name) &7Demotes a junior to member"));
					p.sendMessage(Utils.Chat("&3/Guild setleader (name) &7Transfer guild leadership"));
					p.sendMessage(Utils.Chat("&3/gchat &7Activate/Deactivate guild chat"));
					p.sendMessage(Utils.Chat("&3/guild rankings &7Opens the gui of guild rankings"));
					p.sendMessage(Utils.Chat("&3/Guild sethome &7Set the home for your guild"));
					p.sendMessage(Utils.Chat("&3/Guild home &7Teleport to guild home"));
					p.sendMessage(Utils.Chat("&3/Guild buymcmmoboost (minutes) &7Purchase a guild-wide mcmmo boost &fCost: " + getConfig().getInt("mcmmoppm") + "$ per min"));
					p.sendMessage(Utils.Chat("&3/Guild buyjobsboost (minutes) &7Purchase a guild-wide jobs boost &fCost: " + getConfig().getInt("jobsppm") + "$ per min"));
				} else if (args[0].equalsIgnoreCase("rankings")) {
					p.sendMessage(Utils.Chat("&7[&3Guilds&7] &aOpening rankings gui..."));
					openGUI(p);
				} else if (args[0].equalsIgnoreCase("sethome")) {
					if (gm.hasGuild(p.getUniqueId()) == false) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou are not in a guild."));
						return false;
					}
					Guild guild = gm.getPlayerGuild(p.getUniqueId());
					if (!guild.getGuildMaster().equals(p.getUniqueId())) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou are not the guild master."));
						return false;
					}
					guild.setHome(p.getLocation());
					guild.msgOnlinePlayers(Utils.Chat("&7[&3Guilds&7] &3The guild home has been set by the guild master."));

				} else if (args[0].equalsIgnoreCase("home")) {
					if (gm.hasGuild(p.getUniqueId()) == false) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou are not in a guild."));
						return false;
					}
					Guild guild = gm.getPlayerGuild(p.getUniqueId());
					if (guild.hasHome() == false) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cPlease ask your guildmaster to set a guild home."));
						return false;
					}
					if (hasCmi()) {
						Guilds.cmi.getPlayerManager().getUser(p).setTpLoc(p.getLocation());
					}
					p.sendMessage(Utils.Chat("&7[&3Guilds&7] &3Teleporting to guild home..."));
					p.teleport(guild.getHome());

				} else if (args[0].equalsIgnoreCase("buymcmmoboost")) {
					if (gm.hasGuild(p.getUniqueId()) == false) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou are not in a guild."));
						return false;
					}
					if(args.length < 2){
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou must specify a number of minutes to purchase."));
						return false;
					}
					try{
						int numberofmin = Integer.parseInt(args[1]);
						if(econ.getBalance(p) < (numberofmin * getConfig().getInt("mcmmoppm"))){
							p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou do not have the $" + (numberofmin * getConfig().getInt("mcmmoppm")) + " required to purchase this."));
							return false;
						}
						Guild guild = gm.getPlayerGuild(p.getUniqueId());
						econ.withdrawPlayer(p, numberofmin * getConfig().getInt("mcmmoppm"));
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &aYour purchase was successful."));
						guild.msgOnlinePlayers(Utils.Chat("&7[&3Guilds&7] &b" + p.getName() + " &3has just purchased a guild &bMcmmo Boost &3for &b" + numberofmin + " &3minutes!"));
						guild.setMcmmoboostleft(guild.getMcmmoboostleft() + (numberofmin * 60));
						if(!Guilds.mcmmoboosted.contains(guild)){
							Guilds.mcmmoboosted.add(guild);
						}
						guild.msgOnlinePlayers(Utils.Chat("&f&lTotal left: " + (guild.getMcmmoboostleft() / 60) + " Minutes " + (guild.getMcmmoboostleft()%60) + " Seconds"));
					} catch (NumberFormatException e1){
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cThat is not a valid number!"));
						e1.printStackTrace();
					}
				} else if (args[0].equalsIgnoreCase("buyjobsboost")) {
					if (gm.hasGuild(p.getUniqueId()) == false) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou are not in a guild."));
						return false;
					}
					if(args.length < 2){
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou must specify a number of minutes to purchase."));
						return false;
					}
					try{
						int numberofmin = Integer.parseInt(args[1]);
						if(econ.getBalance(p) < (numberofmin * getConfig().getInt("jobsppm"))){
							p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou do not have the $" + (numberofmin * getConfig().getInt("jobsppm")) + " required to purchase this."));
							return false;
						}
						Guild guild = gm.getPlayerGuild(p.getUniqueId());
						econ.withdrawPlayer(p, numberofmin * getConfig().getInt("jobsppm"));
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &aYour purchase was successful."));
						guild.msgOnlinePlayers(Utils.Chat("&7[&3Guilds&7] &b" + p.getName() + " &3has just purchased a guild &bJobs Boost &3for &b" + numberofmin + " &3minutes!"));
						guild.setJobsboostleft(guild.getJobsboostleft() + (numberofmin * 60));
						if(!Guilds.jobsboosted.contains(guild)){
							Guilds.jobsboosted.add(guild);
						}
						guild.msgOnlinePlayers(Utils.Chat("&f&lTotal left: " + (guild.getJobsboostleft() / 60) + " Minutes " + (guild.getJobsboostleft()%60) + " Seconds"));
					} catch (NumberFormatException e1){
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cThat is not a valid number!"));
						e1.printStackTrace();
					}
				} else if(args[0].equalsIgnoreCase("setleader")) {
					if (args.length == 1) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cIncorrect syntax. /guild help"));
						return false;
					}
					if (gm.hasGuild(p.getUniqueId()) == false) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou are not in a guild."));
						return false;
					}
					String leadername = args[1];
					OfflinePlayer offlinep = Bukkit.getOfflinePlayer(leadername);
					if (gm.isInGuild(gm.getPlayerGuild(p.getUniqueId()), offlinep.getUniqueId()) == false) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cThat player is not in the guild."));
						return false;
					}
					Guild guild = gm.getPlayerGuild(p.getUniqueId());
					if (!guild.getGuildMaster().equals(p.getUniqueId())) {
						p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou are not the guild master."));
						return false;
					}
					guild.setGuildmaster(offlinep.getUniqueId());
					guild.msgOnlinePlayers(Utils.Chat("&7[&3Guilds&7] &aYour new guild master is &3" + offlinep.getName() + "&a!"));
				} else {
					p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cInvalid arguments. /guild help"));
				}
			}
		} else if(cmd.getName().equalsIgnoreCase("gchat")) {
			GuildManager gm = new GuildManager();
			if(sender instanceof Player) {
				Player p = (Player) sender;
				if(gm.hasGuild(p.getUniqueId()) == false) {
					p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou are not in a guild."));
					return false;
				}
				if(Guilds.guildchatting.contains(p)) {
					p.sendMessage(Utils.Chat("&7[&3Guilds&7] &7You are now set to all chat."));
					Guilds.guildchatting.remove(p);
				} else {
					p.sendMessage(Utils.Chat("&7[&3Guilds&7] &7You are now set to guild-only chat."));
					Guilds.guildchatting.add(p);
				}
				
			}
		}
		return false;
	}
	
	public boolean nameTaken(String name) {
		for(Guild guild : Guilds.guilds) {
			String coloredname = Utils.Chat(guild.getName());
			String strippedname = ChatColor.stripColor(coloredname);
			String coloredsuggestedname = Utils.Chat(name);
			String strippedsuggestedname = ChatColor.stripColor(coloredsuggestedname);
			if(strippedsuggestedname.equalsIgnoreCase(strippedname)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean alreadyInGuild(Player p) {
		GuildManager gm = new GuildManager();
		for(Guild guild : Guilds.guilds) {
			if(gm.hasPlayer(guild, p.getUniqueId()) == true) {
				return true;
			}
		}
		return false;
	}
	public boolean isValidPlayer(String name) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(player.getName().equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isValidGuild(String guildname) {
		for(Guild guild : Guilds.guilds) {
			String coloredname = Utils.Chat(guild.getName());
			String strippedname = ChatColor.stripColor(coloredname);
			String coloredsuggestedname = Utils.Chat(guildname);
			String strippedsuggestedname = ChatColor.stripColor(coloredsuggestedname);
			if(strippedsuggestedname.equalsIgnoreCase(strippedname)) {
				return true;
			}
		}
		return false;
	}
	public Player convertToPlayer(String name) {
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(p.getName().equalsIgnoreCase(name)) {
				return p;
			}
		}
		return null;
		
	}
	public int getTimePlayedMinutes(Player p) {
		// divide by 20 to make seconds then divide by 60 to make minutes
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
	
	public String convertPlayerArray(ArrayList<Player> array) {
		StringBuilder sb = new StringBuilder();
		for(Player p : array) {
			if(isVanished(p) == true){
				//skip
			} else {
				sb.append(p.getName() + "&7, &3");
			}
		}
		String newstring = "&3" + sb.toString();
		return newstring;
	}
	public String convertStringArray(ArrayList<String> array) {
		StringBuilder sb = new StringBuilder();
		for(String s : array) {
			sb.append(s + "&7, &3");
		}
		String newstring = "&3" + sb.toString();
		return newstring;
	}
	
	public void updateRankings() {
		calculateAllGuildPoints();
		TreeMap<Integer, Guild> ranking = new TreeMap<Integer, Guild>(Collections.reverseOrder());
		for(Guild guild : Guilds.guilds) {
			ranking.put(guild.getPoints(), guild);
		}
		this.guildranking = ranking;
		
	}
	public void loadRankings() {
		TreeMap<Integer, Guild> ranking = new TreeMap<Integer, Guild>(Collections.reverseOrder());
		for(Guild guild : Guilds.guilds) {
			ranking.put(guild.getPoints(), guild);
		}
		this.guildranking = ranking;
		
	}
	
	
	
	public void RankingGUI(Player p) {
		calculateAllGuildPoints();
		updateRankings();
		Inventory gui = Bukkit.createInventory(null, 54, "Guild Rankings");
		for(Entry<Integer, Guild> entry : this.guildranking.entrySet()) {
			Integer guildpoints = entry.getKey();
			Guild guild = entry.getValue();
			ItemStack guildentry = new ItemStack(Material.CHEST, 1);
			ItemMeta im = guildentry.getItemMeta();
			ArrayList<String> lore = new ArrayList<String>();
			im.setDisplayName(Utils.Chat("&e" + getGuildRanking(guild) + "# &a" + guild.getName()));
			lore.add(" ");
			lore.add(Utils.Chat("&eGuild Master: &f" + guild.getGuildMaster()));
			lore.add(Utils.Chat("&eGuild Points: &f" + guildpoints));
			im.setLore(lore);
			guildentry.setItemMeta(im);
			guildentry.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
			
			gui.addItem(guildentry);
			
		}
		p.openInventory(gui);
		
		
	}
	
	public int getGuildRanking(Guild guild) {
		updateRankings();
		int i = 0;
		for(Entry<Integer, Guild> entry : this.guildranking.entrySet()) {
			i++;
			Guild guildentry = entry.getValue();
			if(guild.equals(guildentry)) {
				return i;
			}
		}
		return 0;
		
	}
	
	public void calculateAllGuildPoints() {
		GuildManager gm = new GuildManager();
		for(Guild guild : Guilds.guilds) {
			gm.updateData(guild);
			gm.calculatePoints(guild);
		}
	}
	
	public String ColorStripped(String string) {
		String convertcolors = Utils.Chat(string);
		String colorstripped = ChatColor.stripColor(convertcolors);
		return colorstripped;
		
	}
	static boolean setupEconomy()
	  {
	    if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
	      return false;
	    }
	    RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
	    if (rsp == null) {
	      return false;
	    }
	    econ = (Economy)rsp.getProvider();
	    return econ != null;
	  }
	
	public boolean hasCreateMoney(Player p) {
		if(econ.getBalance(p) >= this.config.getInt("Guildprice")) {
			return true;
		}
		return false;
	}
	public void openGUI(Player p) {
		updateRankings();
		ArrayList<Inventory> invs = new ArrayList<Inventory>();
		this.inventories = invs;
		ArrayList<Guild> guilds = new ArrayList<Guild>();
		for(Entry<Integer, Guild> entry : this.guildranking.entrySet()) {
			Guild guild = entry.getValue();
			guilds.add(guild);
		}
		int invsize = 21;
		int inventoriesneeded = ((int) Math.ceil((double)Guilds.guilds.size() / (double) invsize));
		if(inventoriesneeded == 0) {
			inventoriesneeded = 1;
		}
		for(int i = 0; i < inventoriesneeded; i++) {
			Inventory inv = Bukkit.createInventory(null, 45, "Guild Rankings");
			inventories.add(inv);
		}
		
		for(Inventory inv : inventories) {
			ItemStack pane = new ItemStack(Material.BROWN_STAINED_GLASS_PANE, 1);
			ItemMeta paneim = pane.getItemMeta();
			paneim.setDisplayName(" ");
			pane.setItemMeta(paneim);
			inv.setItem(0, pane);
			inv.setItem(1, pane);
			inv.setItem(2, pane);
			inv.setItem(3, pane);
			inv.setItem(4, pane);
			inv.setItem(5, pane);
			inv.setItem(6, pane);
			inv.setItem(7, pane);
			inv.setItem(8, pane);
			inv.setItem(9, pane);
			inv.setItem(17, pane);
			inv.setItem(18, pane);
			inv.setItem(26, pane);
			inv.setItem(27, pane);
			inv.setItem(35, pane);
			inv.setItem(37, pane);
			inv.setItem(39, pane);
			inv.setItem(40, pane);
			inv.setItem(41, pane);
			inv.setItem(43, pane);
			ItemStack backbutton = new ItemStack(Material.PAPER, 1);
			ItemMeta backmeta = backbutton.getItemMeta();
			backmeta.setDisplayName(Utils.Chat("&c&lPrevious Page"));
			backbutton.setItemMeta(backmeta);
			inv.setItem(38, backbutton);
			ItemStack nextbutton = new ItemStack(Material.PAPER, 1);
			ItemMeta nextmeta = nextbutton.getItemMeta();
			nextmeta.setDisplayName(Utils.Chat("&c&lNext Page"));
			nextbutton.setItemMeta(nextmeta);
			inv.setItem(42, nextbutton);
			ItemStack exitbutton = new ItemStack(Material.OAK_DOOR, 1);
			ItemMeta exitmeta = exitbutton.getItemMeta();
			exitmeta.setDisplayName(Utils.Chat("&a&lExit"));
			exitbutton.setItemMeta(exitmeta);
			inv.setItem(36, exitbutton);
			inv.setItem(44, exitbutton);
			Iterator<Guild> iterator = guilds.iterator();
			while(iterator.hasNext()) {
				Guild guild = iterator.next();
				if(inv.firstEmpty() != -1) {
					ItemStack guilditem = new ItemStack(Material.ENCHANTED_BOOK, 1);
					ItemMeta guildmeta = guilditem.getItemMeta();
					guildmeta.setDisplayName(Utils.Chat("&7[&3#" + Guilds.instance.getGuildRanking(guild) + "&7] &e" + guild.getName()));
					ArrayList<String> lore = new ArrayList<String>();
					OfflinePlayer offlinep = Bukkit.getOfflinePlayer(guild.getGuildMaster());
					lore.add(Utils.Chat("&eGuildMaster&7: &f" + offlinep.getName()));
					lore.add(Utils.Chat("&eGuildPoints&7: &f" + guild.getPoints()));
					guildmeta.setLore(lore);
					guilditem.setItemMeta(guildmeta);
					if(inv.contains(guilditem)) {
						inv.removeItem(guilditem);
						inv.setItem(inv.firstEmpty(), guilditem);
						iterator.remove();
					} else {
						inv.setItem(inv.firstEmpty(), guilditem);
						iterator.remove();
					}
				} 
			}
			
			
		}
		p.openInventory(inventories.get(0));
	}
	
	public void savePlayerData() {
		ConfigurationSection players = this.playerstatsconfig.getConfigurationSection("Players");
		for(PlayerData playerdata : Guilds.playerdata) {
			OfflinePlayer player = Bukkit.getOfflinePlayer(playerdata.getPlayerName());
			players.set(player.getUniqueId().toString() + ".minutesplayed", playerdata.getMinutesPlayed());
			players.set(player.getUniqueId().toString() + ".mcmmopowerlevel", playerdata.getMcmmoLevel());
			players.set(player.getUniqueId().toString() + ".jobspowerlevel", playerdata.getJobsLevel());
		}
		try {
			this.playerstatsconfig.save(this.playerstatsfile);
		} catch(IOException e1) {
			e1.printStackTrace();
		}
	}
	public void saveGuildData() {
			GuildManager gm = new GuildManager();
			ConfigurationSection guilds = this.guildsconfig.getConfigurationSection("Guilds");
			for (Guild guild : Guilds.guilds) {
				try {
				OfflinePlayer guildmaster = Bukkit.getOfflinePlayer(guild.getGuildMaster());
				guilds.set(guild.getName() + ".guildmaster", guildmaster.getUniqueId().toString());
				Set<String> juniors = new HashSet<String>();
				Set<String> members = new HashSet<String>();
				for (UUID juniorname : guild.getJuniors()) {
					juniors.add(juniorname.toString());


				}
				for (UUID membername : guild.getMembers()) {
					members.add(membername.toString());
				}

				guilds.set(guild.getName() + ".members", new ArrayList<>(members));
				guilds.set(guild.getName() + ".juniors", new ArrayList<>(juniors));
				if (gm.hasHome(guild) == true) {
					guilds.set(guild.getName() + ".location" + ".world", guild.getHome().getWorld().getName());
					guilds.set(guild.getName() + ".location" + ".x", guild.getHome().getX());
					guilds.set(guild.getName() + ".location" + ".y", guild.getHome().getY());
					guilds.set(guild.getName() + ".location" + ".z", guild.getHome().getZ());
				}
				guilds.set(guild.getName() + ".points", guild.getPoints());
				guilds.set(guild.getName() + ".mcmmoboostleft", guild.getMcmmoboostleft());
				guilds.set(guild.getName() + ".jobsboostleft", guild.getJobsboostleft());
				} catch (NullPointerException npe){
					getLogger().info("Skipping guild " + guild.getName() + "Error on saving");
				}
			}
		try {
			this.guildsconfig.save(this.guildsfile);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	public void configChecks() {
		if(!this.guildsconfig.isConfigurationSection("Guilds")) {
			this.guildsconfig.createSection("Guilds");
			try {
				this.guildsconfig.save(this.guildsfile);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		if(!playerstatsconfig.isConfigurationSection("Players")) {
			playerstatsconfig.createSection("Players");
			try {
				this.playerstatsconfig.save(this.playerstatsfile);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		if(!guildspyersconfig.isConfigurationSection("Data")){
			guildspyersconfig.createSection("Data");
		}
		try{
			guildspyersconfig.save(guildspyersfile);
		} catch (IOException e1){
			e1.printStackTrace();
		}
	}
	public void loadGuilds() {
		ConfigurationSection guilds = this.guildsconfig.getConfigurationSection("Guilds");
		for(String guildname : this.guildsconfig.getConfigurationSection("Guilds").getKeys(false)) {
			UUID guildmaster = UUID.fromString(guilds.getString(guildname + ".guildmaster"));
			Guild guild = new Guild(guildmaster, guildname);
			for(String member : guilds.getStringList(guild.getName() + ".members")) {
				if(!guildmaster.equals(UUID.fromString(member))) {
					if(UUID.fromString(member) != null) {
						guild.addMember(UUID.fromString(member));
					}
				}
			}
			for(String junior : guilds.getStringList(guild.getName() + ".juniors")) {
				if(UUID.fromString(junior) != null) {
					guild.addJunior(UUID.fromString(junior));
				}
			}
			if(guilds.getString(guildname + ".location" + ".world") != null) {
				World world = Bukkit.getWorld(guilds.getString(guildname + ".location" + ".world"));
				double x = guilds.getInt(guildname + ".location" + ".x");
				double y = guilds.getInt(guildname + ".location" + ".y");
				double z = guilds.getInt(guildname + ".location" + ".z");
				Location loc = new Location(world, x, y, z);
				guild.setHome(loc);
			}
			int points = guilds.getInt(guildname + ".points");
			guild.setPoints(points);
			guild.setMcmmoboostleft(guilds.getInt(guildname + ".mcmmoboostleft"));
			guild.setJobsboostleft(guilds.getInt(guildname + ".jobsboostleft"));
			if(guild.getMcmmoboostleft() > 0){
				Guilds.mcmmoboosted.add(guild);
				guild.mcmmoboostOn();
			}
			if(guild.getJobsboostleft() > 0){
				Guilds.jobsboosted.add(guild);
				guild.jobsboostOn();
			}
			Guilds.guilds.add(guild);
			
		}
		//old updaetrankings
	}
	public void loadPlayerData() {
		ConfigurationSection players = this.playerstatsconfig.getConfigurationSection("Players");
		for(String uuid : players.getKeys(false)) {
			if(UUID.fromString(uuid) != null) {
			int mcmmolevel = players.getInt(uuid + ".mcmmopowerlevel");
			int jobslevel = players.getInt(uuid + ".jobspowerlevel");
			int minutesplayed = players.getInt(uuid + ".minutesplayed");
				PlayerData playerdata = new PlayerData(UUID.fromString(uuid), mcmmolevel, jobslevel, minutesplayed);
				Guilds.playerdata.add(playerdata);
			}
		}
		
	}
	
	public String getNameFromUUID(UUID uuid) {
		OfflinePlayer offlinep = Bukkit.getOfflinePlayer(uuid);
		return offlinep.getName();
	}

	public boolean hasCmi(){
		if(this.getServer().getPluginManager().getPlugin("CMI") == null){
			return false;
		}
		return true;
	}

	public void scheduler(){
		BukkitScheduler scheduler = getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(this, new Runnable(){
			public void run(){
				Iterator<Guild> iterator1 = Guilds.mcmmoboosted.iterator();
				while(iterator1.hasNext()){
					Guild guild = iterator1.next();
					if(guild.getMcmmoboostleft() == 0){
						guild.msgOnlinePlayers(Utils.Chat("&7[&3Guilds&7] &cThe guild-wide mcmmo boost has just ended."));
						iterator1.remove();
					} else {
						guild.setMcmmoboostleft(guild.getMcmmoboostleft() - 1);
					}
				}
				Iterator<Guild> iterator2 = Guilds.jobsboosted.iterator();
				while(iterator2.hasNext()){
					Guild guild = iterator2.next();
					if(guild.getJobsboostleft() == 0){
						guild.msgOnlinePlayers(Utils.Chat("&7[&3Guilds&7] &cThe guild-wide jobs boost has just ended."));
						iterator2.remove();
					} else {
						guild.setJobsboostleft(guild.getJobsboostleft() - 1);
					}
				}
			}
		},0L, 20L);
	}
	private boolean isVanished(Player player) {
		for (MetadataValue meta : player.getMetadata("vanished")) {
			if (meta.asBoolean()) return true;
		}
		return false;
	}

	public void saveGuildSpyers(){
		Set<String> guildspyers = new HashSet<>();
		for(UUID player : Guilds.guildspying){
			guildspyers.add(player.toString());
		}
		guildspyersconfig.set("Data." + "Spyers", new ArrayList<>(guildspyers));
		try{
			guildspyersconfig.save(guildspyersfile);
		} catch (IOException e1){
			e1.printStackTrace();
		}
	}

	public void loadGuildSpyers(){
		for(String uuid : guildspyersconfig.getStringList("Data." + "Spyers")){
			Guilds.guildspying.add(UUID.fromString(uuid));
		}
	}
}
