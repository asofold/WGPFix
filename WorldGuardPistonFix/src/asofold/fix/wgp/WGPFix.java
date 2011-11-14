package asofold.fix.wgp;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WGPFix extends JavaPlugin {
	class WGPFixBlockListener extends BlockListener {
		private WGPFix plugin;
		long tsWG = 0;
		long tsThreshold = 4000;
		WorldGuardPlugin wg = null;
		
		public boolean monitorPistons = true;
		
		public  WGPFixBlockListener(WGPFix plg) {
			this.plugin = plg;
			
		}

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
			if ( !sameOwners(pistonBlock.getLocation(), locs)){
				event.setCancelled(true);
			}	
		}

		@Override
		public void onBlockPistonRetract(BlockPistonRetractEvent event) {
			if ( !monitorPistons) return;
			if ( event.isCancelled()) return;
			if (event.isSticky()){
				Block pistonBlock = event.getBlock();
				Location affecteLoc = event.getRetractLocation();
				List<Location> affected = new LinkedList<Location>();
				affected.add(affecteLoc);
				affected.add(pistonBlock.getRelative(event.getDirection()).getLocation());
				if ( !sameOwners(pistonBlock.getLocation(), affected)) event.setCancelled(true);	
			}
			
		}
		
		private void setWG() {
			Plugin temp = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
			WorldGuardPlugin wg = (WorldGuardPlugin) temp;
			this.wg = wg;
			this.tsWG = System.currentTimeMillis();
			// TODO: maybe get region managers already here
		}
		
		public boolean sameOwners(Location refLoc, List<Location> locs){
			if (System.currentTimeMillis()-this.tsWG > this.tsThreshold) this.setWG();
			RegionManager mg = this.wg.getRegionManager(refLoc.getWorld());
			ApplicableRegionSet set = mg.getApplicableRegions(refLoc);
			boolean isRegion = set.size() != 0;
			// TODO: use some caching ?
			Set<String> mustMatch = getUserSet(set);
			int size = mustMatch.size();
			for ( Location loc : locs){
				set = mg.getApplicableRegions(loc);
				int sz = set.size();
				if ( (set==null) || (sz==0)){
					// ok.
				} else if ( isRegion ){
					// compare owner sets:
					Set<String> ref = getUserSet(set);
					if ( size != ref.size() ) return false;
					if ( !mustMatch.containsAll(ref)) return false;
				} else {
					// disallow from no region to unowned region !
					return false; 
				}
			}
			
			return true;
		}
		public Set<String> getUserSet(ApplicableRegionSet rs){
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
	}
	
	private final WGPFixBlockListener blockListener = new WGPFixBlockListener(this);
	
	
	@Override
	public void onDisable() {
		System.out.println("WorldGuardPistonFix (WGPFix) 1.0.0 disabled.");
	}

	@Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.BLOCK_PISTON_EXTEND, this.blockListener, Priority.Low, this);
		pm.registerEvent(Event.Type.BLOCK_PISTON_RETRACT, this.blockListener, Priority.Low, this);
		System.out.println("WorldGuardPistonFix (WGPFix) 1.0.0 enabled.");
	}

}
