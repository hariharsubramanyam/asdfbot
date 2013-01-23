package trevIncorporatePlayer;

import battlecode.common.GameActionException;

public class SNukeTransition extends Transition {
	
	public SNukeTransition(StateMachine rootSM){
		this.rootSM = rootSM;
		this.sourceStates = new int[] {SMConstants.SWAITSTATE, SMConstants.SBUILDSTATE};
		this.targetState = SMConstants.SRALLYSTATE;
	}

	@Override
	public int[] getSourceStateID() {
		// TODO Auto-generated method stub
		return sourceStates;
	}

	@Override
	public State getTargetState() {
		return SMConstants.getState(rootSM, targetState);
	}

	@Override
	public boolean isTriggered() {
		try {
			return rootSM.rc.senseEnemyNukeHalfDone();
		} catch (GameActionException e) {return false;}
	}

}
