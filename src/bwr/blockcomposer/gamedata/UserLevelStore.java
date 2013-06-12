package bwr.blockcomposer.gamedata;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeMap;

import android.content.Context;
import android.os.Environment;
import bwr.blockcomposer.GameResources;

public class UserLevelStore implements LevelStore {
	
	private class BlockComposerLevelFileFilter implements FilenameFilter {
		public boolean accept(File dir, String fileName) {
			return fileName.toLowerCase().endsWith(".bclevel");
		}
	}
	
	private final TreeMap<File, Level> levels = new TreeMap<File, Level>();
	private final TreeMap<File, GameState> gameState = new TreeMap<File, GameState>();
	
	private final File levelStorageDirectory;
	private final GameResources gameResources;
	
	private File prevLevel, curLevel, nextLevel;
	
	public UserLevelStore(Context context, File levelStorageDirectory, GameResources gameResources) {
		this.levelStorageDirectory = levelStorageDirectory;
		this.gameResources = gameResources;
		
		loadLevels();
	}

	private void loadLevels() {
		File levelFiles[] = levelStorageDirectory.listFiles(new BlockComposerLevelFileFilter());
		
		for(File lf : levelFiles) {
			try {
				Level level = loadLevel(lf, gameResources);
				
				levels.put(lf, level);
				
				//gameState.put(lf, new GameState(level));
				gameState.put(lf, restoreOrCreateState(lf, level));

				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		resetSelection();
		
	}
	
	private Level loadLevel(File userLevelFile, GameResources gameResources) throws IOException {
		FileInputStream fis = new FileInputStream(userLevelFile);
		Level result = Level.loadFromFile(fis, gameResources);
		return result;
	}
	
	public boolean isExternalStorageAvailable() {
		String state = Environment.getExternalStorageState();
		
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		} 
		
		return false;
	}
	
	public GameState getPrevious() {
		return gameState.get(prevLevel);
	}
	
	public GameState getCurrent() {
		return gameState.get(curLevel);
	}

	public GameState getNext() {
		return gameState.get(nextLevel);
	}
	
	public File getCurrentFile() {
		return curLevel;
	}


	public void moveForward() {
		prevLevel = curLevel;
		curLevel = nextLevel;
		
		if(curLevel == levels.lastKey() || levels.size() == 1) {
			nextLevel = levels.firstKey();
		} else {
			Iterator<File> i = levels.tailMap(curLevel).keySet().iterator();
			i.next();
			nextLevel = i.next();
		}
	}

	public void moveBack() {
		nextLevel = curLevel;
		curLevel = prevLevel;
		
		if(curLevel == levels.firstKey() || levels.size() == 1) {
			prevLevel = levels.lastKey();
		} else {
			prevLevel = levels.headMap(curLevel).lastKey();
		}
	}

	public boolean isStoreEmpty() {
		return levels.isEmpty();
	}

	public boolean hasOnlyOneLevel() {
		return levels.size() == 1;
	}

	private void resetSelection() {
		if(levels.isEmpty()) return;
		
		curLevel = levels.firstKey();
		prevLevel = levels.lastKey();
		if(levels.size() > 1) {
			Iterator<File> i = levels.tailMap(curLevel).keySet().iterator();
			i.next();
			nextLevel = i.next();
		} else {
			nextLevel = levels.firstKey();
		}
	}
	
	public void reset() {
		purgeUserLevels();
	}
	
	public void purgeUserLevels() {
		saveLevelStates();
		levels.clear();
		gameState.clear();
		loadLevels();
	}
	
	private GameState restoreOrCreateState(File levelFile, Level level) {
		GameState result = null;
		
		File stateFile = getStateFileForUserLevel(levelFile);

		if(stateFile != null && stateFile.exists()) try {
			System.out.println("BlockComposer: Restoring State for level: " + level.getName());
			FileInputStream fin = new FileInputStream(stateFile);
			DataInputStream in = new DataInputStream(new BufferedInputStream(fin));
			
			result = new GameState(level, in);
			fin.close();
	
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		if(result == null) {
			result = new GameState(level);
		}
		
		return result;
	}
	
	private File getStateFileForUserLevel(File userLevelLocation) {
		return new File(userLevelLocation.getAbsolutePath().concat(".state"));
	}
	
	private void saveLevelState(File levelFile, GameState state) throws IOException {
		File stateFile = getStateFileForUserLevel(levelFile);
		
		FileOutputStream fout = new FileOutputStream(stateFile);
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(fout));
	
		// TODO: We should not set version here.
		int version = 0;
		state.setVersion(version);
	
		state.writeTo(out);
					
		out.flush();
		fout.close();
	}
	
	public void saveLevelStates() {
		for(File levelFile : levels.keySet()) {
			GameState state = gameState.get(levelFile);
			
			if(state != null && state.isDirty()) {
				Level level = state.getLevel();
				
				if(level != null) {
					try {
						saveLevelState(levelFile, state);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
			}
			
		}
	}

}
