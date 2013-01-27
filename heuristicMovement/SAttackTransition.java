package heuristicMovement;

import battlecode.common.*;

public class SAttackTransition extends Transition {
	
	public SAttackTransition(StateMachine rootSM){
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
		return (Clock.getRoundNum() > 200);
	}

}
