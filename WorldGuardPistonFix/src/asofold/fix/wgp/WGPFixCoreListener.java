package asofold.fix.wgp;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WGPFixCoreListener implements Listener {
	WGPFix plugin;
	public WGPFixCoreListener(WGPFix plugin){
		this.plugin = plugin;
	}
	long tsWG = 0;
	WorldGuardPlugin wg = null;
	// config settings:
	long tsThreshold = 4000;
	boolean monitorPistons = true;
	boolean preventNonStickyRetract = false;
	boolean popDisallowed = false;
	boolean monitorStructureGrowth = false;
	
	
	boolean panic = false;
	
	int maxBlocks = WGPFix.defaultMaxBlocks;
	
	/**
	 * Deny block ids from being affected by sticky pistons.
	 * contains all from denAll as well.
	 */
	public final Set<Integer> denySticky = new HashSet<Integer>();
	/**
	 * Deny block ids from being affected by any piston type.
	 */
	public final Set<Integer> denyAll = new HashSet<Integer>();
	
	
	@EventHandler(priority=EventPriority.LOW)
	final void onBlockPistonExtend(final BlockPistonExtendEvent event) {
		if ( panic){
			event.setCancelled(true);
			return;
		}
		if ( !monitorPistons) return;
		if ( event.isCancelled()) return;
		final Block pistonBlock = event.getBlock();
		final List<Block> affectedBlocks = event.getBlocks();
		final List<Location> locs = new LinkedList<Location>();
		final BlockFace dir = event.getDirection();
		final Block extensionBlock = pistonBlock.getRelative(dir);
		locs.add(extensionBlock.getLocation());
		final int bSize; 
		if ( affectedBlocks == null ) bSize = 0; // TODO: remove if really redundant.
		else bSize = affectedBlocks.size();
		final boolean isSticky = event.isSticky();
		if ( bSize>0 ){
			for ( Block block : affectedBlocks){
				final int id = block.getTypeId();
				if (isSticky ){
					if ( denySticky.contains(id) ){
						event.setCancelled(true);
						if (popDisallowed) pop(pistonBlock, null, isSticky);
						return;
					}
				} else if ( denyAll.contains(id)){
					event.setCancelled(true);
					if (popDisallowed) pop(pistonBlock, null, isSticky);
					return;
				}
				locs.add(block.getLocation());
			}
		}
		// add empty block at end
		final Block endBlock;
		if (bSize>0){
			endBlock = pistonBlock.getRelative(dir, bSize+1);
			locs.add(endBlock.getLocation());
		}
		else endBlock = extensionBlock;
		final int id = endBlock.getTypeId();
		if (isSticky ){ // TODO: get rid of code cloning.
			if ( denySticky.contains(id) ){
				event.setCancelled(true);
				if (popDisallowed) pop(pistonBlock, null, isSticky);
				return;
			}
		} else if ( denyAll.contains(id)){
			event.setCancelled(true);
			if (popDisallowed) pop(pistonBlock, null, isSticky);
			return;
		}
		if ( bSize + 2 > maxBlocks ){ //  >= because the base is counted in
			event.setCancelled(true);	
			if (popDisallowed) pop(pistonBlock, null, isSticky );
			return;
		}
		else 
		if ( !sameOwners(pistonBlock.getLocation(), locs)){
			event.setCancelled(true);
			if (popDisallowed) pop(pistonBlock, null, isSticky);
		} 
	}

	@EventHandler(priority=EventPriority.LOW)
	final void onBlockPistonRetract(final BlockPistonRetractEvent event) {
		if ( panic){
			event.setCancelled(true);
			return;
		}
		if ( !monitorPistons) return;
		if ( event.isCancelled()) return;
		final boolean isSticky = event.isSticky() ;
		if (!(isSticky|| preventNonStickyRetract)) return;
		final BlockFace dir = event.getDirection();
		final Block pistonBlock = event.getBlock();
		final List<Location> affected = new LinkedList<Location>();
		final Block extensionBlock = pistonBlock.getRelative(dir);
		if ( isSticky){
			final Block affectedBlock = extensionBlock.getRelative(dir);
			final int id = affectedBlock.getTypeId();
			if ( denySticky.contains(id)){
				event.setCancelled(true);
				if (popDisallowed) pop(pistonBlock, pistonBlock.getRelative(dir), isSticky );
				return;
			}
			else if ( id != 0 )	affected.add(affectedBlock.getLocation());
		}
		affected.add(extensionBlock.getLocation());
		if (affected.size() >= maxBlocks ){ //  >= because the base is counted in
			event.setCancelled(true);	
			if (popDisallowed) pop(pistonBlock, pistonBlock.getRelative(dir), isSticky );
			return;
		}
		else 
		if ( !sameOwners(pistonBlock.getLocation(), affected)){
			event.setCancelled(true);	
			if (popDisallowed) pop(pistonBlock, pistonBlock.getRelative(dir), isSticky );
			return;
		} 
	}
	
	@EventHandler(priority=EventPriority.LOW)
	final void onStructureGrow(StructureGrowEvent event){
		if ( event.isCancelled()) return;
		if (!monitorStructureGrowth) return;
		final List<Location> affected = new LinkedList<Location>();
		for ( BlockState state : event.getBlocks()){
			affected.add(state.getLocation());
		}
		Location loc = event.getLocation();
		if ( loc == null){
			// compatibility check (i do not know what else might be grown later on and how).
			if ( affected.isEmpty()) return;
			else loc = affected.remove(0);
			if ( affected.isEmpty()) return;
		}
		if (!sameOwners(loc, affected)) event.setCancelled(true);
	}
	

	void pop(Block pistonBlock, Block extensionBlock, boolean isSticky) {
		int itemId = isSticky ? 29:33;
		pistonBlock.setType(Material.AIR);
		pistonBlock.getState().update();
		if (extensionBlock!=null){
			extensionBlock.setType(Material.AIR);
			extensionBlock.getState().update();
		}
		World world = pistonBlock.getWorld();
		double x = .5 + pistonBlock.getX();
		double y = .5 + pistonBlock.getY();
		double z = .5 + pistonBlock.getZ();
		world.dropItemNaturally(new Location(world,x,y,z), new ItemStack(itemId,1));
	}
	
	/**
	 * In case of registered region checkers this will query them and return false if they return false.
	 * Furthermore this adds refLoc to locs in that case (!).
	 * @param refLoc
	 * @param locs
	 * @return
	 */
	final boolean sameOwners(final Location refLoc, final List<Location> locs){
		final WorldGuardPlugin wg = getWorldGuard();
		if ( wg == null) return false; // security option.
		final RegionManager mg = wg.getRegionManager(refLoc.getWorld());
		ApplicableRegionSet set = mg.getApplicableRegions(refLoc);
		final boolean isRegion = set.size() != 0;
		boolean hasEmpty = !isRegion;
		// TODO: use some caching ?
		final Set<String> mustMatch = getUserSet(set);
		final int size = mustMatch.size();
		final boolean hasCheckers = !WGPFix.regionCheckers.isEmpty();
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
				final Set<String> ref = getUserSet(set);
				if ( size != ref.size() ) return false;
				if ( !mustMatch.containsAll(ref)) return false;
				if (hasCheckers) applicableSets.add(set);
			} else {
				// disallow from no region to unowned region !
				return false; 
			}
			
		}
		if (hasCheckers){
			final String worldName = refLoc.getWorld().getName();
			for ( WGPRegionChecker checker : WGPFix.regionCheckers){
				locs.add(refLoc);
				if ( !checker.checkRegions(worldName, applicableSets, hasEmpty)) return false;
			}
		}
		return true;
	}
	
	private final static Set<String> getUserSet(final ApplicableRegionSet rs){
		final Set<String> set = new HashSet<String>();
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
		else if ( !temp.isEnabled() ) ok = false;
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
