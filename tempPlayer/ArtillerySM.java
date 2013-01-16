package tempPlayer;

import battlecode.common.RobotController;

public class ArtillerySM extends StateMachine {

	public ArtillerySM(RobotController rc){
		this.rc = rc;
	}
	
	@Override
	public void enterInitialState() {
		this.currentState = SMConstants.getState(this, SMConstants.A_STATE_AUTONOMOUS);

	}

}
