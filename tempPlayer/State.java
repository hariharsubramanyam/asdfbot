/**
 * This is an abstract class, so don't instantiate it
 * State in the state machine
 */
package tempPlayer;

import battlecode.common.*;


public abstract class State {
	
	int stateID;
	StateMachine rootSM;
	RobotController rc;
	
	public abstract void doEntryAct();
	public abstract void doExitAct();
	public abstract void doAction();
	public abstract State checkTransitions();
	public State changeState(int newStateID){
		this.doExitAct();
		State newState = SMConstants.getState(rootSM, newStateID);
		newState.doEntryAct();
		return newState;
	}

}
