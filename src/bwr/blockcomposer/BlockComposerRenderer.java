package bwr.blockcomposer;

import static javax.microedition.khronos.opengles.GL10.*;

import java.util.LinkedList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import bwr.blockcomposer.modes.EditMode;
import bwr.blockcomposer.modes.GameMode;
import bwr.blockcomposer.modes.LevelSelectMode;
import bwr.blockcomposer.modes.LogoDisplayMode;
import bwr.blockcomposer.modes.Mode;
import bwr.blockcomposer.modes.ModeController;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.SystemClock;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.MotionEvent;

class BlockComposerRenderer extends GLSurfaceView implements GLSurfaceView.Renderer, ModeController {

	private static final float Z_NEAR = 10f;
	private static final float Z_FAR = 60f;
	
	// GL lighting settings
	private static final float LIGHT0_DIFFUSE[]  = { 0.9f, 0.9f, 0.9f, 1.0f };
	private static final float LIGHT0_POSITION[] = { 0.0f, 0.0f, 0.0f, 1.0f };
	private static final float LIGHT0_AMBIENT[]  = { 0.3f, 0.3f, 0.3f, 1.0f };
		
	private static final int VIBRATE_TIME = 30;
		
	private Activity parentActivity = null;

    private int width, height;
	private float aspectRatio;
    		
	private boolean waitingForRelease = false;

    private long lastStartTime = SystemClock.elapsedRealtime();
	private Vibrator vibrator;
	
	private LogoDisplayMode gameIntroMode;
	private LevelSelectMode levelSelectMode;
	private LevelSelectMode tutorialSelectMode;
	private LevelSelectMode userLevelsSelectMode;
	private LevelSelectMode contribSelectMode;
	private GameMode gameMode;
	private EditMode editMode;
	private final LinkedList<Mode> modes = new LinkedList<Mode>();
	private Mode currentMode = null;
	private Mode pushedMode = null;
	private int modePop = 0;
	
	private boolean wasPaused = false;
	
	private final GameResources gameResources;
	
	public GameResources getGameResources() {
		return gameResources;
	}

	public BlockComposerRenderer(Activity context) {
		super(context);
		setRenderer(this);
		
		this.parentActivity = context;
		
		BlockComposer application = (BlockComposer)context.getApplication();
		
		gameResources = new GameResources(application, context);
		
		vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
		
		gameIntroMode = new LogoDisplayMode(this, gameResources);
		levelSelectMode = new LevelSelectMode(this, gameResources, gameResources.getNormalLevelStore());
		tutorialSelectMode = new LevelSelectMode(this, gameResources, gameResources.getTutorialLevelStore());
		contribSelectMode = new LevelSelectMode(this, gameResources, gameResources.getContribLevelStore());

		gameMode = new GameMode(this, gameResources);
		editMode = new EditMode(this, gameResources);

		createUserLevelSelectMode(application);
		
		gameIntroMode.onStart = levelSelectMode;
		gameIntroMode.tutorialMode = tutorialSelectMode;
		gameIntroMode.editMode = editMode;
		gameIntroMode.customMode = userLevelsSelectMode;
		gameIntroMode.contribMode = contribSelectMode;
		
		levelSelectMode.gameMode = gameMode;
		editMode.onStart = gameMode;
		tutorialSelectMode.gameMode = gameMode;
		currentMode = gameIntroMode;
		contribSelectMode.gameMode = gameMode;
	}
	
	private void createUserLevelSelectMode(BlockComposer application) {
		userLevelsSelectMode = new LevelSelectMode(this, gameResources, gameResources.getUserLevelStore());
		userLevelsSelectMode.gameMode = gameMode;
		userLevelsSelectMode.optionsMenu = R.menu.userlevelsmenu;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) { 
			popMode(); 
			return true;
		}
		
		if(currentMode != null) {
			return currentMode.onKeyDown(keyCode, event);
		}

		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		wasPaused = true;
		gameResources.onPause();
		
