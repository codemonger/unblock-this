package bwr.blockcomposer.modes;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.microedition.khronos.opengles.GL10;

import bwr.blockcomposer.Camera;
import bwr.blockcomposer.GameResources;
import bwr.blockcomposer.OnUpdateAction;
import bwr.blockcomposer.R;
import bwr.blockcomposer.gamedata.Block;
import bwr.blockcomposer.gamedata.GameState;
import bwr.blockcomposer.gamedata.Level;
import bwr.blockcomposer.gamedata.LevelStore;
import bwr.blockcomposer.gamedata.UserLevelStore;
import bwr.blockcomposer.models.AxisRenderer;
import bwr.blockcomposer.models.Model;
import bwr.blockcomposer.models.SelectBoxRenderer;
import bwr.blockcomposer.types.IntVector;
import bwr.blockcomposer.ui.UIElementPicture;
import bwr.blockcomposer.ui.UIElementText;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.EditText;

public class EditMode extends Mode {
    private static final float TOUCH_SCALE_FACTOR = 180.0f / 320;

	private static final int LEVEL_NAME_MAX_LENGTH = 25;
    
	private static final int MOVE_LEFT = 0;
	private static final int MOVE_FORWARD = 1;
	private static final int MOVE_RIGHT = 2;
	private static final int MOVE_BACK = 3;
	
	private final static int A_BUTTON = 5;
	private final static int B_BUTTON = 6;
	private static final int PLAY_BUTTON = 7;
	private final static int UP_BUTTON = 8;
	private final static int DOWN_BUTTON = 9;
	private static final int BLOCK_TYPE_BUTTON = 10;

	@Override
	public int optionsMenu() {
		return R.menu.editormenu;
	}

	private int blockTypeSelector = 0;
    static private final byte blockTypeColor[] = { 
    	Block.GREY,
    	Block.PURPLE, 
    	Block.GREEN, 
    	Block.BLUE, 
    	Block.RED
    };
	final CharSequence[] blockTypeNames = {
			"Floor/Wall", 
			"Purple", 
			"Green",
			"Blue",
			"Red",
	};

	
	public GameMode onStart;
	private TreeMap<IntVector, Block> blocks = new TreeMap<IntVector, Block>();
	private IntVector selectorLocation = new IntVector(0,0,0);
	private IntVector playerStartPosition = new IntVector(0,1,0);
	private String levelFileName = "name.bclevel";
	private String levelName = "";
	
	private LevelStore levelStore;
	
    private final AxisRenderer axis = new AxisRenderer();
    private final SelectBoxRenderer selectBox = new SelectBoxRenderer();

    private float mPreviousX, mAngleX;
    
    Block currentBlockTypeExample = new Block();
    
    public void setAngleRotationX(float r) {
    	this.mAngleX = r;
    }
    
    private final Model playerModel;
    private final Camera camera = new Camera();
    
	private int pmx = 0, pmz = 0, pmy = 0; //Player movement commands

	private Model curvedBlockModel;
	private Model blockModel;
    
	static final private IntVector rules[] = {
    	new IntVector( 1, 0, 0), // RIGHT
    	new IntVector( 0, 0, 1), // BACK
    	new IntVector(-1, 0, 0), // LEFT
    	new IntVector( 0, 0,-1), // FORWARD
	};


    
	public EditMode(ModeController modeController, GameResources gameResources) {
		super(modeController, gameResources);
		
		playerModel = gameResources.loadModel(R.raw.player);
		camera.center.set(0, 0, 0);
		camera.eye.set(0, 10, -15f);
		
		curvedBlockModel = gameResources.loadModel(R.raw.newcube);
		blockModel = gameResources.loadModel(R.raw.block);
	}

	@Override
	public void onModeCreate(GL10 gl, int displayWidth, int displayHeight) {
		super.onModeCreate(gl, displayWidth, displayHeight);
		
		createButtons(gl, displayWidth, displayHeight);
	}
	
	public void editLevel(Level level, String fileName, LevelStore levelStore) {
		blocks = level.getBlockData();
		levelFileName = fileName;
		levelName = level.getName();
		playerStartPosition.copy(level.playerStartLocation);
	}
	
	private Level buildLevel() {
		Level level = Level.buildLevelFromData(blocks, playerStartPosition.duplicate());
		level.setName(levelName);
		
		return level;
	}
	
	private void handleSave(final Context context) {
		AlertDialog.Builder alert = new AlertDialog.Builder(context);

		if(application.isExternalStorageAvailable()) {
			alert.setTitle("Save level as...");
	
			// Set an EditText view to get user input 
			final EditText input = new EditText(context);
			input.setText(levelFileName);
			input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(30)});
	
