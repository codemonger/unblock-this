package bwr.blockcomposer.modes;

import javax.microedition.khronos.opengles.GL10;

import bwr.blockcomposer.Camera;
import bwr.blockcomposer.GameResources;
import bwr.blockcomposer.R;
import bwr.blockcomposer.gamedata.GameState;
import bwr.blockcomposer.models.Model;
import bwr.blockcomposer.types.FloatValue;
import bwr.blockcomposer.types.IntVector;
import bwr.blockcomposer.ui.UIElementPicture;
import bwr.blockcomposer.ui.UIElementText;

import android.content.Context;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;

public class GameMode extends Mode {
    private static final float TOUCH_SCALE_FACTOR = 180.0f / 320;

	private static final int MOVE_LEFT = 0;
	private static final int MOVE_FORWARD = 1;
	private static final int MOVE_RIGHT = 2;
	private static final int MOVE_BACK = 3;
	private static final int UNDO_BUTTON = 4;
	private static final int ZOOM_BUTTON = 5;
	
	private static final float ZOOM_LEVEL0 = 0.9f;
	private static final float ZOOM_LEVEL1 = 1.3f;
	private static final float ZOOM_LEVEL2 = 1.7f;

	
	public GameState state;
	
    private float mPreviousX, mAngleX;
    private int zoomMode = 0;
    
    public void setAngleRotationX(float r) {
    	this.mAngleX = r;
    }

    private float playerRotation = 0;
    private FloatValue playerDisplayRotation = new FloatValue(0);
    
    private final Model playerModel;
    
    private final Camera camera = new Camera();

	@Override
	public int optionsMenu() {
		return R.menu.gamemenu;
	}
	
	@Override
	public boolean onOptionsItemSelected(Context context, MenuItem item) {
		if(item.getItemId() == R.id.resetLevel) {
			state.revertAllMoves();
			return true;
		}
	    return false;
	}

	private int pmx = 0, pmy = 0; //Player movement commands
    
	static final private IntVector rules[] = {
    	new IntVector( 1, 0, 0), // RIGHT
    	new IntVector( 0, 0, 1), // BACK
    	new IntVector(-1, 0, 0), // LEFT
    	new IntVector( 0, 0,-1), // FORWARD
	};
    
	public GameMode(ModeController modeController, GameResources gameResources) {
		super(modeController, gameResources);
		
		playerModel = gameResources.loadModel(R.raw.player);
		camera.center.set(0, 0, 0);
		camera.eye.set(0, 10, -15f);
	}

	@Override
	public void onModePop() {
		
	}

	@Override
	public void onModeCreate(GL10 gl, int displayWidth, int displayHeight) {
		super.onModeCreate(gl, displayWidth, displayHeight);
		
		createButtons(gl, displayWidth, displayHeight);
	}
	
	@Override
	public void update(long dt) {
		camera.update(dt);
		state.update(dt);
		
		playerDisplayRotation.update(dt);
		
		if(!(pmx == 0 && pmy == 0) && !state.isBlockAnimInEffect()) {
			movePlayer(pmx, pmy);
			pmx = pmy = 0;
		}
		
		if(state.hasPlayerWon()) {
			if(modeController.getCurrentMode().getId() == getId()) { 
				modeController.popMode();
				
				String exitText = context.getString(state.exitText);
				if(exitText == null) {
					exitText = "Congratulations!";
				}
				
				modeController.pushMode(new GameDialogMode(this, exitText, new GameDialogMode.OnResult() {
					public void onResult() {
						state.revertAllMoves();
						state.update(0); // Update because revert doesn't take effect until next update is called
					}
				}));
			}
		}
	}
	
	private void toggleZoom() {
		zoomMode = (zoomMode+1)%3;
		updateZoom();
	}
	
