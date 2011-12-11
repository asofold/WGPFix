package asofold.fix.wgp;

import java.util.List;

import com.sk89q.worldguard.protection.ApplicableRegionSet;

/**
 * API !
 * Register this and have the last word (unless others register too...) !
 * @author mc_dev
 *
 */
public interface WGPRegionChecker {
	/**
	 * 
	 * @param sets List of ApplicableRegionSet instances, this is the non-empty ones. 
	 * @param hasEmpty indicates that there are locations involved that do not hit a region (i.e. empty sets).
	 * @return
	 */
	public boolean checkRegions(String worldName, List<ApplicableRegionSet> sets, boolean hasEmpty);
}
