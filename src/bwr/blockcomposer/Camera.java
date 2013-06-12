package bwr.blockcomposer;

import javax.microedition.khronos.opengles.GL10;

import bwr.blockcomposer.types.FloatValue;
import bwr.blockcomposer.types.FloatValueVector3;

import android.opengl.GLU;

public class Camera {
	public final FloatValueVector3 eye = new FloatValueVector3(0f, 0f, 0f);
	public final FloatValueVector3 center = new FloatValueVector3(0f, 0f, 0f);
	public final FloatValueVector3 up = new FloatValueVector3(0f, 1f, 0f);
	
	public final FloatValue zoom = new FloatValue(1.0f);
	
	public void update(long dt) {
		eye.update(dt);
		center.update(dt);
		up.update(dt);
		zoom.update(dt);
	}
	
	public void translateView(GL10 gl) {
		GLU.gluLookAt(gl, eye.getX(), eye.getY(), eye.getZ(), center.getX(), center.getY(), center.getZ(), up.getX(), up.getY(), up.getZ());
	}
	
	public void zoom(GL10 gl, float zoom) {
		
	}
}
