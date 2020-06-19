package me.Sam.Guilds;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GuildSpyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
        if (cmd.getName().equalsIgnoreCase("guildspy")) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if(p.hasPermission("SamGuilds.Spy")){
                    if(Guilds.guildspying.contains(p.getUniqueId())){
                        p.sendMessage(Utils.Chat("&7[&a&lSerene&7] &aYou are no longer guildspying."));
                        Guilds.guildspying.remove(p.getUniqueId());
                        return false;
                    } else {
                        p.sendMessage(Utils.Chat("&7[&a&lSerene&7] &aYou are now guildspying."));
                        Guilds.guildspying.add(p.getUniqueId());
                        return false;
                    }
                } else {
                    p.sendMessage(Utils.Chat("&cYou do not have permission to use this command."));
                    return false;
                }
            }
        }
        return false;
    }
}
