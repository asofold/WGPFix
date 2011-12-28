package asofold.fix.wgp.compatlayer;


import java.util.List;

/**
 * CONVENTION: return strings if objects can be made strings.
 * @author mc_dev
 *
 */
public interface CompatConfig {
	
	public boolean hasEntry(String path);

	public void load();
	
	public boolean save();

	public Double getDouble(String path, Double defaultValue);

	public Long getLong(String path, Long defaultValue);

	public String getString(String path, String defaultValue);

	public Integer getInt(String path, Integer defaultValue);
	
	public List<String> getStringKeys(String path);
	
	public List<Object> getKeys(String path);

	public Object getProperty(String path, Object defaultValue);

	public List<String> getStringKeys();

	public void setProperty(String path, Object obj);

	public List<String> getStringList(String path, List<String> defaultValue);
	
	public Boolean getBoolean(String path, Boolean defaultValue);
	
	public List<Integer> getIntList(String path, List<Integer> defaultValue);
	
	public void removeProperty(String path);
	
	public List<Double> getDoubleList(String path , List<Double> defaultValue);
	
}
