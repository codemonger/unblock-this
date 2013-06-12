package bwr.blockcomposer.gamedata;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.TreeSet;

import javax.microedition.khronos.opengles.GL10;

import bwr.blockcomposer.AnimationFinishedListener;
import bwr.blockcomposer.GameResources;
import bwr.blockcomposer.R;
import bwr.blockcomposer.misc.Timer;
import bwr.blockcomposer.models.Model;
import bwr.blockcomposer.types.FloatValueVector3;
import bwr.blockcomposer.types.IntVector;

public class GameState {
	private static final int BLOCK_REMOVAL_TIME = 250;
    private static final long PLAYER_MOVE_TIME = 80;
	private static final long GRAVITY_TIME = PLAYER_MOVE_TIME; // Currently must equal player move time
	
	static final private IntVector rules[] = {
    	new IntVector( 1, 0, 0),
    	new IntVector( 0, 0,-1),
    	new IntVector(-1, 0, 0),
    	new IntVector( 0, 0, 1),
    	new IntVector( 0, 1, 0),
    	new IntVector( 0,-1, 0),
    };
	
	private LinkedList<Move> history = new LinkedList<Move>();
	public final IntVector playerPosition = new IntVector();
	private int version;
    private boolean dirty = false;
    
	public int exitText = R.string.default_exit_text;
	public int entryText = -1; // NO ENTRY TEXT
	
	// Copy of the min, max vectors from LevelDimensions for convenience 
	private final IntVector min, max;
    
	private final Level level;
	private Block[][][] blocks;
	private LevelMask mask;
	private final TreeSet<Block> staticBlockSet = new TreeSet<Block>(); // These are the blocks that can't be moved and aren't affected by gravity, etc
	private final TreeSet<Block> blockSet = new TreeSet<Block>();
	private final TreeSet<Block> toBeRemovedBlockSet = new TreeSet<Block>();
	public final FloatValueVector3 playerDisplayPosition = new FloatValueVector3();
	
    private Timer animStepTimer;
    private boolean blockAnimInEffect = false;
	private Move currentMove;
	
	public Level getLevel() {
		return level;
	}

	
    public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public boolean isDirty() {
		return dirty;
	}
	
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public LinkedList<Move> getMoves() {
		return history;
	}
	
	public void setMoves(LinkedList<Move> moves) {
		this.history = moves;
	}
	
    public boolean isBlockAnimInEffect() {
		return blockAnimInEffect;
	}

	public GameState(Level baseLevel) {
		this.level = baseLevel;
		this.min = level.getDimensions().min;
		this.max = level.getDimensions().max;
		
		playerPosition.copy(baseLevel.playerStartLocation);
		playerDisplayPosition.set(playerPosition.x, playerPosition.y, playerPosition.z);
		
		initGameStructures();
	}
	
	public GameState(Level baseLevel, DataInputStream in) throws IOException {		
		this.level = baseLevel;
		this.min = level.getDimensions().min;
		this.max = level.getDimensions().max;
		
		playerPosition.copy(baseLevel.playerStartLocation);
		playerDisplayPosition.set(playerPosition.x, playerPosition.y, playerPosition.z);
		
		version = in.readInt();
		
		initGameStructures();
		
		playerPosition.readFrom(in);
		
		int numMoves = in.readInt();
		
		for(int i = 0; i < numMoves; i++) {
			history.add(Move.readFrom(in));
		}
		
		restoreState();
	}
	
	private void setBlockAt(IntVector l, Block b) {
		if(level.getDimensions().isOnLevel(l)) {
			blocks[l.x-level.getDimensions().min.x][l.y-level.getDimensions().min.y][l.z-level.getDimensions().min.z] = b;
		}
	}
	
	public void initGameStructures() {
		int numBlocks = level.getBlocks().size();
		
		if(numBlocks > 0) {
			blocks = new Block[level.getDimensions().getSizeX()][level.getDimensions().getSizeY()][level.getDimensions().getSizeZ()];
			mask = new LevelMask(level.getDimensions());
		}
		
		for(Block b : level.getBlocks()) {
			if(b.movable) {
				blockSet.add(b);
			} else {
				staticBlockSet.add(b);
			}
			setBlockAt(b.location, b);
		}
	}
	
