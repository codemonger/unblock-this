package bwr.blockcomposer.modes;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.microedition.khronos.opengles.GL10;

import bwr.blockcomposer.BlockComposer;
import bwr.blockcomposer.Camera;
import bwr.blockcomposer.GameResources;
import bwr.blockcomposer.OnUpdateAction;
import bwr.blockcomposer.R;
import bwr.blockcomposer.ui.UIElementPicture;
import bwr.blockcomposer.ui.UIElementText;

import android.content.Context;
import android.view.KeyEvent;
import android.view.MenuItem;

public abstract class Mode {
	protected final LinkedList<UIElementText> uiElements = new LinkedList<UIElementText>();
	protected final LinkedList<UIElementPicture> buttons = new LinkedList<UIElementPicture>();

	protected final ConcurrentLinkedQueue<OnUpdateAction> updateActions = new ConcurrentLinkedQueue<OnUpdateAction>();
	
	protected final Context context;
	protected final ModeController modeController;
	protected final GameResources gameResources;
	
	private final int id;
	private boolean initialized = false;

	protected final static int BUTTON_TEXT_SIZE = 32;
	protected final static int DIALOG_TEXT_SIZE = 26;
	
	protected int displayWidth, displayHeight;
	protected BlockComposer application;
	
	private static int nextId = 0;
	
	public Mode(ModeController modeController, GameResources gameResources) {
		this.context = gameResources.getContext();
		this.application = gameResources.getBlockComposer();
		this.modeController = modeController;
		this.gameResources = gameResources;
		this.id = nextId++;
	}
	
	public int getId() {
		return id;
	}
	
	public boolean isInitialized() {
		return initialized;
	}
	
	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}
	
	public abstract void update(long dt);
	
	public void onModeCreate(GL10 gl, int displayWidth, int displayHeight) {
		initialized = true;
		this.displayHeight = displayHeight;
		this.displayWidth = displayWidth;
	}
	
	public void buildUserInterface(GL10 gl) {
		
	}
	
	public abstract void render3D(GL10 gl);
	
	public abstract Camera getCamera();
	
	public void render2D(GL10 gl) {
        gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glColor4f(1f, 1f, 1f, 1f);
		for(UIElementText e : uiElements) {
			if(!e.hidden) e.render(gl);
		}
		for(UIElementPicture b : buttons) {
			if(!b.hidden) b.draw(gl, gameResources);
		}
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	}
	
	protected boolean handleUIEvent(int id, int action) {
		return false;
	}
	
	public boolean onTouchEvent(int x, int y, int action) {
		for(UIElementText e : uiElements) {
			if(!e.hidden && e.isHit(x, y)) if(handleUIEvent(e.getId(), action)) return true;
		}
		
		for(UIElementPicture b : buttons) {
			if(!b.hidden && b.isHit(x, y)) if(handleUIEvent(b.id, action)) return true;
		}
		
		return false;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
	}

	public void onModePop() {
		
	}
	
	public int optionsMenu() {
		return R.menu.optionsmenu;
	}
	
	public boolean onOptionsItemSelected(Context context, MenuItem item) {
	    return false;
	}

	public void onModeBecomeActive(GL10 gl) {
		if(!uiElements.isEmpty()) {
			for(UIElementText uie : uiElements) {
				uie.refresh(context, gl);
			}
		}
	}
	
}
