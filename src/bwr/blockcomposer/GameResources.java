package bwr.blockcomposer;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import bwr.blockcomposer.gamedata.LevelStore;
import bwr.blockcomposer.gamedata.StaticLevelStore;
import bwr.blockcomposer.gamedata.UserLevelStore;
import bwr.blockcomposer.misc.GLUtility;
import bwr.blockcomposer.models.Model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.SparseArray;

public class GameResources {
	
	private final SparseArray<Model> models = new SparseArray<Model>();
	private final SparseArray<int[]> textures = new SparseArray<int[]>();

	private final BlockComposer blockComposer;
	private final Context context;
	
	private final StaticLevelStore normalLevelStore;
	private final StaticLevelStore tutorialLevelStore;
	private final StaticLevelStore contribLevelStore;
	private final UserLevelStore userLevelStore;
	
	public Context getContext() {
		return context;
	}
	
	public BlockComposer getBlockComposer() {
		return blockComposer;
	}

	public GameResources(BlockComposer blockComposer, Context context) {
		this.context = context;
		this.blockComposer = blockComposer;
		
		normalLevelStore = new StaticLevelStore(this, BlockComposer.levels);
		tutorialLevelStore = new StaticLevelStore(this, BlockComposer.tutorialLevels);
		contribLevelStore = new StaticLevelStore(this, BlockComposer.contribLevels);

		userLevelStore = new UserLevelStore(getContext(), blockComposer.levelStorageDirectory, this);
	}
	
	public Model loadModel(int resourceId) {
		Model m = models.get(resourceId);
		
		if(m != null) {
			return m;
		} else {
			try {
				Model model = Model.loadFromResource(context, resourceId);
				models.put(resourceId, model);
				return model;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public void bindTexture(GL10 gl, int resourceId) {
		int[] texture = textures.get(resourceId);
		
		if(texture == null) {
			loadTexture(gl, resourceId);
		}
		
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures.get(resourceId)[0]);
	}
	
	public void purgeTextures(GL10 gl) {
		int size = textures.size();
		int[] texture;
		for(int i = 0; i < size; i++) {
			texture = textures.valueAt(i);
			if(texture != null) {
				gl.glDeleteTextures(1, texture, 0);
			}
		}
		textures.clear();
	}
	
	private void loadTexture(GL10 gl, int resourceId) {
		if(textures.get(resourceId) != null) return;
		
		//Get the texture from the Android resource directory
		InputStream is = context.getResources().openRawResource(resourceId);
		Bitmap bitmap = null;
		try {
			//BitmapFactory is an Android graphics utility for images
			bitmap = BitmapFactory.decodeStream(is);

		} finally {
			//Always clear and close
			try {
				is.close();
				is = null;
			} catch (IOException e) {}
		}

		textures.put(resourceId, new int[1]);
		
		//Generate one texture pointer...
		gl.glGenTextures(1, textures.get(resourceId), 0);
		
		GLUtility.loadBitmapIntoTexture(gl, textures.get(resourceId)[0], bitmap);
		
		//Clean up
		bitmap.recycle();
	}

	public StaticLevelStore getNormalLevelStore() {
		return normalLevelStore;
	}

	public StaticLevelStore getTutorialLevelStore() {
		return tutorialLevelStore;
	}

	public UserLevelStore getUserLevelStore() {
		return userLevelStore;
	}
	
	public LevelStore getContribLevelStore() {
		return contribLevelStore;
	}

	public void onPause() {
		saveMapState();
	}

	public void saveMapState() {
		tutorialLevelStore.saveLevelStates();
		normalLevelStore.saveLevelStates();
		userLevelStore.saveLevelStates();
	}


}
