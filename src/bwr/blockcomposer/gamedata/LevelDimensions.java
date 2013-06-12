package bwr.blockcomposer.gamedata;

import bwr.blockcomposer.types.IntVector;

public class LevelDimensions {
	
	public final IntVector max = new IntVector();
	public final IntVector min = new IntVector();

	private float diameter = -1f;
	
	public LevelDimensions() {
		max.x = Integer.MIN_VALUE;
		max.y = Integer.MIN_VALUE;
		max.z = Integer.MIN_VALUE;

		min.x = Integer.MAX_VALUE;
		min.y = Integer.MAX_VALUE;
		min.z = Integer.MAX_VALUE;
	}
	
	public void expandFor(IntVector location) {
		if(location.x > max.x) max.x = location.x;
		if(location.y > max.y) max.y = location.y;
		if(location.z > max.z) max.z = location.z;
		if(location.x < min.x) min.x = location.x;
		if(location.y < min.y) min.y = location.y;
		if(location.z < min.z) min.z = location.z;
		
		diameter = -1f; // Reset diameter
	}
	
	public LevelDimensions(IntVector max, IntVector min) {
		this.max.copy(max);
		this.min.copy(min);
	}
	
	public int getSizeX() {
		return max.x-min.x+1;
	}
	
	public int getSizeY() {
		return max.y-min.y+1;
	}
	
	public int getSizeZ() {
		return max.z-min.z+1;
	}
	
	public int getCenterX() {
		return (max.x + min.x)/2;
	}
	
	public int getCenterY() {
		return (max.y + min.y)/2;
	}
	
	public int getCenterZ() {
		return (max.z + min.z)/2;
	}
	
	public float getDiameter() {
		if(diameter < 0) {
			if(diameter < getSizeX()) diameter = getSizeX();
			if(diameter < getSizeY()) diameter = getSizeY();
			if(diameter < getSizeZ()) diameter = getSizeZ();
		}
		return diameter;
	}
	
	public IntVector mapToArrayCoords(IntVector v) {
		IntVector r = v.duplicate();
		
		r.sub(min);
		
		return r;
	}
	
	public boolean isOnLevel(IntVector l) {
    	if(l.x > max.x || l.x < min.x || l.y > max.y || l.y < min.y || l.z > max.z || l.z < min.z) return false;
    	return true;
	}
}
