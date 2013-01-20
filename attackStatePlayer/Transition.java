package attackStatePlayer;

public abstract class Transition {
	
	StateMachine rootSM;
	int targetState;
	int sourceState;
	public int getSourceStateID(){
		return sourceState;
	}
	public State getTargetState(){
		return SMConstants.getState(rootSM, targetState);
	}
	public abstract boolean isTriggered();

}
