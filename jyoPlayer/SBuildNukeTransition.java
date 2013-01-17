package jyoPlayer;

import battlecode.common.Clock;
import battlecode.common.GameActionException;

public class SBuildNukeTransition extends Transition {
	
	public SBuildNukeTransition(StateMachine rootSM){
		this.rootSM = rootSM;
		this.sourceState = SMConstants.SBUILDSTATE;
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
