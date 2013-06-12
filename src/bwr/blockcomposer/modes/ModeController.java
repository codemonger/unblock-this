package bwr.blockcomposer.modes;


public interface ModeController {
	void pushMode(Mode mode);
	void popMode();
	Mode getCurrentMode();
}
