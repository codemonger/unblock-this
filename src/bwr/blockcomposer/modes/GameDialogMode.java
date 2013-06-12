package bwr.blockcomposer.modes;

import javax.microedition.khronos.opengles.GL10;

import bwr.blockcomposer.AnimationFinishedListener;
import bwr.blockcomposer.R;
import bwr.blockcomposer.types.FloatValue;
import bwr.blockcomposer.ui.UIElementText;

import android.view.MotionEvent;

public class GameDialogMode extends OverlayMode {

	public static interface OnResult {
		public void onResult();
	}
	
	private static final int ANIMATION_TIME = 200;
	private static final int OKAY_BUTTON = 1;
	
	private UIElementText okayButton;
	private UIElementText message;
	
	private FloatValue gameDialogY = new FloatValue();
	private FloatValue okayButtonAlpha = new FloatValue();
	
	private boolean finishedInitialAnimation = false;
	
	private final String text;
	private final OnResult resultHandler;
	
	public GameDialogMode(Mode mode, String text, OnResult resultHandler) {
		super(mode);
		this.text = text;
		this.resultHandler = resultHandler;
	}

	@Override
	public void onModeCreate(GL10 gl, int displayWidth, int displayHeight) {
		super.onModeCreate(gl, displayWidth, displayHeight);

		final int paddingX = (int) context.getResources().getDimension(R.dimen.buttonPaddingX);
		final int paddingY = (int) context.getResources().getDimension(R.dimen.buttonPaddingY);
		
		final int okayButtonWidth = 100;
		final int okayButtonHeight = 64;
		final int okayButtonX = displayWidth - okayButtonWidth - paddingX;
		final int okayButtonY = okayButtonHeight + paddingY;
				
		okayButton = new UIElementText(gameResources, context, gl, OKAY_BUTTON, "Okay", okayButtonX, okayButtonY, okayButtonWidth, okayButtonHeight, BUTTON_TEXT_SIZE);
		uiElements.add(okayButton);
		
		final int messageWidth = displayWidth * 2/3;
		final int messageRequiredHeight = UIElementText.calculateRequiredHeight(text, messageWidth, DIALOG_TEXT_SIZE);
		final int messageHeight = Math.max(messageRequiredHeight, displayHeight/3);
		final int messageX = (displayWidth - messageWidth)/2;
		final int messageY = displayHeight - paddingY;
		
		message = new UIElementText(gameResources, context, gl, 0, text, messageX, messageY, messageWidth, messageHeight, DIALOG_TEXT_SIZE);
	
		gameDialogY.setValue(messageHeight);
		gameDialogY.initLinearAnimation(-messageHeight, ANIMATION_TIME);
		
		okayButtonAlpha.setValue(0.0f);
		okayButtonAlpha.initLinearAnimation(1, ANIMATION_TIME, new AnimationFinishedListener() {
			public void onAnimationFinished() {
				finishedInitialAnimation = true;
			}
		});
	}

	@Override
	public void update(long dt) {
		super.update(dt);
		gameDialogY.update(dt);
		okayButtonAlpha.update(dt);
	}

	@Override
	public void render2D(GL10 gl) {
        gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		
        gl.glColor4f(1f, 1f, 1f, okayButtonAlpha.getValue());
        okayButton.render(gl);

        gl.glColor4f(1f, 1f, 1f, 1f);
        
		gl.glPushMatrix();
		gl.glTranslatef(0, gameDialogY.getValue(), 0);
		message.render(gl);
		gl.glPopMatrix();
		
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

	}

	@Override
	protected boolean handleUIEvent(int id, int action) {
		if(action == MotionEvent.ACTION_DOWN && id == OKAY_BUTTON && finishedInitialAnimation) {
			gameDialogY.initLinearAnimation(150, ANIMATION_TIME);
			
			okayButtonAlpha.initLinearAnimation(-1f, ANIMATION_TIME, new AnimationFinishedListener() {
				public void onAnimationFinished() {
					finishedInitialAnimation = false;
					modeController.popMode();
				}
			});
			return true;
		}
		return false;
	}

	@Override
	public void onModePop() {
		if(resultHandler != null) {
			resultHandler.onResult();
		}
	}
	
}
