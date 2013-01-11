/**
 * This is an abstract class, so don't instantiate it.
 * State machine. HQSM, SoldierSM, and ArtillerySM should derive from this
 */
package SMPlayer;
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
