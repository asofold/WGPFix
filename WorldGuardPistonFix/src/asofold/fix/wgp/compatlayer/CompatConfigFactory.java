package asofold.fix.wgp.compatlayer;

import java.io.File;

public class CompatConfigFactory {
	
	public static final String version = "0.0.5";
	
	/**
	 * 
	 * @param file
	 * @return null if fails.
	 */
	public static final CompatConfig getConfig(File file){
		CompatConfig out = null;
		// TODO: add more (latest API)
		try{
			return new OldConfig(file);
		} catch (Throwable t){
			
		}
		return out;
	}
	
	public static final CompatConfig getOldConfig(File file){
		return new OldConfig(file);
	}
}
