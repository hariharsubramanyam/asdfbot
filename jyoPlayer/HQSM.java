package jyoPlayer;

import battlecode.common.RobotController;

public class HQSM extends StateMachine {
	
	// Construct and set initial state
	public HQSM(RobotController rc){
		this.rc = rc;
		// SWAITSTATE = hang around near our HQ and group together, SATTACKSTATE = attack nearest enemy or enemy HQ
		this.stateIDs = new int[]{SMConstants.HDEFAULTSTATE};
		this.goToState(SMConstants.HDEFAULTSTATE);	// start in build state
	}

}
