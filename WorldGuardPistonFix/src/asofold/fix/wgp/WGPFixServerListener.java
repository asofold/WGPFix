package asofold.fix.wgp;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

public class WGPFixServerListener implements Listener{
	private WGPFix plugin;
	public WGPFixServerListener(WGPFix plugin){
		this.plugin = plugin;
	}
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPluginDisable(PluginDisableEvent event) {
		if ( event.getPlugin().getDescription().getName().equals("WorldGuard")) plugin.blockListener.resetWG();
	}
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPluginEnable(PluginEnableEvent event) {
		if ( event.getPlugin().getDescription().getName().equals("WorldGuard")) plugin.blockListener.setWG();
	}
}
