package bwr.blockcomposer.modes;

import javax.microedition.khronos.opengles.GL10;

import bwr.blockcomposer.AnimationFinishedListener;
import bwr.blockcomposer.Camera;
import bwr.blockcomposer.GameResources;
import bwr.blockcomposer.R;
import bwr.blockcomposer.gamedata.GameState;
import bwr.blockcomposer.gamedata.LevelStore;
import bwr.blockcomposer.types.FloatValueVector3;
import bwr.blockcomposer.types.RotatingFloat;
import bwr.blockcomposer.ui.UIElementPicture;
import bwr.blockcomposer.ui.UIElementText;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.MenuItem;
import android.view.MotionEvent;

public class LevelSelectMode extends Mode implements AnimationFinishedListener {
	private static final int NEXT_LEVEL_BUTTON = 0;
	private static final int PREVIOUS_LEVEL_BUTTON = 1;
	private static final int START_BUTTON = 2;
	
	private static final int CAMERA_TRANSITION_SPEED = 200;
	
	private static final int MAP_STATIC_DISP = 0;
	private static final int MAP_TRANSITION_ANIM = 1;
	
	private static final String NO_USER_LEVELS_INSTALLED = "No user levels installed.";
	
	private UIElementPicture nextLevelButton, previousLevelButton;
	private UIElementText startButton, levelName, authorName;

	private Camera camera = new Camera();
	public GameMode gameMode;
	
	private LevelStore levelStore;
	private GameState prevMap, nextMap;
	
	private int displayMode = MAP_STATIC_DISP;
	
	private final RotatingFloat rotateMap = new RotatingFloat(360, 10000);
	private int rotationModifier = 0;
	
	private final FloatValueVector3 prevMapLocation = new FloatValueVector3();
	private final FloatValueVector3 nextMapLocation = new FloatValueVector3();
	
	public int optionsMenu = super.optionsMenu();
	
	private boolean firstActive = true;

	@Override
	public void onModeBecomeActive(GL10 gl) {
		super.onModeBecomeActive(gl);
		
		if(firstActive) {
			firstActive = false;
			return;
		}
		
		refreshLevelStore();
		
		if(!levelStore.isStoreEmpty()) {
			startButton.hidden = false;
			nextLevelButton.hidden = false;
			previousLevelButton.hidden = false;
			
			levelName.setText(levelStore.getCurrent().getLevel().getName());
		} else {
			startButton.hidden = true;
			nextLevelButton.hidden = true;
			previousLevelButton.hidden = true;
			
			levelName.setText(NO_USER_LEVELS_INSTALLED);
		}
	}
	
	private void refreshLevelStore() {
		levelStore.reset();
	}

	public LevelSelectMode(ModeController modeController, GameResources gameResources, LevelStore levelStore) {
		super(modeController, gameResources);
		
		this.levelStore = levelStore;
		
		camera.center.set(0,0, 0);
		camera.eye.set(0, 15, -30f);
	}

