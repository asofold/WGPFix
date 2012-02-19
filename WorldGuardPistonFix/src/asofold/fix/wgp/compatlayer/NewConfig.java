package asofold.fix.wgp.compatlayer;

import java.io.File;

import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class NewConfig extends AbstractNewConfig{
	
	
	public NewConfig(File file) {
		super(file);
	}


	@Override
	public void load(){
		config = new MemoryConfiguration();
		setOptions(config);
		FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
		setOptions(cfg);
		config.addDefaults(cfg);
		
		for (String x : config.getValues(true).keySet()){
			System.out.println(x);
		}
	}
	

	@Override
	public boolean save(){
		YamlConfiguration cfg = new YamlConfiguration();
		setOptions(cfg);
		cfg.addDefaults(config);
		try{
			cfg.save(file);
			return true;
		} catch (Throwable t){
			return false;
		}
	}
	
	
	
}
