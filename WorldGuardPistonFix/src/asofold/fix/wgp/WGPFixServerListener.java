package asofold.fix.wgp;

import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;

public class WGPFixServerListener extends ServerListener{
	private WGPFix plugin;
	public WGPFixServerListener(WGPFix plugin){
		this.plugin = plugin;
	}
	@Override
	public void onPluginDisable(PluginDisableEvent event) {
		if ( event.getPlugin().getDescription().getName().equals("WorldGuard")) plugin.blockListener.resetWG();
	}
	@Override
	public void onPluginEnable(PluginEnableEvent event) {
		if ( event.getPlugin().getDescription().getName().equals("WorldGuard")) plugin.blockListener.setWG();
	}
}