	@Override
	public void onModeCreate(GL10 gl, int displayWidth, int displayHeight) {
		super.onModeCreate(gl, displayWidth, displayHeight);

		final int paddingX = (int) context.getResources().getDimension(R.dimen.buttonPaddingX);
		final int paddingY = (int) context.getResources().getDimension(R.dimen.buttonPaddingY);
		
		final int nextLevelButtonWidth = 64;
		final int nextLevelButtonHeight = 64;
		final int nextLevelButtonY = displayHeight - (displayHeight - nextLevelButtonHeight)/2;
		final int nextLevelButtonX = displayWidth - nextLevelButtonWidth - paddingX;
		
		final int prevLevelButtonWidth = 64;
		final int prevLevelButtonHeight = 64;
		final int prevLevelButtonY = displayHeight - (displayHeight - prevLevelButtonHeight)/2;
		final int prevLevelButtonX = paddingX;
		
		final int levelNameWidth = displayWidth * 2/3;
		final int levelNameHeight = 64;
		final int levelNameX = (displayWidth - levelNameWidth)/2;
		final int levelNameY = displayHeight - paddingY;
		
		final int startButtonWidth = 210;
		final int startButtonHeight = 64;
		final int startButtonX = (displayWidth - startButtonWidth)/2;
		final int startButtonY = paddingY + startButtonHeight;
		
		String firstName = NO_USER_LEVELS_INSTALLED;
		if(!levelStore.isStoreEmpty()) firstName = levelStore.getCurrent().getLevel().getName();
		
		levelName = new UIElementText(gameResources, context, gl, -1, firstName, levelNameX, levelNameY, levelNameWidth, levelNameHeight, BUTTON_TEXT_SIZE);
		
		authorName = new UIElementText(gameResources, context, gl, -1, "", levelNameX, levelNameY-67, levelNameWidth, levelNameHeight, BUTTON_TEXT_SIZE);
		
		startButton = new UIElementText(gameResources, context, gl, START_BUTTON, "Select Level", startButtonX, startButtonY, startButtonWidth, startButtonHeight, BUTTON_TEXT_SIZE);
		
		updateAuthorNameUI();
		
		uiElements.add(levelName);
		uiElements.add(startButton);
		uiElements.add(authorName);
		
		nextLevelButton = new UIElementPicture(NEXT_LEVEL_BUTTON, 
				nextLevelButtonX, nextLevelButtonY, nextLevelButtonWidth, nextLevelButtonHeight, UIElementPicture.BUTTON_RIGHT);
		previousLevelButton = new UIElementPicture(PREVIOUS_LEVEL_BUTTON, 
				prevLevelButtonX, prevLevelButtonY, prevLevelButtonWidth, prevLevelButtonHeight, UIElementPicture.BUTTON_LEFT);

		buttons.add(nextLevelButton);
		buttons.add(previousLevelButton);
		
		if(levelStore.isStoreEmpty()) {
			startButton.hidden = true;
			nextLevelButton.hidden = true;
			previousLevelButton.hidden = true;
		}
	}
	
	@Override
	public void onModePop() {
		levelStore.saveLevelStates();
	}

	@Override
	public int optionsMenu() {
		return optionsMenu;
	}
	
	private void smoothCameraTransition() {
		float x = gameMode.getCamera().eye.getX();
		float y = gameMode.getCamera().eye.getY();
		float z = gameMode.getCamera().eye.getZ();
		
		gameMode.getCamera().eye.copy(camera.eye);
		
		gameMode.getCamera().eye.animateTo(x, y, z, CAMERA_TRANSITION_SPEED);
		gameMode.setAngleRotationX(rotateMap.getValue() + 45*rotationModifier);
	}
	
	@Override
	public boolean onOptionsItemSelected(final Context context, MenuItem item) {
		if(item.getItemId() == R.id.shareLevel) {
			if(!levelStore.isStoreEmpty()) {
				Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
				shareIntent.setType("application/vnd.bwr.blockcomposer.level");
				
				shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.fromFile(levelStore.getCurrentFile()));
				shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Check out my block composer level");
				shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
	
				context.startActivity(Intent.createChooser(shareIntent, "Share level with..."));
			}
			return true;
		} else if(item.getItemId() == R.id.editLevel) {
			EditMode editMode = new EditMode(modeController, gameResources);
			editMode.onStart = gameMode;
			
			if(!levelStore.isStoreEmpty()) {
				editMode.editLevel(levelStore.getCurrent().getLevel(), levelStore.getCurrentFile().getName(), levelStore);
			
				modeController.pushMode(editMode);
			}
			return true;
		}
		
