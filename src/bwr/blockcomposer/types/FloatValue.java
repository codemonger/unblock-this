package bwr.blockcomposer.types;

import bwr.blockcomposer.AnimationFinishedListener;

public class FloatValue {

	public final static byte ANIMATION_NONE = 0;
	public final static byte ANIMATION_LINEAR = 1;

	// Cached value
	private float value;
	
	// Linear interpolation variables
	private float initial;
	private float rate = 0;
	
	// Animation variables
	private long time = 0;
	private long duration = 0;
	private byte animationType = ANIMATION_NONE;

	AnimationFinishedListener listener = null;
	
	public FloatValue(float initial) {
		this.value = initial;
	}
	
	public FloatValue() {
		this.value = 0f;
	}

	public void initLinearAnimation(float change, long duration, AnimationFinishedListener listener) {
		this.listener = listener;
		initLinearAnimation(change, duration);
	}
	
	public void initLinearAnimation(float change, long duration) {
		animationType = ANIMATION_LINEAR;
		initial = getValue(); // get result of last interpolation
		time = 0;
		this.duration = duration;
		rate = change/duration;
	}
	
	public void setValueOverTime(float value, long duration) {
		
		
		initLinearAnimation(value - this.value, duration);
	}
	
	public void update(long dt) {
		if(animationType == ANIMATION_NONE) return;
		
		time = Math.min(time + dt, duration);
		
		if(animationType == ANIMATION_LINEAR) {
			value = initial + time * rate;
		}
		
		if(time == duration) {
			animationType = ANIMATION_NONE;
			
			if(listener != null) {
				listener.onAnimationFinished();
				listener = null;
			}
		}
	}
	
	public float getValue() {
		return value;
	}

	public void setValue(float v) {
		this.value = v;
		animationType = ANIMATION_NONE;
	}
	
}
