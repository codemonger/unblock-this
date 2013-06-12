package bwr.blockcomposer.gamedata;

import java.io.File;

public class LevelMetadata {
	public String name;
	public String author;
	public int resourceId;
	public int entryText;
	public int exitText;
	public int version;
	public String stateFilename;
	
	public boolean isUserLevel;
	public File levelFile;

	public LevelMetadata(String name, String author, int resourceId, int version, int entryText, int exitText, String stateFilename) {
		this.name = name;
		this.author = author;
		this.resourceId = resourceId;
		this.entryText = entryText;
		this.exitText = exitText;
		this.stateFilename = stateFilename;
		
		isUserLevel = false;
	}

	public String toString() {
		return name;
	}
}
