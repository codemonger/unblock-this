package bwr.blockcomposer.gamedata;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import bwr.blockcomposer.GameResources;

import android.content.Context;
import android.util.SparseArray;

public class StaticLevelStore implements LevelStore {
	private final LevelMetadata[] levelData;
	private final SparseArray<Level> levels = new SparseArray<Level>();
	private final SparseArray<GameState> gameStates = new SparseArray<GameState>();
	
	private int currentLevel = 0;
	private int nextLevel, prevLevel;
	
	private final GameResources gameResources;
	
	public StaticLevelStore(GameResources gameResources, LevelMetadata[] levelData) {
		this.gameResources = gameResources;
		this.levelData = levelData;
		loadLevels();
		updatePrevAndNext();
	}
	
	private void loadLevels() {
		for(LevelMetadata lm : levelData) {
			try {
				Level level = loadLevel(lm.resourceId);
				
				level.version = lm.version; // TODO: Level Version should be inside the level file
				
				level.setAuthor(lm.author); // TODO: Level Author should be inside the level file
				
				level.setName(lm.name);
				
				levels.append(lm.resourceId, level);
				
				GameState state = null;
				
				if(lm.stateFilename != null) {
					try {
						FileInputStream fin = gameResources.getContext().openFileInput(lm.stateFilename);
						
						DataInputStream in = new DataInputStream(new BufferedInputStream(fin));
						
						state = new GameState(level, in);
						fin.close();
	
	//					if(state.getVersion() != l.version) {
	//						Log.i("BlockComposer", "Unable to restore level '" + l.name + "' due to mismatch in version.");
	//						deleteFile(l.stateFilename);
	//					} else {
	//						m.restoreState(state);
	//					}
						
					} catch (FileNotFoundException e) {
						state = new GameState(level);
					}
				} else {
					state = new GameState(level);
				}
				
				state.exitText = lm.exitText;
				state.entryText = lm.entryText;
				
				gameStates.append(lm.resourceId, state);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private Level loadLevel(int resourceId) throws IOException {
		InputStream is = gameResources.getContext().getResources().openRawResource(resourceId);
		Level result = Level.loadFromFile(is, gameResources);
		is.close();
		return result;
	}

	public GameState getCurrent() {
		return gameStates.get(levelData[currentLevel].resourceId);
	}

	public GameState getPrevious() {
		return gameStates.get(levelData[prevLevel].resourceId);
	}

	public GameState getNext() {
		return gameStates.get(levelData[nextLevel].resourceId);
	}
	
	public File getCurrentFile() {
		return levelData[currentLevel].levelFile;
	}
	
	private void updatePrevAndNext() {
		nextLevel = (currentLevel + 1) % levelData.length;
		prevLevel = currentLevel - 1;
		if(prevLevel < 0) prevLevel = levelData.length-1;
	}

	public void moveForward() {
		currentLevel++;
		currentLevel %= levelData.length;
		updatePrevAndNext();
	}

	public void moveBack() {
		currentLevel--;
		if(currentLevel < 0) currentLevel = levelData.length-1;
		updatePrevAndNext();
	}

	public boolean isStoreEmpty() {
		return levelData.length == 0;
	}

	public boolean hasOnlyOneLevel() {
		return levelData.length == 1;
	}

	public void reset() {
		// nothing
	}

	private void saveLevelState(Level level, String stateFilename, GameState state) throws IOException {
		final Context context = gameResources.getContext();
		
		final FileOutputStream fout = context.openFileOutput(stateFilename, Context.MODE_PRIVATE);
		final DataOutputStream out = new DataOutputStream(new BufferedOutputStream(fout));

		state.setVersion(level.version);
		state.writeTo(out);
					
		out.flush();
		fout.close();
	}
	
	public void saveLevelStates() {
		for(LevelMetadata levelMetadata : levelData) {
			if(levelMetadata.stateFilename == null) continue;
			
			GameState state = gameStates.get(levelMetadata.resourceId);
			
			if(state != null && state.isDirty()) {
				Level level = state.getLevel();
				
				if(level != null) {
					try {
						saveLevelState(level, levelMetadata.stateFilename, state);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
			}
			
		}
	}
}