	private Block getBlockAt(IntVector l) {
		if(level.getDimensions().isOnLevel(l)) {
			IntVector l2 = level.getDimensions().mapToArrayCoords(l);
			return blocks[l2.x][l2.y][l2.z];
		}
		return null;
	}
	
    private Block getBlockAt(int x, int y, int z) {
    	return getBlockAt(new IntVector(x,y,z));
    }
	
    private int pool(Block b, LevelMask mask, ArrayList<IntVector> touched) {
    	IntVector next = new IntVector();
    	int sum = 1;
    	
    	mask.setMaskAt(b.location, true);
    	touched.add(b.location.duplicate());
    	
    	for(int r = 0 ; r < rules.length; r++) {
    		next.copy(b.location);
    		next.add(rules[r]);
    		
       		if(isOnMap(next) && !mask.getMaskAt(next)) {

    			Block nb = getBlockAt(next);
    			
    			if(nb != null && nb.color == b.color) {
    				sum += pool(nb, mask, touched);
    			}
    		}
    	}
    	
    	return sum;
    }
   
	private void moveBlock(final Block b, int x, int z, final Move m) {
		Move.BlockMovement blockMovement = new Move.BlockMovement();
		final int nx = b.location.x + x;
		final int nz = b.location.z + z;
		final int ny = b.location.y;
				
		blockMovement.from.copy(b.location);
		
		setBlockAt(b.location, null);
		b.location.set(nx, ny, nz);
		blockMovement.to.copy(b.location);
		setBlockAt(b.location, b);
		b.displayLocation.animateTo(nx, ny, nz, PLAYER_MOVE_TIME);
		
		m.steps.addFirst(blockMovement);
		
	}
		
