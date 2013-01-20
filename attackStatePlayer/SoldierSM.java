/**
 * State machine for the soldier
 */
package attackStatePlayer;
import battlecode.common.*;


public class SoldierSM extends StateMachine {

	// Construct and set initial state
	public SoldierSM(RobotController rc){
		this.rc = rc;
		// SWAITSTATE = hang around near our HQ and group together, SATTACKSTATE = attack nearest enemy or enemy HQ
		this.stateIDs = new int[]{SMConstants.SATTACKSTATE, SMConstants.SBUILDSTATE};
		if(Clock.getRoundNum()< 200)
			this.goToState(SMConstants.SBUILDSTATE);	// start in build state
		else
			this.goToState(SMConstants.SATTACKSTATE);
	}

}