package me.Sam.Guilds;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatCommand implements CommandExecutor {

    GuildManager gm = new GuildManager();


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
        if (cmd.getName().equalsIgnoreCase("g")) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if (Guilds.cmi.getPlayerManager().getUser(p).isSilenceMode() == true) {
                    p.sendMessage(Utils.Chat("&cYou are muted!"));
                    return false;
                }
                if (args.length < 1) {
                    p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cInvalid usage. /g (message)"));
                    return false;
                }
                if (gm.hasGuild(p.getUniqueId()) == false) {
                    p.sendMessage(Utils.Chat("&7[&3Guilds&7] &cYou do not have a guild!"));
                    return false;
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < args.length; i++) {
                    sb.append(args[i]).append(" ");
                }
                String newmessage = sb.toString().trim();
                Guild guild = gm.getPlayerGuild(p.getUniqueId());
                guild.sendGuildChat(p, Utils.Chat("&7[&eGC&7] &e&l" + guild.getRank(p.getUniqueId()) + " &7" + p.getName() + " &3⋙&r " + newmessage));
                sendCommandSpyMessage(p, guild, newmessage);
            }
        }
        return false;
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
