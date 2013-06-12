package bwr.blockcomposer.gamedata;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.khronos.opengles.GL10;

import bwr.blockcomposer.GameResources;
import bwr.blockcomposer.R;
import bwr.blockcomposer.types.FloatValue;
import bwr.blockcomposer.types.FloatValueVector3;
import bwr.blockcomposer.types.IntVector;

public class Block extends GameEntity implements Comparable<Block> {

	private static final int LEGACY_JUNK = 0;
	
	public static final byte RED = 0;
	public static final byte BLUE = 1;
	public static final byte GREEN = 2;
//	public static final byte ORANGE = 3; // UNUSED
	public static final byte PURPLE = 4;
//	public static final byte PINK = 5;  // UNUSED
//	public static final byte WHITE = 6; // UNUSED
	public static final byte GREY = 7;
//	public static final byte YELLOW = 8; // UNUSED
//	public static final byte BROWN = 9; // UNUSED
	public static final byte WALL = 10;
	
	public static final int STATIC_BLOCK_MODEL = R.raw.block;
	public static final int MOVABLE_BLOCK_MODEL = R.raw.newcube;
		
    static private final int ctextures[] = {
    	R.drawable.newred, // RED
    	R.drawable.newblue, // BLUE
    	R.drawable.newgreen, // GREEN
    	LEGACY_JUNK,     // ORANGE
    	R.drawable.newpurple, // PURPLE
    	LEGACY_JUNK, // PINK
    	LEGACY_JUNK,     // WHITE
        R.drawable.floor, // GREY
        LEGACY_JUNK, // YELLOW
        LEGACY_JUNK, // BROWN
        R.drawable.wall, // WALL
    };
	
	public int color;
	public boolean movable;
	public final IntVector location = new IntVector();
	public final FloatValueVector3 displayLocation = new FloatValueVector3();
	public final FloatValue alpha = new FloatValue(1.0f);

	public Block duplicate() {
		Block b = new Block();
		b.model = model;
		b.color = color;
		b.movable = movable;
		b.location.copy(location);
		b.displayLocation.copy(displayLocation);
		b.alpha.setValue(alpha.getValue());
		
		return b;
	}
	
	public void update(long dt) {
		displayLocation.update(dt);
		alpha.update(dt);
	}
	
	public void bindTexture(GL10 gl, GameResources gameResources) {
		gameResources.bindTexture(gl, ctextures[color]);
	}

	public int compareTo(Block another) {
		int typeCompare = color - another.color;
		if(typeCompare != 0) return typeCompare;
		return hashCode() - another.hashCode();
	}
	
	public static Block readFrom(DataInputStream in) throws IOException {
		Block b = new Block();
		b.color = in.readInt();
		b.movable = in.readBoolean();
		b.location.readFrom(in);
		return b;
	}
	
	public void writeTo(DataOutputStream out) throws IOException {
		out.writeInt(color);
		out.writeBoolean(movable);
		location.writeTo(out);
	}
}
