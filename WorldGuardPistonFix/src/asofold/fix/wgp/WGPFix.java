package asofold.fix.wgp;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

/**
 * 
 * 
 * @author mc_dev
 *
 */
public class WGPFix extends JavaPlugin {

	class WGPFixBlockListener extends BlockListener {
		long tsWG = 0;
		WorldGuardPlugin wg = null;
		// config settings:
		long tsThreshold = 4000;
		boolean monitorPistons = true;
		boolean preventNonStickyRetract = false;
		boolean popDisallowed = false;
		@Override
		public void onBlockPistonExtend(BlockPistonExtendEvent event) {
			if ( !monitorPistons) return;
			if ( event.isCancelled()) return;
			Block pistonBlock = event.getBlock();
			List<Block> affectedBlocks = event.getBlocks();

			List<Location> locs = new LinkedList<Location>();
			BlockFace dir = event.getDirection();
			locs.add(pistonBlock.getRelative(dir).getLocation());
			int bSize = affectedBlocks.size();
			if ( (affectedBlocks!=null) && (bSize>0) ){
				for ( Block block : affectedBlocks){
					locs.add(block.getLocation());
				}
				// add empty block at end
				Block endBlock = pistonBlock.getRelative(dir,bSize+1 );
				locs.add(endBlock.getLocation());
			}
			Location pistonLoc = pistonBlock.getLocation();
			if ( !sameOwners(pistonLoc, locs)){
				event.setCancelled(true);
				if (popDisallowed) pop(pistonBlock, null, event.isSticky());
			} 
		}

		@Override
		public void onBlockPistonRetract(BlockPistonRetractEvent event) {
			if ( !monitorPistons) return;
			if ( event.isCancelled()) return;
			boolean check = event.isSticky() || preventNonStickyRetract;
			if (check){
				Block pistonBlock = event.getBlock();
				List<Location> affected = new LinkedList<Location>();
				Location affectedLoc = event.getRetractLocation(); // pulled if sticky
				if ( affectedLoc != null) affected.add(affectedLoc);
				// TODO: bug search
				affected.add(pistonBlock.getRelative(event.getDirection()).getLocation()); // piston extension
				Location pistonLoc = pistonBlock.getLocation();
				if ( !sameOwners(pistonLoc, affected)){
					event.setCancelled(true);	
					if (popDisallowed) pop(pistonBlock, pistonBlock.getRelative(event.getDirection()), event.isSticky() );
				} 
			}
		}

		void pop(Block pistonBlock, Block extensionBlock, boolean isSticky) {
			int itemId = isSticky ? 29:33;
			pistonBlock.setType(Material.AIR);
			pistonBlock.getState().update();
			if (extensionBlock!=null){
				extensionBlock.setType(Material.AIR);
				extensionBlock.getState().update();
			}
			pistonBlock.getWorld().dropItemNaturally(pistonBlock.getLocation(), new ItemStack(itemId, 1));
		}
		
		boolean sameOwners(Location refLoc, List<Location> locs){
			WorldGuardPlugin wg = getWorldGuard();
			if ( wg == null) return false; // security option.
			RegionManager mg = wg.getRegionManager(refLoc.getWorld());
			ApplicableRegionSet set = mg.getApplicableRegions(refLoc);
			boolean isRegion = set.size() != 0;
			boolean hasEmpty = !isRegion;
			// TODO: use some caching ?
			Set<String> mustMatch = getUserSet(set);
			int size = mustMatch.size();
			boolean hasCheckers = !regionCheckers.isEmpty();
			List<ApplicableRegionSet> applicableSets  = null;
			if ( hasCheckers){ 
				applicableSets = new LinkedList<ApplicableRegionSet>();
				if (isRegion) applicableSets.add(set);
			}
			for ( Location loc : locs){
				set = mg.getApplicableRegions(loc);
				if (set.size()==0){ // ok.
					hasEmpty = true;
				} else if ( isRegion ){
					// compare owner sets:
					Set<String> ref = getUserSet(set);
					if ( size != ref.size() ) return false;
					if ( !mustMatch.containsAll(ref)) return false;
					if (hasCheckers) applicableSets.add(set);
				} else {
					// disallow from no region to unowned region !
					return false; 
				}
				
			}
			if (hasCheckers){
				String worldName = refLoc.getWorld().getName();
				for ( WGPRegionChecker checker : regionCheckers){
					locs.add(refLoc);
					if ( !checker.checkRegions(worldName, applicableSets, locs, hasEmpty)) return false;
				}
			}
			return true;
		}
		Set<String> getUserSet(ApplicableRegionSet rs){
			Set<String> set = new HashSet<String>();
			if ( rs != null ){
				for ( ProtectedRegion region : rs){
					DefaultDomain dom = region.getOwners();
					for ( String p : dom.getPlayers()){
						set.add(p.toLowerCase());
					}
					for ( String p : dom.getGroups()){
						set.add("g:"+p.toLowerCase());
					}
					dom = region.getMembers();
					for ( String p : dom.getPlayers()){
						set.add(p.toLowerCase());
					}
					for ( String p : dom.getGroups()){
						set.add("g:"+p.toLowerCase());
					}
				}
			}
			
			return set;
		}
		final boolean setWG() {
			Plugin temp = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
			boolean ok = true;
			if ( temp == null ) ok = false;
			if ( !temp.isEnabled() ) ok = false;
			if ( !ok){
				resetWG();
				return false;
			} 
			WorldGuardPlugin wg = (WorldGuardPlugin) temp;
			this.wg = wg;
			this.tsWG = System.currentTimeMillis();
			// TODO: maybe get region managers already here
			return true;
		}
		final WorldGuardPlugin getWorldGuard(){
			if (System.currentTimeMillis()-this.tsWG > this.tsThreshold) this.setWG();
			return this.wg;
		}
		final void resetWG(){
			this.wg = null;
			this.tsWG = 0;
		}
	}
	
