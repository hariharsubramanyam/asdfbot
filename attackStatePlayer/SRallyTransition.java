package attackStatePlayer;

import battlecode.common.Clock;

public class SRallyTransition extends Transition {

	public SRallyTransition(StateMachine rootSM){
		this.rootSM = rootSM;
		this.sourceState = SMConstants.SBUILDSTATE;
		this.targetState = SMConstants.SWAITSTATE;
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
		return ((Clock.getRoundNum()+50)%300 == 0);
	}

}