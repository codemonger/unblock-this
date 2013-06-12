package bwr.blockcomposer.gamedata;

import java.io.File;

public interface LevelStore {
	GameState getCurrent();
	GameState getPrevious();
	GameState getNext();
	
	File getCurrentFile();
	
	void moveForward();
	void moveBack();
	
	boolean isStoreEmpty();
	boolean hasOnlyOneLevel();
	
	void reset();
	
	void saveLevelStates();
}
