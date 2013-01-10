/**
 * This is an abstract class, so don't instantiate it
 * State in the state machine
 */
package hariharPlayer;

import battlecode.common.*;


public abstract class State {
	
	int stateID;
	StateMachine rootSM;
	RobotController rc;
	
	public abstract void doEntryAct();
	public abstract void doExitAct();
	public abstract void doAction();

}
