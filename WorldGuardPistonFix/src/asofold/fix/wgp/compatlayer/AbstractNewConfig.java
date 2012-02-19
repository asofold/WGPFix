package asofold.fix.wgp.compatlayer;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationOptions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

public abstract class AbstractNewConfig extends AbstractConfig {
	File file = null;
	MemoryConfiguration config = null;
	public AbstractNewConfig(File file){
		this.file = file;
		this.config = new MemoryConfiguration();
		setOptions(config);
	}
	
	
	
	
	@Override
	public boolean hasEntry(String path) {
		return config.contains(path);
	}
	
	


	@Override
	public String getString(String path, String defaultValue) {
		if (!hasEntry(path)) return defaultValue;
		return config.getString(path, defaultValue);
	}

	

	@Override
	public List<String> getStringKeys(String path) {
		// TODO policy: only strings or all keys as strings ?
		List<String> out = new LinkedList<String>();
		List<Object> keys = getKeys(path);
		if ( keys == null ) return out;
		for ( Object obj : keys){
			if ( obj instanceof String ) out.add((String) obj);
			else{
				try{
					out.add(obj.toString());
				} catch ( Throwable t){
					// ignore.
				}
			}
		}
		return out;
	}

	@Override
	public List<Object> getKeys(String path) {
		List<Object> out = new LinkedList<Object>();
		Set<String> keys;
		if ( path == null) keys = config.getKeys(false);
		else{
			ConfigurationSection sec = config.getConfigurationSection(path);
			if (sec == null) return out;
			keys = sec.getKeys(false);
		}
		if ( keys == null) return out;
		out.addAll(keys);
		return out;
	}
	
	@Override
	public List<Object> getKeys() {
		return getKeys(null);
	}

	@Override
	public Object getProperty(String path, Object defaultValue) {
		Object obj = config.get(path);
		if ( obj  == null ) return defaultValue;
		else return obj;
	}

	@Override
	public List<String> getStringKeys() {
		return getStringKeys(null);
	}

	@Override
	public void setProperty(String path, Object obj) {
		config.set(path, obj);
	}

	@Override
	public List<String> getStringList(String path, List<String> defaultValue) {
		if ( !hasEntry(path)) return defaultValue;
		List<String> out = new LinkedList<String>();
		List<String> entries = config.getStringList(path);
		if ( entries == null ) return defaultValue;
		for ( String entry : entries){
			if ( entry instanceof String) out.add(entry);
			else{
				try{
					out.add(entry.toString());
				} catch (Throwable t){
					// ignore
				}
			}
		}
		return out;
	}

	@Override
	public void removeProperty(String path) {
		// VERY EXPENSIVE
		MemoryConfiguration temp = new MemoryConfiguration();
		setOptions(temp);
		Map<String, Object> values = config.getValues(true);
		values.remove(path);
		for ( String p : values.keySet()){
			temp.set(p, values.get(p));
		}
		config = temp;
	}
	

	void setOptions(Configuration cfg){
		ConfigurationOptions opt = cfg.options();
		opt.pathSeparator('.');
		opt.copyDefaults(true);
	}

}
