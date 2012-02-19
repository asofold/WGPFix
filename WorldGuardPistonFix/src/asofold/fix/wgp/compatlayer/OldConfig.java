package asofold.fix.wgp.compatlayer;

import java.io.File;


@SuppressWarnings("deprecation")
public class OldConfig extends AbstractOldConfig{
	public OldConfig(File file) {
		super(file);
	}
	@Override
	public void load(){
		config.load();
	}
	@Override
	public boolean save(){
		return config.save();
	}
}
