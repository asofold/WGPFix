package asofold.fix.wgp.compatlayer;

import java.util.List;

/**
 * Does SnakeYaml handling but not load and save.
 * @author mc_dev
 *
 */
public abstract class AbstractSnakeConfig extends AbstractConfig {
	@Override
	public boolean hasEntry(String path) {
		
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getString(String path, String defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getStringKeys(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Object> getKeys(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Object> getKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getProperty(String path, Object defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getStringKeys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setProperty(String path, Object obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<String> getStringList(String path, List<String> defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeProperty(String path) {
		// TODO Auto-generated method stub
		
	}
	
}
