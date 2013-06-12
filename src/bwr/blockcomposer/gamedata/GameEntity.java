package bwr.blockcomposer.gamedata;

import bwr.blockcomposer.models.Model;

public abstract class GameEntity {
	protected Model model = null;

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public GameEntity(Model model) {
		this.model = model;
	}
	
	public GameEntity() {}
	
	public abstract void update(long dt);
}
