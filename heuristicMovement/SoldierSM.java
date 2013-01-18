/**
 * State machine for the soldier
 */
package heuristicMovement;
import battlecode.common.*;


public class SoldierSM extends StateMachine {

	// Construct and set initial state
	public SoldierSM(RobotController rc){
		this.rc = rc;
		// SWAITSTATE = hang around near our HQ and group together, SATTACKSTATE = attack nearest enemy or enemy HQ
		this.stateIDs = new int[]{SMConstants.SWAITSTATE, SMConstants.SATTACKSTATE, SMConstants.SBUILDSTATE};
		this.goToState(SMConstants.SWAITSTATE);	// start in build state
	}

}