	class WGPFixServerListener extends ServerListener{
		@Override
		public void onPluginDisable(PluginDisableEvent event) {
			if ( event.getPlugin().getDescription().getName().equals("WorldGuard")) blockListener.resetWG();
		}
		@Override
		public void onPluginEnable(PluginEnableEvent event) {
			if ( event.getPlugin().getDescription().getName().equals("WorldGuard")) blockListener.setWG();
		}
	}
	
	class WGPFixCommand implements CommandExecutor{
		@Override
		public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
			if ( !label.equalsIgnoreCase("wgpfix")) return false;
			if ( args.length == 1 ){
				if ( args[0].equalsIgnoreCase("reload")){
					if ( blockListener.getWorldGuard().hasPermission(sender, "wgpfix.reload" )){
						if (loadSettings()){
							sender.sendMessage("WGPFix - Settings reloaded.");
							return true;
						}
						else{
							sender.sendMessage("WGPFix - Failed to reload settings, fall back to paranoid settings.");
							return true; // just to prevent showing usage.
						}
					}
				}
			}
			return false;
		}
	}
	
	private final WGPFixBlockListener blockListener = new WGPFixBlockListener();
	private final WGPFixServerListener serverListener = new WGPFixServerListener();
	final static List<WGPRegionChecker> regionCheckers = new LinkedList<WGPRegionChecker>();
	
	@Override
	public void onDisable() {
		blockListener.monitorPistons = false;
		blockListener.resetWG();
		System.out.println("WorldGuardPistonFix (WGPFix) "+getDescription().getVersion()+" disabled.");
	}

	@Override
	public void onEnable() {
		loadSettings();
		getCommand("wgpfix").setExecutor(new WGPFixCommand());
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.BLOCK_PISTON_EXTEND, blockListener, Priority.Low, this);
		pm.registerEvent(Event.Type.BLOCK_PISTON_RETRACT, blockListener, Priority.Low, this);
		pm.registerEvent(Event.Type.PLUGIN_DISABLE, serverListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLUGIN_ENABLE, serverListener, Priority.Monitor, this);
		System.out.println("WorldGuardPistonFix (WGPFix) "+getDescription().getVersion()+" enabled.");
	}
	
	/**
	 * API !
	 * If set to false pistons will not be monitored at all !
	 * @param monitor
	 */
	public void setMonitorPistons( boolean monitor){
		this.blockListener.monitorPistons = monitor;
	}
	
	/**
	 * API !
	 * If set to true, non sticky pistons might be prevented from retracting at some performance loss (imagine item lifting devices).
	 * (For the paranoid))
	 * @param prevent
	 */
	public void setPreventNonStickyRetract( boolean prevent){
		this.blockListener.preventNonStickyRetract = prevent;
	}
	
	/**
	 * API !
	 * Set the interval in ms, at which the WorldGuard instance is set.
	 * @param ms
	 */
	public void setWorldGuardSetInterval(long ms){
		this.blockListener.tsThreshold = ms;
	}
	
	public void setPopDisallowed( boolean pop){
		this.blockListener.popDisallowed = pop;
	}
	
	/**
	 * (API)
	 * Load and apply settings from wgpfx.yml !
	 */
	public boolean loadSettings(){
		File file = new File( getDataFolder(), "wgpfix.yml");
		try{
			org.bukkit.util.config.Configuration config = new org.bukkit.util.config.Configuration(file);
			if (!file.exists()){
				config.setProperty("monitor-pistons", true);
				config.setProperty("prevent-nonsticky-retract", false);
				config.setProperty("set-worldguard-interval", 4000);
				config.setProperty("pop-disallowed", false);
				config.save(); // ignore result
			} else{
				config.load();
			}
			this.setMonitorPistons(config.getBoolean("monitor-pistons", true));
			this.setPreventNonStickyRetract(config.getBoolean("prevent-nonsticky-retract", false));
			this.setWorldGuardSetInterval(config.getInt("set-worldguard-interval", 4000));
			this.setPopDisallowed(config.getBoolean("pop-disallowed", false));
			blockListener.setWG();
			return true;
		} catch (Throwable t){
			getServer().getLogger().severe("WGPFix - Could not load configuration, continue wirh paranoid settings !");
			setParanoid();
			return false;
		}
	}
	
	/**
	 * (API)
	 * Prevent everything that can be prevented by this plugin.
	 */
	public void setParanoid(){
		this.setMonitorPistons(true);
		this.setPreventNonStickyRetract(true);
		this.setWorldGuardSetInterval(4000);
		this.setPopDisallowed(true); // PARANOID !
		blockListener.setWG();
	}
	
	/**
	 * API !
	 * Register implementation that will check regions just before allowing piston-action.
	 */
	public static void addRegionChecker( WGPRegionChecker checker){
		regionCheckers.add(checker);
	}
	
	/**
	 * API !
	 * Unregister implementation for checking regions affected by piston actions.
	 * @param checker
	 */
	public static void removeRegionChecker( WGPRegionChecker checker){
		regionCheckers.remove(checker);
	}

}
