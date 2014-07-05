package io.github.ratchetmaster12.bfc.main;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The BFC Main Class, packed with not-that-important stuff; planning on better organisation in n2.1.0
 * @author Sebastian Gluch (Ratchetmaster12)
 */
public class Main extends JavaPlugin {

	//The FMSG (FakeMessage) message used by the FMSG command (player)
	private String msg;
	//The FMSG (FakeMessage) message that will get dumped to the console.
	private String cmsg;
	
	@Override
	public void onEnable(){
		//Set up PluginMetrics
		try {
		    Metrics metrics = new Metrics(this);
		    metrics.start();
		} catch (IOException e) {
		    //Sadly; for some unknown reason plugin metrics was not loaded
		}
	}
	
	/**
	 * The String formatter function
	 * @param strToReplace The String to replace all chat format codes prefixed with a Ampersand and not a Section sign
	 * @return Returns Minecraft-Chat-Ready string
	 */
	public String rep(String strToReplace){
		return strToReplace.replace("&0", "§0").replace("&1", "§1").replace("&2", "§2").replace("&3", "§3").replace("&4", "§4").replace("&5", "§5").replace("&6", "§6").replace("&7", "§7").replace("&8", "§8").replace("&9", "§9").replace("&a", "§a").replace("&b", "§b").replace("&c", "§c").replace("&d", "§d").replace("&e", "§e").replace("&f", "§f").replace("&k", "§k").replace("&l", "§l").replace("&m", "§m").replace("&n", "§n").replace("&o", "§o").replace("&r", "§r");
	}
	
	/**
	 * The Custom Command (CC) 'out' formatter function
	 * @param sender The Sender (not Player nor Console but CommandSender)
	 * @param targetname The Username of the Target
	 * @param rep  The String to replace
	 * @param cid Config Command ID
	 * @return A formatted 'out' of a CC
	 */
	public String crep(CommandSender sender, String targetname, String rep, String cid){
		//A Instance-Player, used later
		Player p;
		//The Sender name
		String pname= "";
		//Checks if the Sender is NOT a Player
		if (!(sender instanceof Player)){
			//If true then set the name to 'Sender'
			pname = "Server";
		}else{
			//If false then set the Player object to be a Player casted from the sender
			p = (Player) sender;
			//Then Get display name and set as Sender's name
			pname = p.getDisplayName();
		}
		return rep(rep.replace("{SENDER}", pname).replace("{TARGET}", targetname).replace("{VERB}",this.getConfig().getString("commands." + cid + ".verb")));
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("fmsg")){
			if(sender.hasPermission("bfc.fmsg") || sender.hasPermission("bfc.*") || sender.isOp()){
				if(args.length > 0){
					msg = "";
					cmsg = "";
					for(String s: args){
						msg = msg + rep(s) + " ";
						cmsg = cmsg + s + " ";
					}
					getLogger().info("[fmsg]" + sender.getName() + " sent fake message: " + cmsg);
					for(Player p: Bukkit.getServer().getOnlinePlayers()){
						if(p.hasPermission("bfc.fmsg.see")){
							p.sendMessage("§3[FMSG]§c" + sender.getName() + ":§f " + rep(msg));
						}else{
							p.sendMessage(rep(msg));
						}
					}
					if(this.getConfig().getBoolean("fmsg-smsg") == true){
						sender.sendMessage("§3[B§cF§3C]§2SUCCESS!");
					}
					return true;
				}else{
					sender.sendMessage("§3[B§cF§3C]§2No words given!");
					return true;
				}
			}else{
				if(sender instanceof Player){
					sender.sendMessage("§3[B§cF§3C]§2You lack the permissions");
				}else{
					sender.sendMessage("There seems there to be an error that has occured.");
				}
				return true;
			}
		}
		if(cmd.getName().equalsIgnoreCase("cc") && sender.hasPermission("bfc.cc") || sender.hasPermission("bfc.*") || sender.isOp()){
			if (args.length > 0){
				executeCustom(args[0], args, sender);
				return true;
			}else{
				sender.sendMessage("§3[B§cF§3C]§2No commands given!");
				return true;
			}
		}
		return true;
	}
	
	public void executeCustom(String cname, String[] args, CommandSender sender){
		try{
			if(sender.hasPermission("cc." + "commands." + cname + ".permission") || sender.hasPermission("bfc.*") || sender.hasPermission("cc.*") || sender.isOp()){
				if(this.getConfig().getBoolean("commands." + cname + ".required-target") == true && args.length == 2){
					if(Bukkit.getServer().getPlayer(args[1]) == (Player) sender){
						sender.sendMessage("§3[B§cF§3C]§2You can't target yourself dummy!");
					}else{
						Bukkit.getServer().broadcastMessage(crep(sender, Bukkit.getServer().getPlayer(args[1]).getDisplayName(),this.getConfig().getString("commands." + cname + ".out"), cname));
					}
				}else if(this.getConfig().getBoolean("commands." + cname + ".required-target") == false && args.length == 1){
					Bukkit.getServer().broadcastMessage(crep(sender, "", this.getConfig().getString("commands." + cname + ".out"), cname));
				}else{
					sender.sendMessage("§3[B§cF§3C]§2That command requires one or no targets!");
				}
			}else{
				sender.sendMessage("§3[B§cF§3C]§2You lack the permissions!");
			}
		}catch (Exception e){
			sender.sendMessage("§3[B§cF§3C]§2There was an error whilst executing that command.");
		}
	}
}
