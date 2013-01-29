package jyoPlayer2;

import battlecode.common.GameActionException;

public class SNukeTransition extends Transition {
	
	public SNukeTransition(StateMachine rootSM){
		this.rootSM = rootSM;
		this.sourceStates = new int[] {SMConstants.SWAITSTATE, SMConstants.SBUILDSTATE/*, SMConstants.SRALLYSTATE*/};
		this.targetState = SMConstants.SATTACKSTATE;
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
			if (rootSM.rc.readBroadcast(39842) == 186254){
				PlayerConstants.NUM_ROBOTS_IN_ATTACK_GROUP = 10;
				return true;
			}
			return false;
		} catch (GameActionException e) {return false;}
	}

}
