package jyoPlayer;
import battlecode.common.*;

public abstract class StateMachine {

	RobotController rc;
	int[] stateIDs;
	State currentState;
	Transition[] currentTransitions;
	
	public abstract void step();	
	public abstract RobotController getRC();
	public abstract void goToState(int stateID);
	
}
