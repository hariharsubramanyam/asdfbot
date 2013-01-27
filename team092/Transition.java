package team092;

public abstract class Transition {
	
	StateMachine rootSM;
	int targetState;
	int[] sourceStates;
	public abstract int[] getSourceStateID();
	public abstract State getTargetState();
	public abstract boolean isTriggered();

}
