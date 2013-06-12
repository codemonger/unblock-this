package bwr.blockcomposer;

import java.io.IOException;

import bwr.blockcomposer.modes.LevelImportMode;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;

public class BlockComposerActivity extends Activity {
	private final static int DIALOG_HELP = 4;
	private final static int DIALOG_IMPORT = 5;

	private BlockComposerRenderer renderer;
	private int currentOptionsMenu = -1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		renderer = new BlockComposerRenderer(this);
		
		setContentView(renderer);

		Intent i = getIntent();
		if(i != null && Intent.ACTION_VIEW.equals(i.getAction())) {
			try {
				renderer.pushMode(new LevelImportMode(renderer, renderer.getGameResources(), i));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    
	    if(renderer == null || renderer.getCurrentMode() == null) return false;
	    
		inflater.inflate(renderer.getCurrentMode().optionsMenu(), menu);
		currentOptionsMenu = renderer.getCurrentMode().optionsMenu();
	    
	    return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(currentOptionsMenu != renderer.getCurrentMode().optionsMenu()) {
		    menu.clear();
		    MenuInflater inflater = getMenuInflater();
		    inflater.inflate(renderer.getCurrentMode().optionsMenu(), menu);
		    currentOptionsMenu = renderer.getCurrentMode().optionsMenu();
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.help) {
			showDialog(DIALOG_HELP); 
			return true;
		} else if(item.getItemId() == R.id.importLevel) {
			showDialog(DIALOG_IMPORT); 
			return true;
		}
		if(renderer.getCurrentMode() != null) {
			return renderer.getCurrentMode().onOptionsItemSelected(this, item);
		}
	    return false;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case DIALOG_HELP:
			return createDialogFromResources("Help", R.layout.helpdialog);
		case DIALOG_IMPORT:
			return createDialogFromResources("How to import...", R.layout.importdialog);
		}
		
		return super.onCreateDialog(id);
	}
	
	private Dialog createDialogFromResources(String title, int layout) {
		Dialog dialog = new Dialog(this);

		dialog.setContentView(layout);
		dialog.setTitle(title);

		return dialog;	
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return renderer.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		return renderer.onTrackballEvent(event);
	}

	@Override
	protected void onPause() {
		super.onPause();
		renderer.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		renderer.onStop();
	}

	@Override
	protected void onResume() {
		super.onResume();
		renderer.onResume();
	}
}
