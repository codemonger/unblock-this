package bwr.blockcomposer.types;

public class RotatingFloat {
	private final float duration, rate;
	private float value = 0;
	private long time = 0;
	
	public RotatingFloat(float change, long duration) {
		this.duration = duration;
		this.rate = change/duration;
	}
	
	public void update(long dt) {
		time += dt;
		while(time > duration) {
			time -= duration;
		}
		value = time * rate;
	}
	
	public float getValue() {
		return value;
	}
}
