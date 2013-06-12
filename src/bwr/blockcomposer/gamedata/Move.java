package bwr.blockcomposer.gamedata;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import bwr.blockcomposer.types.IntVector;

public class Move { 
	private static final byte BLOCK_MOVEMENT_ID = 0;
	private static final byte BLOCK_REMOVAL_ID = 1;

	public final IntVector previousPlayerLocation = new IntVector();
	public final LinkedList<Step> steps = new LinkedList<Step>();
	
	static public interface Step {}
	
	static public class BlockMovement implements Step {
		public final IntVector from = new IntVector();
		public final IntVector to = new IntVector();
	}
	
	static public class BlockRemoval implements Step {
		public Block removedBlock;
	}
	
	public static Move readFrom(DataInputStream in) throws IOException {
		Move m = new Move();
		m.previousPlayerLocation.readFrom(in);
		
		int numSteps = in.readInt();
		byte type;
		for(int i = 0; i < numSteps; i++) {
			type = in.readByte();
			if(type == BLOCK_MOVEMENT_ID) {
				BlockMovement movement = new BlockMovement();
				movement.from.readFrom(in);
				movement.to.readFrom(in);
				m.steps.add(movement);
			} else if(type == BLOCK_REMOVAL_ID) {
				BlockRemoval removal = new BlockRemoval();
				removal.removedBlock = Block.readFrom(in);
				m.steps.add(removal);
			}
		}
		
		return m;
	}
	
	public void writeTo(DataOutputStream out) throws IOException {
		previousPlayerLocation.writeTo(out);
		out.writeInt(steps.size());
		for(Step s : steps) {
			if(s instanceof BlockMovement) {
				out.writeByte(BLOCK_MOVEMENT_ID);
				BlockMovement movement = (BlockMovement) s;
				movement.from.writeTo(out);
				movement.to.writeTo(out);
			} else if(s instanceof BlockRemoval) {
				out.writeByte(BLOCK_REMOVAL_ID);
				BlockRemoval removal = (BlockRemoval) s;
				removal.removedBlock.writeTo(out);
			}
		}
	}
}
