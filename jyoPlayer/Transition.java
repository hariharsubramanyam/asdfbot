package jyoPlayer;

public abstract class Transition {
	
	StateMachine rootSM;
	int targetState;
	int sourceState;
	public abstract int getSourceStateID();
	public abstract State getTargetState();
	public abstract boolean isTriggered();

}
