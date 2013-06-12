package bwr.blockcomposer.modes;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import bwr.blockcomposer.Camera;
import bwr.blockcomposer.GameResources;
import bwr.blockcomposer.R;
import bwr.blockcomposer.gamedata.GameState;
import bwr.blockcomposer.gamedata.Level;
import bwr.blockcomposer.types.RotatingFloat;
import bwr.blockcomposer.ui.UIElementText;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputFilter;
import android.view.MotionEvent;
import android.widget.EditText;

public class LevelImportMode extends Mode {

	private static final int IMPORT_BUTTON = 0;
	
	private Level level;
	private GameState state;
	
	private Camera camera = new Camera();
	private final RotatingFloat rotateMap = new RotatingFloat(360, 10000);
	
	private String levelFileName = ".bclevel";
	
	private UIElementText levelName, importButton;

	@Override
	public void onModeCreate(GL10 gl, int displayWidth, int displayHeight) {
		super.onModeCreate(gl, displayWidth, displayHeight);

		final int paddingX = (int) context.getResources().getDimension(R.dimen.buttonPaddingX);
		final int paddingY = (int) context.getResources().getDimension(R.dimen.buttonPaddingY);
		
		final int levelNameWidth = displayWidth * 2/3;
		final int levelNameHeight = 64;
		final int levelNameX = (displayWidth - levelNameWidth)/2;
		final int levelNameY = displayHeight - paddingY;
		
		final int startButtonWidth = 210;
		final int startButtonHeight = 64;
		final int startButtonX = (displayWidth - startButtonWidth - paddingX);
		final int startButtonY = paddingY + startButtonHeight;
		
		String name = "Unknown";
		if(level != null && level.getName() != null) name = level.getName();
		
		levelName = new UIElementText(gameResources, context, gl, -1, name, levelNameX, levelNameY, levelNameWidth, levelNameHeight, BUTTON_TEXT_SIZE);
		importButton = new UIElementText(gameResources, context, gl, IMPORT_BUTTON, "Import Level", startButtonX, startButtonY, startButtonWidth, startButtonHeight, BUTTON_TEXT_SIZE);
		
		uiElements.add(levelName);
		uiElements.add(importButton);
	}

	public LevelImportMode(ModeController modeController, GameResources gameResources, Intent intent) throws IOException {
		super(modeController, gameResources);
		
		camera.center.set(0, 0, 0);
		camera.eye.set(0, 15, -15f);
		
		if ("content".equals(intent.getScheme())) {
			InputStream attachment = context.getContentResolver().openInputStream(intent.getData());
			level = Level.loadFromFile(attachment, gameResources);
			attachment.close();
		} else {
			File importFile = new File(intent.getDataString());
			levelFileName = importFile.getName();
			if(!levelFileName.endsWith(".bclevel")) levelFileName = ".bclevel";

			InputStream attachment = context.getContentResolver().openInputStream(intent.getData());
			level = Level.loadFromFile(attachment, gameResources);
			attachment.close();	
		}
		
		if(level != null) {
			state = new GameState(level);
		}
	}
	
	private void importLevel() {
		if(application.isExternalStorageAvailable()) {
			
			AlertDialog.Builder alert = new AlertDialog.Builder(context);

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
						
						File file = new File(application.levelStorageDirectory, levelFileName);
						FileOutputStream fos = new FileOutputStream(file);
						DataOutputStream out = new DataOutputStream(new BufferedOutputStream(fos));
						level.writeToFile(out);
						out.close();
						modeController.popMode();
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
			
			alert.show();

		} else {
			AlertDialog.Builder alert = new AlertDialog.Builder(context);
			
			alert.setTitle("External storage not available");
			alert.setMessage(R.string.ext_storage_not_available);
			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					// Nothing
				}
			});
			
			alert.show();
		}
	}

	@Override
	protected boolean handleUIEvent(int id, int action) {
		if(action == MotionEvent.ACTION_DOWN) {
			if(id == IMPORT_BUTTON) {
				importLevel();
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void update(long dt) {
		rotateMap.update(dt);
	}

	@Override
	public void render3D(GL10 gl) {
    	gl.glPushMatrix();
		gl.glRotatef(rotateMap.getValue() + 45, 0, 1, 0);
		state.draw(gl, gameResources);
    	gl.glPopMatrix();
	}

	@Override
	public Camera getCamera() {
		return camera;
	}

}
