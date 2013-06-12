package bwr.blockcomposer.ui;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;

public interface UIElement {
	void update(Context context, GL10 gl, long dt);
	int getId();
	boolean isHit(int x, int y);
}
