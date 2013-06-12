package bwr.blockcomposer.modes;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import bwr.blockcomposer.Camera;
import bwr.blockcomposer.GameResources;
import bwr.blockcomposer.R;
import bwr.blockcomposer.gamedata.GameState;
import bwr.blockcomposer.gamedata.Level;
import bwr.blockcomposer.types.FlipRotateFloat;
import bwr.blockcomposer.ui.UIElementText;

import android.view.MotionEvent;

public class LogoDisplayMode extends Mode {

	private static final int START_BUTTON = 0;
	private static final int TUTORIAL_BUTTON = 1;
	private static final int LEVEL_EDIT_BUTTON = 2;
	private static final int USER_LEVELS_BUTTON = 3;
	private static final int CONTRIB_LEVELS_BUTTON = 4;
	
	private FlipRotateFloat xAngle = new FlipRotateFloat(20, 1600);
	private FlipRotateFloat yAngle = new FlipRotateFloat(20, 1800);

	private final Camera camera = new Camera();
	
	private Level logoLevel;
	private GameState logo;

	private UIElementText tutorialsButton, startButton;
	private UIElementText levelEditButton;
	private UIElementText userLevelsButton;
	
	public Mode onStart;
	public LevelSelectMode tutorialMode;
	public Mode customMode;
	public EditMode editMode;
	public Mode contribMode;
	private UIElementText contributedLevelsButton;

	public LogoDisplayMode(ModeController modeController, GameResources gameResources) {
		super(modeController, gameResources);
		
		camera.center.set(0, 0, 0);
		camera.eye.set(0, 0, -29f);
	}

	@Override
	public void buildUserInterface(GL10 gl) {
		super.buildUserInterface(gl);
		
		int paddingX = (int) context.getResources().getDimension(R.dimen.buttonPaddingX);
		int paddingY = (int) context.getResources().getDimension(R.dimen.buttonPaddingY);
		
		int startButtonWidth = 100;
		int startButtonHeight = 64;
		
		int tutorialButtonWidth = 150;
		int tutorialButtonHeight = 64;
		
		int customButtonWidth = 190;
		int customButtonHeight = 64;
		
		int mapEditButtonWidth = 190;
		int mapEditButtonHeight = 64;
		
		int contributedLevelsButtonWidth = 204;
		int contributedLevelsButtonHeight = 64;

				
		startButton = new UIElementText(gameResources, context, gl, START_BUTTON, "Start", 
				displayWidth - startButtonWidth - paddingX, startButtonHeight + paddingY, startButtonWidth, startButtonHeight, BUTTON_TEXT_SIZE);
		
		tutorialsButton = new UIElementText(gameResources, context, gl, TUTORIAL_BUTTON, "Tutorial", 
				paddingX, startButtonHeight + paddingY, tutorialButtonWidth, tutorialButtonHeight, BUTTON_TEXT_SIZE);
		
		userLevelsButton = new UIElementText(gameResources, context, gl, USER_LEVELS_BUTTON, "User Levels", 
				(displayWidth - customButtonWidth)/2,
				customButtonHeight + paddingY, customButtonWidth, customButtonHeight, BUTTON_TEXT_SIZE);
		
		levelEditButton = new UIElementText(gameResources, context, gl, LEVEL_EDIT_BUTTON, "Level Editor", 
				displayWidth - mapEditButtonWidth - paddingX, displayHeight - paddingY, mapEditButtonWidth, mapEditButtonHeight, BUTTON_TEXT_SIZE);
		
		contributedLevelsButton = new UIElementText(gameResources, context, gl, CONTRIB_LEVELS_BUTTON, "Contributed", 
				paddingX, displayHeight - paddingY, 
				contributedLevelsButtonWidth, contributedLevelsButtonHeight, BUTTON_TEXT_SIZE);
		
		uiElements.add(startButton);
		uiElements.add(tutorialsButton);
		
		uiElements.add(levelEditButton);
		uiElements.add(userLevelsButton);
		
		uiElements.add(contributedLevelsButton);
	}

	@Override
	public void onModeCreate(GL10 gl, int displayWidth, int displayHeight) {
		super.onModeCreate(gl, displayWidth, displayHeight);
		buildUserInterface(gl);
		logoLevel = loadLevel(R.raw.logo);
		logo = new GameState(logoLevel);
	}
	
	private Level loadLevel(int levelId) {
		InputStream mapInputStream = context.getResources().openRawResource(levelId);
		try {
			return Level.loadFromFile(mapInputStream, gameResources);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void update(long dt) {
    	logo.update(dt);
    	xAngle.update(dt);
    	yAngle.update(dt);
	}
	
	@Override
	protected boolean handleUIEvent(int id, int action) {
		if(action == MotionEvent.ACTION_DOWN) {
			if(id == START_BUTTON) {
				modeController.pushMode(onStart);
				return true;
			} else if(id == TUTORIAL_BUTTON) {
				modeController.pushMode(tutorialMode);
				return true;
			} else if(id == LEVEL_EDIT_BUTTON) {
				editMode.newLevel();
				modeController.pushMode(editMode);
				return true;
			} else if(id == USER_LEVELS_BUTTON) {
				modeController.pushMode(customMode);
				return true;
			} else if(id == CONTRIB_LEVELS_BUTTON) {
				modeController.pushMode(contribMode);
				return true;
			}
		}
		return false;
	}

	@Override
	public void render3D(GL10 gl) {
		gl.glRotatef(xAngle.getValue()-10, 0, 1, 0);
		gl.glRotatef(yAngle.getValue()-10, 1, 0, 0);
    	logo.draw(gl, gameResources);
	}

	@Override
	public Camera getCamera() {
		return camera;
	}
	
}
