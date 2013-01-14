package hariharPlayer;

import battlecode.common.RobotController;

public class HQSM extends StateMachine{
	
	public HQSM(RobotController rc){
		this.rc = rc;
		this.enterInitialState();
	}

	@Override
	public void enterInitialState() {
		this.currentState = SMConstants.getState(this, SMConstants.H_STATE_MAIN);
		this.currentState.doEntryAct();
	}

}
