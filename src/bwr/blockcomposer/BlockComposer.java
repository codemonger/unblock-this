package bwr.blockcomposer;

import java.io.File;
import bwr.blockcomposer.gamedata.LevelMetadata;
import android.app.Application;
import android.os.Environment;

/* TODO:
 *  1. Game crashes when SD card is not present
 */

public class BlockComposer extends Application {

	public static final int NO_ENTRY_TEXT = -1;
	
	public static final LevelMetadata[] tutorialLevels = new LevelMetadata[] {
		new LevelMetadata("Tutorial 1", null, R.raw.tutorial1, 1, R.string.tutorial1_entry_text, R.string.default_exit_text, null),
		new LevelMetadata("Tutorial 2", null, R.raw.tutorial2, 1, R.string.tutorial2_entry_text, R.string.default_exit_text, null),
		new LevelMetadata("Tutorial 3", null, R.raw.tutorial3, 1, R.string.tutorial3_entry_text, R.string.tutorial3_exit_text, null),
	};
	
	public static final LevelMetadata[] levels = new LevelMetadata[] {
		new LevelMetadata("Level 0", null, R.raw.level0, 1, NO_ENTRY_TEXT, R.string.default_exit_text, "level0.state"),
		new LevelMetadata("Level 1", null, R.raw.level1, 1, NO_ENTRY_TEXT, R.string.default_exit_text, "level1.state"),
		new LevelMetadata("Level 2", null, R.raw.level2, 1, NO_ENTRY_TEXT, R.string.default_exit_text, "level2.state"),
		new LevelMetadata("Level 3", "Mike Welsh", R.raw.level3, 1, NO_ENTRY_TEXT, R.string.default_exit_text, "level3.state"),
		new LevelMetadata("Level 4", null, R.raw.level4, 1, NO_ENTRY_TEXT, R.string.default_exit_text, "level4.state"),
		new LevelMetadata("Level 5", null, R.raw.level5, 1, NO_ENTRY_TEXT, R.string.default_exit_text, "level5.state"),
	};
	
	public static final LevelMetadata[] contribLevels = new LevelMetadata[] {
		new LevelMetadata("Simultaneous Losses", "ibookyn", R.raw.simul_loss, 1, NO_ENTRY_TEXT, R.string.default_exit_text, "simul_loss.state"),
		new LevelMetadata("London Bridge", "ibookyn", R.raw.london_bridge, 1, NO_ENTRY_TEXT, R.string.default_exit_text, "london_bridge.state"),
		new LevelMetadata("Abacus", "ibookyn", R.raw.abacus, 1, NO_ENTRY_TEXT, R.string.default_exit_text, "abacus.state"),
		new LevelMetadata("Eisenhower", "ibookyn", R.raw.eisenhower, 1, NO_ENTRY_TEXT, R.string.default_exit_text, "eisenhower.state"),
		new LevelMetadata("Hell's Gate", "ibookyn", R.raw.hellsgate, 1, NO_ENTRY_TEXT, R.string.default_exit_text, "hellsgate.state"),
		new LevelMetadata("Aitken", "ibookyn", R.raw.aitken, 1, NO_ENTRY_TEXT, R.string.default_exit_text, "aitken.state"),
		new LevelMetadata("Down the Hatch", "ibookyn", R.raw.hatch, 1, NO_ENTRY_TEXT, R.string.default_exit_text, "hatch.state"),
	};
	
	
	public final File levelStorageDirectory = new File(Environment.getExternalStorageDirectory(), "blockcomposer");
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if(isExternalStorageAvailable() && !levelStorageDirectory.exists()) 
			levelStorageDirectory.mkdirs();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}
	
	public boolean isExternalStorageAvailable() {
		String state = Environment.getExternalStorageState();
		
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		} 
		
		return false;
	}
}