			alert.setView(input);
	
			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					try {
						levelFileName = input.getText().toString();
						
						if(!levelFileName.endsWith(".bclevel")) levelFileName = levelFileName.concat(".bclevel");
						
						Level level = buildLevel();
						
						File file = new File(application.levelStorageDirectory, levelFileName);
						FileOutputStream fos = new FileOutputStream(file);
						DataOutputStream out = new DataOutputStream(new BufferedOutputStream(fos));
						level.writeToFile(out);
						out.close();
						
						if(levelStore != null && levelStore instanceof UserLevelStore) 
							((UserLevelStore)levelStore).purgeUserLevels();
						
					} catch(IOException e) {
						// TODO: Error
					}
				}
			});
	
			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {
			    // Canceled.
			  }
			});
		} else {
			alert.setTitle("External storage not available");
			alert.setMessage(R.string.ext_storage_not_available);
			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					// Nothing
				}
			});
		}

		alert.show();
	}
	
	@Override
	public boolean onOptionsItemSelected(final Context context, MenuItem item) {
		
		if(item.getItemId() == R.id.saveLevel) {
			
			handleSave(context);
			return true;

		}
		
		
		if(item.getItemId() == R.id.setLevelName) {

			AlertDialog.Builder alert = new AlertDialog.Builder(context);

				alert.setTitle("Set level name...");

				// Set an EditText view to get user input 
				final EditText input = new EditText(context);
				input.setText(levelName);
				alert.setView(input);
				input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(LEVEL_NAME_MAX_LENGTH)});

				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
							levelName = input.getText().toString();
					}
				});

				alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				  public void onClick(DialogInterface dialog, int whichButton) {
				    // Canceled.
				  }
				});

				alert.show();
				return true;

		} else if(item.getItemId() == R.id.centerLevel) {
			updateActions.add(new OnUpdateAction() {
				public void onUpdate(long dt) {
					centerLevel();
				}
			});
		} else if(item.getItemId() == R.id.setPlayerStartLocation) {
			playerStartPosition.copy(selectorLocation);
		} else if(item.getItemId() == R.id.newLevel) {
			updateActions.add(new OnUpdateAction() {
				public void onUpdate(long dt) {
					newLevel();
				}
			});
		}
		
		return false;
	}

	private void centerLevel() {
		IntVector min = new IntVector(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
		IntVector max = new IntVector(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
		
		for(IntVector l : blocks.keySet()) {
			if(l.x > max.x) max.x = l.x;
			if(l.y > max.y) max.y = l.y;
			if(l.z > max.z) max.z = l.z;
			
			if(l.x < min.x) min.x = l.x;
			if(l.y < min.y) min.y = l.y;
			if(l.z < min.z) min.z = l.z;
		}
		
		IntVector offset = new IntVector();
		
		offset.x = -(min.x + max.x)/2;
		offset.y = -(min.y + max.y)/2;
		offset.z = -(min.z + max.z)/2;
		
		TreeMap<IntVector, Block> newBlocks = new TreeMap<IntVector, Block>();
		for(Entry<IntVector, Block> e : blocks.entrySet()) {
			IntVector l = e.getKey().duplicate();
			l.add(offset);
			e.getValue().location.copy(l);
			newBlocks.put(l, e.getValue());
		}
		blocks = newBlocks;
		selectorLocation.add(offset);
		playerStartPosition.add(offset);
	}
	
	@Override
	public void update(long dt) {
		camera.update(dt);
				
		if(!(pmx == 0 && pmz == 0 && pmy == 0)) {
			movePlayer(pmx, pmy, pmz);
			pmx = pmy = pmz = 0;
		}
		
		OnUpdateAction action = null;
		while(!updateActions.isEmpty()) {
			action = updateActions.remove();
			action.onUpdate(dt);
		}
	}
	
	public void newLevel() {
		blocks.clear();
		playerStartPosition.set(0, 1, 0);
		selectorLocation.set(0, 0, 0);
		levelFileName = "name.bclevel";
		levelName = "";
		levelStore = null;
	}

	@Override
	public void render3D(GL10 gl) {
		gl.glPushMatrix();
		gl.glTranslatex(0, -3<<16, 8<<16);
		gl.glRotatef(mAngleX, 0, 1, 0);
		Model model = null;

		int color = -1;
		
		for(Entry<IntVector, Block> e : blocks.entrySet()) {
			Block b = e.getValue();
			IntVector bloc = e.getKey(); // Block Location
			
    		if(model != b.getModel()) {
    			if(model != null) {
    				model.postrender(gl);
    			}
        		model = b.getModel();
        		model.prerender(gl);
    		}
			
			gl.glPushMatrix();
			gl.glTranslatex(bloc.x<<16, bloc.y<<16, bloc.z<<16);

    		if(b.color != color) {
    			color = b.color;
    			b.bindTexture(gl, gameResources);
    		}
    		
	        if(model != null) model.render(gl);
	        
	        gl.glPopMatrix();
		}
		
		gl.glPushMatrix();
		gl.glTranslatex(selectorLocation.x<<16, selectorLocation.y<<16, selectorLocation.z<<16);
		selectBox.render(gl);
		gl.glPopMatrix();

		axis.render(gl);
		
		drawPlayer(gl);
		gl.glPopMatrix();
	}
	
	
	private void drawPlayer(GL10 gl) {
		gl.glColor4x(1<<16, 1<<16, 1<<16, 1<<16);
        gl.glPushMatrix();
        
        gl.glTranslatex(playerStartPosition.x<<16, playerStartPosition.y<<16, playerStartPosition.z<<16);
        
        gl.glTranslatef(0.5f, 0.5f, 0.5f); // rotate about the center of the player
        gl.glTranslatef(-0.5f, -0.5f, -0.5f);
        playerModel.prerender(gl);
        gameResources.bindTexture(gl, R.drawable.player);
		playerModel.render(gl);
		playerModel.postrender(gl);
        gl.glPopMatrix();
	}
	
	private int testDirection() {
		return ((int)((normalizeAngle(mAngleX)+45.0f)/90.0f))%4;
	}
	
	private float normalizeAngle(float a) {
		float r = a;
		while(r<0) {
			r += 360.0f;
		}
		r %= 360.0f;
		return r;
	}
	
	private void handleMovement(int direction) {
		int _x = 0, _z = 0;
		int input = testDirection();

		_x = rules[(input + direction) % 4].x;
		_z = rules[(input + direction) % 4].z;

		pmx = _x;
		pmz = _z;
	}
	
	private void movePlayer(int x, int y, int z) {
		selectorLocation.x += x;
		selectorLocation.z += z;
		selectorLocation.y += y;
	}

	@Override
	protected boolean handleUIEvent(int id, int action) {
		if(action == MotionEvent.ACTION_DOWN) {
			if(id >= MOVE_LEFT && id <= MOVE_BACK) {
				handleMovement(id);
				return true;
			} else if(id == UP_BUTTON) {
				pmy = 1;
				return true;	
			} else if(id == DOWN_BUTTON) {
				pmy = -1;
				return true;
			} else if(id == PLAY_BUTTON) {
				onStart.state = new GameState(buildLevel());
//				onStart.metadata = testMetadata;
				
				onStart.getCamera().eye.copy(camera.eye);
				onStart.setAngleRotationX(mAngleX);
				modeController.pushMode(onStart);

				return true;
			} else if(id == A_BUTTON) {
				// place current block type or remove selected block
				updateActions.add(new OnUpdateAction() {
					public void onUpdate(long dt) {
						if(blocks.containsKey(selectorLocation)) {
							blocks.remove(selectorLocation);
						} else {
							Block newBlock = new Block();
							newBlock.movable = true;
							newBlock.color = blockTypeColor[blockTypeSelector];
							if(newBlock.color == Block.GREY) {
								newBlock.setModel(blockModel);
								newBlock.movable = false;
							}
							else newBlock.setModel(curvedBlockModel);
			
							newBlock.location.copy(selectorLocation);
							blocks.put(newBlock.location.duplicate(), newBlock);
						}
					}
				});
				
				return true;
			} else if(id == B_BUTTON) {
				// change block types
				blockTypeSelector++;
				blockTypeSelector %= blockTypeColor.length;
				
				return true;
			} else if(id == BLOCK_TYPE_BUTTON) {
				changeColorDialog();
				return true;
			}
		}
		
		return false;
	}
	
	private void changeColorDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Block Types");
		builder.setItems(blockTypeNames, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	blockTypeSelector = item;
		    }
		});
		AlertDialog alert = builder.create();	
		alert.show();
	}

	@Override
	public boolean onTouchEvent(int x, int y, int action) {
		if(super.onTouchEvent(x, y, action)) {
			return true;
		}
		if(action == MotionEvent.ACTION_MOVE) {
            float dx = x - mPreviousX;
            mAngleX += dx * TOUCH_SCALE_FACTOR;
        }
        mPreviousX = x;
		return false;
	}
	
	@Override
	public Camera getCamera() {
		return camera;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode) {
			case KeyEvent.KEYCODE_W: handleMovement(MOVE_FORWARD); return true;
			case KeyEvent.KEYCODE_A: handleMovement(MOVE_LEFT); return true;
			case KeyEvent.KEYCODE_S: handleMovement(MOVE_BACK); return true;
			case KeyEvent.KEYCODE_D: handleMovement(MOVE_RIGHT); return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void createButtons(GL10 gl, int displayWidth, int displayHeight) {
		int ix = (int) context.getResources().getDimension(R.dimen.buttonInitX);
		int iy = (int) context.getResources().getDimension(R.dimen.buttonInitY);
		
		int buttonWidth = (int) context.getResources().getDimension(R.dimen.buttonWidth);
		int buttonHeight = (int) context.getResources().getDimension(R.dimen.buttonHeight);
		int paddingX = (int) context.getResources().getDimension(R.dimen.buttonPaddingX);
		int paddingY = (int) context.getResources().getDimension(R.dimen.buttonPaddingY);
		
		UIElementPicture[] gameArrowButtons = new UIElementPicture[4];
		gameArrowButtons[MOVE_LEFT] = new UIElementPicture(MOVE_LEFT, ix, iy+buttonHeight, buttonWidth, buttonHeight, UIElementPicture.BUTTON_LEFT); // LEFT
		gameArrowButtons[MOVE_RIGHT] = new UIElementPicture(MOVE_RIGHT, ix+buttonWidth+buttonWidth+paddingX+paddingX, iy+buttonHeight, buttonWidth, buttonHeight, UIElementPicture.BUTTON_RIGHT); // RIGHT
		gameArrowButtons[MOVE_BACK] = new UIElementPicture(MOVE_BACK, ix+buttonWidth+paddingX, iy+buttonHeight, buttonWidth, buttonHeight, UIElementPicture.BUTTON_DOWN); // DOWN
		gameArrowButtons[MOVE_FORWARD] = new UIElementPicture(MOVE_FORWARD, ix+buttonWidth+paddingX, iy+buttonHeight+paddingY+buttonHeight, buttonWidth, buttonHeight, UIElementPicture.BUTTON_UP); // UP
		
		UIElementText UpButton = new UIElementText(gameResources, context, gl, UP_BUTTON, "Y+", ix, iy+buttonHeight+paddingY+buttonHeight, buttonWidth, buttonHeight, BUTTON_TEXT_SIZE);
		UIElementText DownButton = new UIElementText(gameResources, context, gl, DOWN_BUTTON, "Y-", ix+buttonWidth+buttonWidth+paddingX+paddingX, iy+buttonHeight+paddingY+buttonHeight, buttonWidth, buttonHeight, BUTTON_TEXT_SIZE);
		
		final int playButtonWidth = 100;
		final int playButtonHeight = 64;
		final int playButtonX = paddingX;
		final int playButtonY = displayHeight - paddingY;
		
		UIElementText playButton = new UIElementText(gameResources, context, gl, PLAY_BUTTON, "Play", playButtonX, playButtonY, playButtonWidth, playButtonHeight, BUTTON_TEXT_SIZE);
		
		final int colorButtonWidth = 180;
		final int colorButtonHeight = 64;
		final int colorButtonX = displayWidth - colorButtonWidth - paddingX;
		final int colorButtonY = displayHeight - paddingY;
		
		UIElementText colorButton = new UIElementText(gameResources, context, gl, BLOCK_TYPE_BUTTON, "Block Type", colorButtonX, colorButtonY, colorButtonWidth, colorButtonHeight, BUTTON_TEXT_SIZE);

		final int aButtonWidth = 210;
		final int aButtonX = displayWidth - aButtonWidth - paddingX;
		final int aButtonY = paddingY + buttonHeight;
		
		UIElementText insertDeleteButton = new UIElementText(gameResources, context, gl, A_BUTTON, "Insert/Delete", aButtonX, aButtonY, aButtonWidth, buttonHeight, BUTTON_TEXT_SIZE);

		
		for(UIElementPicture b : gameArrowButtons) {
			buttons.add(b);
		}
		
		uiElements.add(playButton);
		uiElements.add(insertDeleteButton);
		uiElements.add(UpButton);
		uiElements.add(DownButton);
		uiElements.add(colorButton);
	}
	
}
