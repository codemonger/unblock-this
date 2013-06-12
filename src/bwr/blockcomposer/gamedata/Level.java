package bwr.blockcomposer.gamedata;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.TreeMap;

import bwr.blockcomposer.GameResources;
import bwr.blockcomposer.models.Model;
import bwr.blockcomposer.types.IntVector;

public class Level {
	private String name;
	private String author;

	int version;

	public final IntVector playerStartLocation = new IntVector();
	private ArrayList<Block> blocks;
	
	private LevelDimensions dimensions;

	public LevelDimensions getDimensions() {
		return dimensions;
	}
	
	public ArrayList<Block> getBlocks() {
		return blocks;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private static Block readBlock(DataInputStream in, Model staticBlockModel, Model movableBlockModel) throws IOException {
		Block result = new Block();

		result.color = in.readInt();
		result.movable = in.readInt() == 1 ? true : false;
		
		if(result.movable) {
			result.setModel(movableBlockModel);
		} else {
			result.setModel(staticBlockModel);
		}
		
		result.location.x = in.readInt();
		result.location.y = in.readInt();
		result.location.z = in.readInt();
		
		result.displayLocation.set(result.location.x, result.location.y, result.location.z);
		
		return result;
	}
	
	private static void writeBlock(DataOutputStream out, Block block) throws IOException {
		out.writeInt(block.color);
		
		if(block.movable) out.writeInt(1);
		else out.writeInt(0);
		
		out.writeInt(block.location.x);
		out.writeInt(block.location.y);
		out.writeInt(block.location.z);
	}
	
	public void writeToFile(DataOutputStream out) throws IOException {
		int version = 0;
		out.writeByte(version);
		
		byte[] buffer = new byte[255];
		
		byte[] nameBytes = name.getBytes();
		for(int i=0;i<nameBytes.length&&i<255;i++) {
			buffer[i] = nameBytes[i];
		}
		out.write(buffer);
		
		out.writeInt(playerStartLocation.x);		
		out.writeInt(playerStartLocation.y);		
		out.writeInt(playerStartLocation.z);
		
		out.writeInt(blocks.size());
		
		for(Block b : blocks) {
			writeBlock(out, b);
		}
	}
	
	public static Level loadFromFile(InputStream in, GameResources gameResources) throws IOException {
		Level level = new Level();
		
		Model movableBlockModel = gameResources.loadModel(Block.MOVABLE_BLOCK_MODEL);
		Model staticBlockModel = gameResources.loadModel(Block.STATIC_BLOCK_MODEL);
		
		DataInputStream dataInput = new DataInputStream(new BufferedInputStream(in));
		
		byte[] buffer = new byte[256];
				
		dataInput.readFully(buffer, 0, 256);
		
		level.name = new String(buffer).trim();
		if("".equals(level.name)) level.name = "";
		
		level.playerStartLocation.x = dataInput.readInt();
		level.playerStartLocation.y = dataInput.readInt();
		level.playerStartLocation.z = dataInput.readInt();
		
		int numBlocks = dataInput.readInt();
		level.blocks = new ArrayList<Block>(numBlocks);

		level.dimensions = new LevelDimensions();
		
		for(int i = 0; i < numBlocks; i++) {
			Block b = readBlock(dataInput, staticBlockModel, movableBlockModel);
			
			level.dimensions.expandFor(b.location);
			
			level.blocks.add(i, b);
		}
		
		if(level.blocks.size() == 0) {
			IntVector zero = new IntVector();
			level.dimensions.min.copy(zero);
			level.dimensions.max.copy(zero);
		}
		
		level.dimensions.max.y++; // This prevents the player from getting stuck if on top of a top level block				
		
		return level;
	}
    
	public static Level buildLevelFromData(TreeMap<IntVector, Block> blocks, IntVector playerPosition) {
		Level level = new Level();
		
		level.name = "";
		level.playerStartLocation.copy(playerPosition);
		
		level.dimensions = new LevelDimensions();
		
		level.dimensions.expandFor(level.playerStartLocation);

		level.blocks = new ArrayList<Block>(blocks.size());
		
		for(Block b : blocks.values()) {
			level.dimensions.expandFor(b.location);
			
			b.displayLocation.copy(b.location);
			
			level.blocks.add(b);
		}
		
		if(level.blocks.size() == 0) {
			IntVector zero = new IntVector();
			level.dimensions.min.copy(zero);
			level.dimensions.max.copy(zero);
		}
		
		level.dimensions.max.y++; // This prevents the player from getting stuck if on top of a top level block
		
		return level;
	}
	
	public TreeMap<IntVector, Block> getBlockData() {
		TreeMap<IntVector, Block> blockData = new TreeMap<IntVector, Block>();
				
		for(Block b : blocks) {
			blockData.put(b.location.duplicate(), b.duplicate());
		}
		
		return blockData;
	}
	
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

}
