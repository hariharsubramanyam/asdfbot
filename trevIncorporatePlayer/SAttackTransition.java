package trevIncorporatePlayer;

import battlecode.common.*;

public class SAttackTransition extends Transition {
	
	public SAttackTransition(StateMachine rootSM){
		this.rootSM = rootSM;
		this.sourceStates = new int[] {SMConstants.SWAITSTATE, SMConstants.SBUILDSTATE};
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
		return (Clock.getRoundNum() > 200);
	}

}
