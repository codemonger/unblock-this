package bwr.blockcomposer.misc;

import bwr.blockcomposer.AnimationFinishedListener;

public class Timer {
	private final long duration;
	private long time = 0;
	private AnimationFinishedListener listener;
	
	public Timer(long duration, AnimationFinishedListener listener) {
		this.duration = duration;
		this.listener = listener;
	}
	
	public void update(long dt) {
		time += dt;
		
		if(time >= duration && listener != null) {
			AnimationFinishedListener tmp = listener;
			listener = null;
			tmp.onAnimationFinished();
		}
	}
}
