package bwr.blockcomposer.modes;

import javax.microedition.khronos.opengles.GL10;

import bwr.blockcomposer.Camera;

public class OverlayMode extends Mode {

	private Mode mode;
	
	public OverlayMode(Mode mode) {
		super(mode.modeController, mode.gameResources);
		this.mode = mode;
	}

	@Override
	public Camera getCamera() {
		return mode.getCamera();
	}

	@Override
	public void render3D(GL10 gl) {
		mode.render3D(gl);
	}

	@Override
	public void update(long dt) {
		mode.update(dt);
	}

	@Override
	public void onModeCreate(GL10 gl, int displayWidth, int displayHeight) {
		super.onModeCreate(gl, displayWidth, displayHeight);
	}
}