	private void updateZoom() {
		switch(zoomMode) {
		case 0:
			camera.zoom.setValueOverTime(ZOOM_LEVEL0, 150);
			break;
		case 1:
			camera.zoom.setValueOverTime(ZOOM_LEVEL1, 150);
			break;
		case 2:
			camera.zoom.setValueOverTime(ZOOM_LEVEL2, 150);
			break;
		default:
			camera.zoom.setValue(ZOOM_LEVEL0);
			break;
		}
	}

	@Override
	public void render3D(GL10 gl) {
		gl.glPushMatrix();
		//gl.glTranslatex(0, -3<<16, 8<<16); // zoom
		gl.glRotatef(mAngleX, 0, 1, 0);	
		state.draw(gl, gameResources);
		drawPlayer(gl, state);
		gl.glPopMatrix();
	}
	
	private void drawPlayer(GL10 gl, GameState state) {
		gl.glColor4f(1, 1, 1, 1);
        gl.glPushMatrix();
        
        gl.glTranslatef(state.playerDisplayPosition.getX(), state.playerDisplayPosition.getY(), state.playerDisplayPosition.getZ());
        
        gl.glTranslatef(0.5f, 0.5f, 0.5f); // rotate about the center of the player
        gl.glRotatef(playerDisplayRotation.getValue(), 0, 1, 0);
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
		int _x = 0, _y = 0;
		int input = testDirection();

		_x = rules[(input + direction) % 4].x;
		_y = rules[(input + direction) % 4].z;

		pmx = _x;
		pmy = _y;
	}
	
	private void movePlayer(int x, int y) {
		if (!(x == 0 && y == 0)) {
			state.movePlayer(x, y);
			
		    if(x == 0 && y == 1 && playerRotation != 0) {
		        playerDisplayRotation.initLinearAnimation(0 - playerRotation, 100);
		        playerRotation = 0;
	        } else if (x == 1 && y == 0 && playerRotation != 90) {
		        playerDisplayRotation.initLinearAnimation(90 - playerRotation, 100);
		        playerRotation = 90;
	        } else if (x == 0 && y == -1 && playerRotation != 180) {
		        playerDisplayRotation.initLinearAnimation(180 - playerRotation, 100);
		        playerRotation = 180;
	        } else if (x == -1 && y == 0 && playerRotation != 270) {
		        playerDisplayRotation.initLinearAnimation(270 - playerRotation, 100);
		        playerRotation = 270;
	        }
		}
	}

	@Override
	protected boolean handleUIEvent(int id, int action) {
		if(action == MotionEvent.ACTION_DOWN) {
			if(id >= MOVE_LEFT && id <= MOVE_BACK) {
				handleMovement(id);
				return true;
			} else if(id == UNDO_BUTTON) {
				state.revertLastMove();
				return true;
			} else if(id == ZOOM_BUTTON) {
				toggleZoom();
				return true;
			}
		}
		
		return false;
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
		
		final int undoButtonWidth = 100;
		final int undoButtonHeight = 64;
		final int undoButtonX = displayWidth - undoButtonWidth - paddingX;
		final int undoButtonY = displayHeight - paddingY;
		
		final int zoomButtonWidth = 100;
		final int zoomButtonHeight = 64;
		final int zoomButtonX = paddingX;
		final int zoomButtonY = displayHeight - paddingY;
		
		UIElementText undoButton = new UIElementText(gameResources, context, gl, UNDO_BUTTON, "Undo", undoButtonX, undoButtonY, undoButtonWidth, undoButtonHeight, BUTTON_TEXT_SIZE);
		
		UIElementText zoomButton = new UIElementText(gameResources, context, gl, ZOOM_BUTTON, "Zoom", zoomButtonX, zoomButtonY, zoomButtonWidth, zoomButtonHeight, BUTTON_TEXT_SIZE);

		
		for(UIElementPicture b : gameArrowButtons) {
			buttons.add(b);
		}
		
		uiElements.add(undoButton);
		uiElements.add(zoomButton);
	}
	
}
