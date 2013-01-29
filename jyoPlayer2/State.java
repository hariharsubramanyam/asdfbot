package jyoPlayer2;

import battlecode.common.*;


public abstract class State {
	
	int stateID;
	StateMachine rootSM;
	RobotController rc;
	
	public abstract void doEntryAct();
	public abstract void doExitAct();
	public abstract void doAction();

}
