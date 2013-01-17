/**
 * This is an abstract class, so don't instantiate it.
 * State machine. HQSM, SoldierSM, and ArtillerySM should derive from this
 */
package hariharPlayer;
import battlecode.common.*;

public abstract class StateMachine {
	
	RobotController rc;
	int[] stateIDs;
	State currentState;
	DataManager dataManager;
	
	public void step(){
		dataManager.update(false,false,false);
		currentState = currentState.checkTransitions(); // perform any necessary transitions and update the state
		currentState.doAction();			// perform the behavior described by this state
	}
	
	public abstract void enterInitialState();

	
}
