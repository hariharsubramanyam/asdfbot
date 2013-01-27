/**
 * This is an abstract class, so don't instantiate it
 * A transition takes us from the source state to the target state if it is triggered
 */
package trevPlayer;

public abstract class Transition {
	
	StateMachine rootSM;
	int targetState;
	int sourceState;
	public abstract int getSourceStateID();
	public abstract State getTargetState();
	public abstract boolean isTriggered();

}
