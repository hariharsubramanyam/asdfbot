package heuristicMovement;

import battlecode.common.GameActionException;

public class SNukeTransition extends Transition {
	
	public SNukeTransition(StateMachine rootSM){
		this.rootSM = rootSM;
		this.sourceState = SMConstants.SWAITSTATE;
		this.targetState = SMConstants.SATTACKSTATE;
	}
	
	public boolean nukeHalfDone;
	
	public void NukeHalfDone() throws GameActionException{
		nukeHalfDone = rootSM.rc.senseEnemyNukeHalfDone();
	}
	

	@Override
	public int getSourceStateID() {
		// TODO Auto-generated method stub
		return sourceState;
	}

	@Override
	public State getTargetState() {
		return SMConstants.getState(rootSM, targetState);
	}

	@Override
	public boolean isTriggered() {
		return nukeHalfDone;
	}

}