		return false;
	}
	
	@Override
	protected boolean handleUIEvent(int id, int action) {
		if(action == MotionEvent.ACTION_DOWN) {
			if(id == START_BUTTON) {
				gameMode.state = levelStore.getCurrent();
				
				if(levelStore.getCurrent().entryText != -1) {
					String entryText = context.getString(levelStore.getCurrent().entryText);
					modeController.pushMode(new GameDialogMode(this, entryText, new GameDialogMode.OnResult() {
						public void onResult() {
							modeController.pushMode(gameMode);
							smoothCameraTransition();
						}
					}));
				} else {
					modeController.pushMode(gameMode);
					smoothCameraTransition();
				}

				return true;
			} else if(id == NEXT_LEVEL_BUTTON || id == PREVIOUS_LEVEL_BUTTON) {
				moveToMap(id);
				return true;
			}
		}
		return false;
	}
	
	private void moveToMap(int direction) {
		
		prevMap = levelStore.getCurrent();
		
		if(direction == NEXT_LEVEL_BUTTON) {
			levelStore.moveForward();
		} else if(direction == PREVIOUS_LEVEL_BUTTON) {
			levelStore.moveBack();
		}
		
		nextMap = levelStore.getCurrent();
		
		prevMapLocation.set(0, 0, 0);
		if(direction == NEXT_LEVEL_BUTTON) {
			prevMapLocation.animateTo(40, 0, 0, 400);
			nextMapLocation.set(-40, 0, 0);
		} else if(direction == PREVIOUS_LEVEL_BUTTON) {
			prevMapLocation.animateTo(-40, 0, 0, 400);
			nextMapLocation.set(40, 0, 0);
		}
		nextMapLocation.animateToWithCallback(0, 0, 0, 400, this);
		
		camera.eye.animateTo(0, nextMap.getLevel().getDimensions().getSizeY() + 10, -30f, 600);
		
		updateLevelNameUI();
		updateAuthorNameUI();
		
		
		displayMode = MAP_TRANSITION_ANIM;
	}
	
	private void updateLevelNameUI() {
		levelName.setText(levelStore.getCurrent().getLevel().getName());
	}
	
	private void updateAuthorNameUI() {
		String author = null;
		
		if(!levelStore.isStoreEmpty()) 
			author = levelStore.getCurrent().getLevel().getAuthor();
		
		if(author == null || author.equals("")) {
			authorName.hidden = true;
		} else {
			authorName.hidden = false;
			authorName.setText("by " + author);
		}
	}
	
	public void onAnimationFinished() {
		displayMode = MAP_STATIC_DISP;
	}

	@Override
	public void render3D(GL10 gl) {
		gl.glTranslatex(0, -5<<16, 10<<16);
		levelName.update(context, gl, 0); // HACK
		authorName.update(context, gl, 0); // HACK
    	
		if(levelStore.isStoreEmpty()) return;
		
    	if(displayMode == MAP_TRANSITION_ANIM) {        	
        	gl.glPushMatrix();
        	gl.glTranslatef(prevMapLocation.getX(), prevMapLocation.getY(),prevMapLocation.getZ());
        	gl.glRotatef(rotateMap.getValue() + 45*rotationModifier, 0, 1, 0);
        	prevMap.draw(gl, gameResources);
        	gl.glPopMatrix();
        	gl.glPushMatrix();
        	gl.glTranslatef(nextMapLocation.getX(), nextMapLocation.getY(),nextMapLocation.getZ());
        	gl.glRotatef(rotateMap.getValue() + 45*rotationModifier, 0, 1, 0);
        	nextMap.draw(gl, gameResources);
        	gl.glPopMatrix();
    	} else {
        	gl.glPushMatrix();
    		gl.glRotatef(rotateMap.getValue() + 45*rotationModifier, 0, 1, 0);
    		levelStore.getCurrent().draw(gl, gameResources);
        	gl.glPopMatrix();
    	}
    	
	}

	@Override
	public void update(long dt) {
		camera.update(dt);
		rotateMap.update(dt);
		prevMapLocation.update(dt);
		nextMapLocation.update(dt);
	}

	@Override
	public Camera getCamera() {
		return camera;
	}
}
