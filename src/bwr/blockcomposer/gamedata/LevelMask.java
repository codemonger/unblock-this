package bwr.blockcomposer.gamedata;

import java.util.Arrays;

import bwr.blockcomposer.types.IntVector;

public class LevelMask {
	
	private final boolean[][][] mask;
	
	private final LevelDimensions dimensions;
	
	public LevelMask(LevelDimensions dimensions) {
		this.dimensions = dimensions;
		mask = new boolean[dimensions.getSizeX()][dimensions.getSizeY()][dimensions.getSizeZ()];
	}
	
	public void setMaskAt(IntVector location, boolean value) {
		if(dimensions.isOnLevel(location)) {
			IntVector arrayLocation = dimensions.mapToArrayCoords(location);
			mask[arrayLocation.x][arrayLocation.y][arrayLocation.z] = value;
		}
	}
	
	public boolean getMaskAt(IntVector location) {
		if(dimensions.isOnLevel(location)) {
			IntVector arrayLocation = dimensions.mapToArrayCoords(location);
			return mask[arrayLocation.x][arrayLocation.y][arrayLocation.z];
		}
		return false; // TODO: throw exception
	}
	
	public void clear() {
    	for(int i = 0; i < mask.length; i++) {
    		for(int j=0; j < mask[i].length; j++) {
    			Arrays.fill(mask[i][j], false);
    		}
    	}
	}
}
