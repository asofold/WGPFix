package asofold.fix.wgp.compatlayer;

import java.util.List;

import com.avaje.ebean.EbeanServer;

public class DBConfig extends AbstractConfig{

	public DBConfig(EbeanServer server, String dbKey) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean hasEntry(String path) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void load() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean save() {
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
