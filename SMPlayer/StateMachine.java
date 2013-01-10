package SMPlayer;
import battlecode.common.*;

public abstract class StateMachine {

	RobotController rc;
	int[] stateIDs;
	State currentState;
	Transition[] currentTransitions;
	
	public abstract void step();	
	public abstract RobotController getRC();
	public abstract Transition[] goToState(int stateID);
	
}
