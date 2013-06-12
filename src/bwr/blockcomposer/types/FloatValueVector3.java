package bwr.blockcomposer.types;

import bwr.blockcomposer.AnimationFinishedListener;

public class FloatValueVector3 {
	private final FloatValue x = new FloatValue();
	private final FloatValue y = new FloatValue();
	private final FloatValue z = new FloatValue();

	private AnimationFinishedListener listener;
	private long time, duration;
	private boolean animating;
	
	public float getX() {
		return x.getValue();
	}

	public void setX(float x) {
		this.x.setValue(x);
	}

	public float getY() {
		return y.getValue();
	}

	public void setY(float y) {
		this.y.setValue(y);
	}

	public float getZ() {
		return z.getValue();
	}

	public void setZ(float z) {
		this.z.setValue(z);
	}
	
	public void animateToWithCallback(float newx, float newy, float newz, long duration, AnimationFinishedListener listener) {
		this.listener = listener;
		animateTo(newx, newy, newz, duration);
	}
	
	public void animateTo(float newx, float newy, float newz, long duration) {
		this.duration = duration;
		time = 0;

		x.initLinearAnimation(newx - x.getValue(), duration);
		y.initLinearAnimation(newy - y.getValue(), duration);
		z.initLinearAnimation(newz - z.getValue(), duration);
		
		animating = true;
	}

	public void update(long dt) {
		if(!animating) return;
		
		time += dt;
		
		x.update(dt);
		y.update(dt);
		z.update(dt);
		
		if(time >= duration) {
			animating = false;
			if(listener != null) {
				AnimationFinishedListener listener = this.listener;
				this.listener = null;
				listener.onAnimationFinished();
			}
		}
	}
	
	public FloatValueVector3() {}
	
	public FloatValueVector3(float x, float y, float z) {
		set(x,y,z);
	}
	
	public void copy(FloatValueVector3 other) {
		set(other.getX(), other.getY(), other.getZ());
	}
	
	public void copy(IntVector other) {
		set(other.x, other.y, other.z);
	}
	
	public void set(float x, float y, float z) {
		this.x.setValue(x);
		this.y.setValue(y);
		this.z.setValue(z);
	}
	
	public void add(FloatValueVector3 v) {
		this.x.setValue(x.getValue() + v.getX());
		this.y.setValue(y.getValue() + v.getY());
		this.z.setValue(z.getValue() + v.getZ());
	}
	
	public void sub(FloatValueVector3 v) {
		this.x.setValue(x.getValue() - v.getX());
		this.y.setValue(y.getValue() - v.getY());
		this.z.setValue(z.getValue() - v.getZ());
	}
	
	public FloatValueVector3 duplicate() {
		return new FloatValueVector3(x.getValue(),y.getValue(),z.getValue());
	}
	
}