package bwr.blockcomposer.types;

public class FlipRotateFloat {
	private final float rate;
	private float value = 0;
	private long time = 0;
	private long duration;
	boolean flipped = false;
	
	public FlipRotateFloat(float change, long duration) {
		this.duration = duration;
		this.rate = change/duration;
	}
	
	public void update(long dt) {
		if(flipped) {
			time -= dt;
		} else {
			time += dt;
			
		}

		if(time < 0) {
			time = 0;
			flipped = !flipped;
		} else if(time > duration) {
			time = duration;
			flipped = !flipped;
		}

		value = time * rate;
	}
	
	public float getValue() {
		return value;
	}
}
