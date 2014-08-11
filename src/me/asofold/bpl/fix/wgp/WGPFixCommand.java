package me.asofold.bpl.fix.wgp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class WGPFixCommand implements CommandExecutor{
	private WGPFix plugin;
	public WGPFixCommand(WGPFix plugin){
		this.plugin = plugin;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if ( !label.equalsIgnoreCase("wgpfix")) return false;
		if ( args.length == 1 ){
			if ( args[0].equalsIgnoreCase("reload")){
				if ( plugin.hasPermission(sender, "wgpfix.reload" )){
					if (plugin.loadSettings()){
						sender.sendMessage("[WGPFix] Settings reloaded.");
						return true;
					}
					else{
						sender.sendMessage("[WGPFix] Failed to reload settings, fall back to paranoid settings.");
						return true; 
					}
				}
			} 
			else if ( args[0].equalsIgnoreCase("panic") || args[0].equalsIgnoreCase("titanic")){
				if ( plugin.hasPermission(sender, "wgpfix.panic.enable" )){
					plugin.setPanic(true);
					sender.sendMessage("[WGPFix] Set to PANIC - all piston action will be prevented.");
					sender.sendMessage("NOTE: This does not change the configuration, which might allow pistons on reload!");
					return true; 
				}
			}
			else if ( args[0].equalsIgnoreCase("nopanic") || args[0].equalsIgnoreCase("unpanic")){
				if ( plugin.hasPermission(sender, "wgpfix.panic.disable" )){
					plugin.setPanic(false);
					sender.sendMessage("[WGPFix] Allowing pistons, no more panic.");
					sender.sendMessage("NOTE: This does not change the configuration, which might deny pistons on reload!");
					return true; 
				}
			}
			else if ( args[0].equalsIgnoreCase("monitor") || args[0].equalsIgnoreCase("on")){
				if ( plugin.hasPermission(sender, "wgpfix.monitor.enable" )){
					plugin.setMonitorPistons(true);
					sender.sendMessage("[WGPFix] Pistons are being monitored now.");
					sender.sendMessage("NOTE: This does not change the configuration, which might set monitor-pistons to false on reload!");
					return true; 
				}
			}
			else if ( args[0].equalsIgnoreCase("nomonitor") || args[0].equalsIgnoreCase("unmonitor") || args[0].equalsIgnoreCase("off")){
				if ( plugin.hasPermission(sender, "wgpfix.monitor.disable" )){
					plugin.setMonitorPistons(false);
					sender.sendMessage("[WGPFix] Pistons are not being monitored, for now.");
					sender.sendMessage("NOTE: This does not change the configuration, which might set monitor-pistons to true on reload!");
					return true; 
				}
			}
		}
		return false;
	}
}