		for(Mode m : modes) m.setInitialized(false);
		currentMode.setInitialized(false);
	}
	
	public void onStop() {
		onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

    @Override 
    public boolean onTouchEvent(MotionEvent e) {
        final int x = (int) e.getX();
        final int y = height - (int) e.getY();
        final int action = e.getAction();
        
        if(waitingForRelease && (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE)) {
        	return true;
        }
        
        waitingForRelease = false;
        
        if(currentMode != null) {
			if(currentMode.onTouchEvent(x, y, action)) {
				waitingForRelease = true;
				vibrator.vibrate(VIBRATE_TIME);
			}
        }
        
        return true;
    }
    
	public void popMode() {
		getCurrentMode().onModePop();
		modePop++;
	}

	public void pushMode(Mode mode) {
		pushedMode = mode;
	}
	
	public Mode getCurrentMode() {
		return currentMode;
	}

    private void updateModeController(GL10 gl) {
    	boolean modeChanged = false;
    	
    	while(modePop > 0) {
    		if(modes.isEmpty()) {
    			parentActivity.finish();
    		} else {
    			currentMode.onModePop();
	    		currentMode = modes.removeLast();
	    		modeChanged = true;
    		}
    		modePop--;
    	}
    	
    	if(pushedMode != null) {
    		modes.addLast(currentMode);
    		currentMode = pushedMode;
    		pushedMode = null;
    		modeChanged = true;
    	}
    	
    	if(modeChanged) {
    		if(currentMode.isInitialized()) 
    			currentMode.onModeBecomeActive(gl);
    	}
    }
    
	public void onDrawFrame(GL10 gl) {
		long dt = updateClock();

		updateModeController(gl);

		if(!currentMode.isInitialized()) {
			currentMode.onModeCreate(gl, width, height);
			currentMode.onModeBecomeActive(gl);
		}
		
		init3D(gl);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		
		if(currentMode != null) {
			currentMode.update(dt);
			currentMode.render3D(gl);
		    init2D(gl);
		    currentMode.render2D(gl);
		}
        
        gl.glFlush();
	}
	
	private long updateClock() {
		long t = SystemClock.elapsedRealtime();
		
		long dt = t - lastStartTime;
		
		lastStartTime = t;
		
		return dt;
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		this.width = width;
		this.height = height;
		this.aspectRatio = ((float) width) / height;
		
		gl.glViewport(0, 0, width, height);

		gameResources.purgeTextures(gl);
				
		init3D(gl);
	}
	
	private void init2D(GL10 gl) {
		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();

		gl.glOrthox(0, width<<16, 0, height<<16, -(1<<16), 1<<16);
		
		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity();
		
		gl.glDisable(GL_DEPTH_TEST);
		gl.glDisable(GL_LIGHTING);
		
	}
	
	private void init3D(GL10 gl) {
		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();
		
		Camera camera = currentMode.getCamera();

		GLU.gluPerspective(gl, 45.0f*camera.zoom.getValue(), aspectRatio, Z_NEAR, Z_FAR);

		gl.glMatrixMode(GL_MODELVIEW);
		gl.glLoadIdentity();
		
		camera.translateView(gl);
		
		gl.glEnable(GL_DEPTH_TEST);
		gl.glEnable(GL_LIGHTING);
	}
	
	private void setupLighting(GL10 gl) {
		gl.glEnable(GL_LIGHTING);

		gl.glLightfv(GL_LIGHT0, GL_DIFFUSE, LIGHT0_DIFFUSE, 0);
		gl.glLightfv(GL_LIGHT0, GL_POSITION, LIGHT0_POSITION, 0);
		gl.glLightfv(GL_LIGHT0, GL_AMBIENT, LIGHT0_AMBIENT, 0);
		
		gl.glEnable(GL_LIGHT0);
	}
	
	private void initGL(GL10 gl) {
		
		if(wasPaused) {
			gameResources.purgeTextures(gl);
			wasPaused = false;
		}
		
		setupLighting(gl);

		gl.glEnable(GL_DEPTH_TEST);

		gl.glShadeModel(GL_SMOOTH);
		gl.glDepthFunc(GL_LEQUAL);
		
		gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);

		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_CULL_FACE);
		gl.glCullFace(GL_BACK);

		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClearDepthf(1.0f);

		gl.glEnable(GL_COLOR_MATERIAL);
		
		gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		gl.glEnable(GL_TEXTURE_2D);
	}
	
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		initGL(gl);
	}

}
