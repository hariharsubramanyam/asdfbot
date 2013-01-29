package smartNuke;

import battlecode.common.Clock;

public class SRound200Transition extends Transition {

	public SRound200Transition(StateMachine rootSM){
		this.rootSM = rootSM;
		this.sourceStates = new int[] {SMConstants.SWAITSTATE};
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
		return (Clock.getRoundNum()%500 == 0);
	}

}