    private boolean canLocationAccept(int x, int y, int z) {
    	if(!isOnMap(x, y, z)) return false;
    	
    	Block b = getBlockAt(x, y, z);
    	if(b != null) return false;
    	
    	for(int iy = y-1; iy >= level.getDimensions().min.y; iy--) {
    		b = getBlockAt(x, iy, z);
    		if(b != null) {
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    private int removeGroups(final Move move) {
    	int numRemoved = 0;

    	mask.clear();
    	
    	ArrayList<IntVector> removals = new ArrayList<IntVector>();
    	ArrayList<IntVector> touched = new ArrayList<IntVector>(6);
    	
    	final IntVector L = new IntVector();
    	
    	final IntVector I = new IntVector(1,0,0);
    	final IntVector J = new IntVector(0,1,0);
    	final IntVector K = new IntVector(0,0,1);
    	
    	for(L.x=min.x;L.x<=max.x;L.add(I)) for(L.y=min.y;L.y<=max.y;L.add(J)) for(L.z=min.z;L.z<=max.z;L.add(K)) {
    		if(!mask.getMaskAt(L)) {
    			Block b = getBlockAt(L);
				if(b != null && b.movable == true) {
					int v = pool(b, mask, touched);
					if(v >= 4) {
						removals.addAll(touched);
					}
					touched.clear();
				}
    		}
    	}
    	
    	Move.BlockRemoval removal;
    	IntVector v;
    	for(int i = 0; i < removals.size() ; i++) {
    		removal = new Move.BlockRemoval();
    		v = removals.get(i);
    		removal.removedBlock = getBlockAt(v);
    		removal.removedBlock.alpha.initLinearAnimation(-1.0f, BLOCK_REMOVAL_TIME);
    		setBlockAt(v, null);
    		move.steps.addFirst(removal);
    		
    		toBeRemovedBlockSet.add(removal.removedBlock);
    		blockSet.remove(removal.removedBlock);
    		numRemoved++;
    	}
    	return numRemoved;
    }
    
    private int reverts = 0;
    
    public void revertAllMoves() {
    	reverts = history.size();
    }
    
    public void revertLastMove() {
    	reverts++;
    }
    
    private void revertLastMove_actualWork() {
    	boolean significant = false;
		dirty = true;

    	while(!significant && !history.isEmpty()) {
    		Move lastMove = history.removeLast();
    		playerPosition.copy(lastMove.previousPlayerLocation);
    		playerDisplayPosition.set(playerPosition.x, playerPosition.y, playerPosition.z);
    		
    		for(Move.Step s : lastMove.steps) {
    			significant = true;
    			if(s instanceof Move.BlockMovement) {
    				Move.BlockMovement movement = (Move.BlockMovement) s;
        			Block b = getBlockAt(movement.to);
        			b.location.copy(movement.from);
        			b.displayLocation.set(b.location.x, b.location.y, b.location.z);
        			setBlockAt(movement.to, null);
        			setBlockAt(b.location, b);
    			} else {
    				Move.BlockRemoval removal = (Move.BlockRemoval) s;
    				Block b = removal.removedBlock;
    				setBlockAt(removal.removedBlock.location, removal.removedBlock);
        			b.alpha.setValue(1.0f);
    				blockSet.add(removal.removedBlock);
    			}
    		}
    	}
    }
    
	private void restoreState() {
		
		playerDisplayPosition.set(playerPosition.x, playerPosition.y, playerPosition.z);
		
		for(Move m : history) {
			Collections.reverse(m.steps);
			for(Move.Step s : m.steps) {
    			if(s instanceof Move.BlockMovement) {
    				Move.BlockMovement movement = (Move.BlockMovement) s;
        			Block b = getBlockAt(movement.from);
        			b.location.copy(movement.to);
        			b.displayLocation.set(b.location.x, b.location.y, b.location.z);
        			setBlockAt(movement.from, null);
        			setBlockAt(b.location, b);
    			} else {
    				Move.BlockRemoval removal = (Move.BlockRemoval) s;
    				removal.removedBlock = getBlockAt(removal.removedBlock.location);
    				setBlockAt(removal.removedBlock.location, null);
    				blockSet.remove(removal.removedBlock);
    			}
			}
			Collections.reverse(m.steps);
		}
	}
	
    private void gravityPhase(final boolean removeGroupsHasBeenRun) {
    	// Finish up the remove groups phase?
		final LinkedList<Block> removals = new LinkedList<Block>();
		
		for(Block b : toBeRemovedBlockSet) {
			if(b.alpha.getValue() <= 0.001f) {
				removals.add(b);
			}
		}
		
		for(Block b : removals) {
			toBeRemovedBlockSet.remove(b);
			setBlockAt(b.location, null);
		}
    	
//		boolean gravityEffects = false;
//		
//		if(gravity(currentMove) > 0) {
//			gravityEffects = true;
//		}
		
		if(gravity(currentMove) > 0) {
    		animStepTimer = new Timer(GRAVITY_TIME, new AnimationFinishedListener() {
				public void onAnimationFinished() {
					removeGroupsPhase();
				}
			});
		} else if (!removeGroupsHasBeenRun) { // Want to run removeGroups at least once
			removeGroupsPhase();

		} else { // Animation is finished
    		blockAnimInEffect = false;
    		history.addLast(currentMove);
    		currentMove = null;
		}
    }
    
    private void removeGroupsPhase() {
    	boolean blocksRemoved = false;
    	
    	// TODO: switch back to while
    	if(removeGroups(currentMove) > 0) blocksRemoved = true;
    	
    	if(blocksRemoved) {
			animStepTimer = new Timer(BLOCK_REMOVAL_TIME, new AnimationFinishedListener() {
				public void onAnimationFinished() {
					gravityPhase(true);
				}
			});
    	} else {
    		gravityPhase(true);
    	}
    }
    
    private boolean groundBelow(final int x, final int y, final int z) {
    	for(int i = y-1; i >= min.y; i--) {
    		Block b = getBlockAt(x, i, z);
    		if(b != null) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public void movePlayer(int x, int z) {
    	currentMove = new Move();

		currentMove.previousPlayerLocation.copy(playerPosition);
    	
		// Current Player Position
    	final int px = playerPosition.x;
    	final int py = playerPosition.y;
    	final int pz = playerPosition.z;
    	
    	// Next Player Position
    	final int nx = playerPosition.x+x;
    	final int ny = playerPosition.y;
    	final int nz = playerPosition.z+z;
    	
    	// Position block would be pushed into if block existed at new player position
    	final int nnx = nx+x;
    	final int nny = ny;
    	final int nnz = nz+z;
    	
    	if(!isOnMap(nx,ny,nz)) return;
    	
    	// check if next location is empty
    	Block b = getBlockAt(nx,ny,nz);
    	
    	if(b == null) {
    		if(!groundBelow(nx, ny, nz)) return; // Don't let player walk off the map
    		dirty = true;
    		
    		blockAnimInEffect = true;
			playerPosition.set(nx, ny, nz);
			playerDisplayPosition.animateTo(nx, ny, nz, PLAYER_MOVE_TIME);
			animStepTimer = new Timer(PLAYER_MOVE_TIME, new AnimationFinishedListener() {
				public void onAnimationFinished() {
					gravityPhase(false);
				}
			});
    	} else if(b.movable == true && canLocationAccept(nnx,nny,nnz)) {
    		dirty = true;

    		blockAnimInEffect = true;
			moveBlock(b, x, z, currentMove);
			playerPosition.set(nx, ny, nz);
			playerDisplayPosition.animateTo(nx, ny, nz, PLAYER_MOVE_TIME);
			animStepTimer = new Timer(PLAYER_MOVE_TIME, new AnimationFinishedListener() {
				public void onAnimationFinished() {
					gravityPhase(false);
				}
			});

    	} else if(getBlockAt(nx, ny+1, nz) == null && getBlockAt(px,py+1,pz) == null) { // This can cause no effects
    		dirty = true;

    		blockAnimInEffect = true;
			playerPosition.set(nx, ny+1, nz);
	    	history.addLast(currentMove);
    		currentMove = null;
			playerDisplayPosition.animateToWithCallback(px, ny+1, pz, PLAYER_MOVE_TIME, new AnimationFinishedListener() {
				public void onAnimationFinished() {
					playerDisplayPosition.animateToWithCallback(nx, ny+1, nz, PLAYER_MOVE_TIME, new AnimationFinishedListener() {
						public void onAnimationFinished() {
				    		blockAnimInEffect = false;
						}
					});
				}
			});
    	}
    }
    
    
    private boolean playerGravity() {
    	final int px = playerPosition.x;
    	final int py = playerPosition.y;
    	final int pz = playerPosition.z;
    	
    	int my = py;
    	for(int y = py-1; y >= min.y && getBlockAt(px, y, pz) == null; y--) {
    		my = y;
    	}
    	
    	final int ny = my;
    	
    	if(ny != py) {
			playerPosition.set(px, ny, pz);

			playerDisplayPosition.animateToWithCallback(px, ny, pz, PLAYER_MOVE_TIME, new AnimationFinishedListener() {
				public void onAnimationFinished() {
				}
			});
			return true;
    	}
    	return false;
    }

    private int gravity(final Move move) {
    	int numMoved = 0;
    	Move.BlockMovement gravityEffect;
    	
    	for(Block b : blockSet) {
    		
			int y = b.location.y;
			while(y-1>=min.y && (getBlockAt(b.location.x, y-1, b.location.z) == null && (playerPosition.x != b.location.x || playerPosition.y != y-1 || playerPosition.z != b.location.z))) y--;
			Block floor = getBlockAt(b.location.x, y-1, b.location.z);

			if(y != b.location.y && floor != null) {
				gravityEffect = new Move.BlockMovement();
								
				gravityEffect.from.copy(b.location);

				setBlockAt(b.location, null);
				
				b.location.y = y;
				gravityEffect.to.copy(b.location);

				setBlockAt(b.location, b);
				
				b.displayLocation.animateTo(b.location.x, y, b.location.z, PLAYER_MOVE_TIME);
				
				move.steps.addFirst(gravityEffect);
				
				numMoved++;
			}
    	}
    	
    	if(playerGravity()) {
    		numMoved++;
    	}
    	
    	return numMoved;
    }

    public boolean hasPlayerWon() {	
    	return blockSet.isEmpty() && toBeRemovedBlockSet.isEmpty();
    }
    
	public void update(long dt) {
		
		playerDisplayPosition.update(dt);
		
		for(Block b : toBeRemovedBlockSet) {
				b.update(dt);
		}
		
		if(blockAnimInEffect) {
			if(animStepTimer != null) 
				animStepTimer.update(dt);
		} else {
			
			// Only allow reverts when block animation is not in effect
			while(reverts > 0) {
				reverts--;
				revertLastMove_actualWork();
			}
		}
		
		for(Block b : blockSet) {
       		b.update(dt);
		}
	}
	
    public void draw(GL10 gl, GameResources gameResources) {
    	Model model = null;
    	int color = -1;
    	
    	gl.glColor4x(1<<16, 1<<16, 1<<16, 1<<16);
    	
    	// Draw all static blocks
    	if(!staticBlockSet.isEmpty()) {
    		model = staticBlockSet.first().getModel();
    		
    		model.prerender(gl);

	    	for(Block b : staticBlockSet) {
	    		
	    		if(b.color != color) {
	    			color = b.color;
	    			b.bindTexture(gl, gameResources);
	    		}
	    		
		        gl.glPushMatrix();
		        gl.glTranslatex(b.location.x<<16, b.location.y<<16, b.location.z<<16);
		        model.render(gl);
		        gl.glPopMatrix();
	    	}
	    	
	    	model.postrender(gl);
	    	model = null;
    	}
    	
    	for(Block b : blockSet) {
        		if(model != b.getModel()) {
        			if(model != null) {
        				model.postrender(gl);
        			}
            		model = b.getModel();
            		model.prerender(gl);
        		}
		        gl.glPushMatrix();
		        gl.glTranslatef(b.displayLocation.getX(), b.displayLocation.getY(), b.displayLocation.getZ());

	    		if(b.color != color) {
	    			color = b.color;
	    			b.bindTexture(gl, gameResources);
	    		}
		        
		        model.render(gl);
		        gl.glPopMatrix();
        }
    	
    	if(model != null) {
    		model.postrender(gl);
            model = null;
    	}
        
    	if(!toBeRemovedBlockSet.isEmpty()) {
    		gl.glDisable(GL10.GL_LIGHTING);
    		
    		for(Block b : toBeRemovedBlockSet) {
    			
        		if(model != b.getModel()) {
        			if(model != null) {
        				model.postrender(gl);
        			}
            		model = b.getModel();
            		model.prerender(gl);
        		}
        		
		        gl.glPushMatrix();
		        gl.glTranslatef(b.displayLocation.getX(), b.displayLocation.getY(), b.displayLocation.getZ());
		        
	    		if(b.color != color) {
	    			color = b.color;
	    			b.bindTexture(gl, gameResources);
	    		}
		        
		        float alpha = b.alpha.getValue();
		        gl.glColor4f(1.0f, 1.0f, 1.0f, alpha);
		        
		        model.render(gl);
		        gl.glPopMatrix();
		        
	    	}
    		
	        gl.glEnable(GL10.GL_LIGHTING);
	    	model.postrender(gl);
	    	model = null;
	    	gl.glColor4x(1<<16, 1<<16, 1<<16, 1<<16);
    	}
    }
    
    private boolean isOnMap(int x, int y, int z) {
    	return level.getDimensions().isOnLevel(new IntVector(x,y,z));
    }
    
    private boolean isOnMap(IntVector v) {
    	return level.getDimensions().isOnLevel(v);
    }
	
	public void writeTo(DataOutputStream out) throws IOException {
		out.writeInt(version);
		playerPosition.writeTo(out);
		out.writeInt(history.size());
		for(Move m : history) {
			m.writeTo(out);
		}
	}

}
