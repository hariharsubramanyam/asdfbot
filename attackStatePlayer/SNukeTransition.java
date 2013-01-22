package attackStatePlayer;

import battlecode.common.GameActionException;

public class SNukeTransition extends Transition {
	
	public SNukeTransition(StateMachine rootSM){
		this.rootSM = rootSM;
		this.sourceState = SMConstants.SWAITSTATE;
		this.targetState = SMConstants.SATTACKSTATE;
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
		try {
			return rootSM.rc.senseEnemyNukeHalfDone();
		} catch (GameActionException e) {return false;}
	}

}
