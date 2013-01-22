/**
 * State machine for the soldier
 */
package team092;
import battlecode.common.*;



public class SoldierSM extends StateMachine {

	// Construct and set initial state
	public SoldierSM(RobotController rc){
		this.rc = rc;
		// SWAITSTATE = hang around near our HQ and group together, SATTACKSTATE = attack nearest enemy or enemy HQ
		this.stateIDs = new int[]{SMConstants.SWAITSTATE, SMConstants.SATTACKSTATE, SMConstants.SBUILDSTATE};
		int assignment = getAssignment();
		if (assignment == 1)
			this.goToState(SMConstants.SBUILDSTATE);	// start in build state
		else if (assignment == 2)
			this.goToState(SMConstants.SWAITSTATE);
	}
	
	int getAssignment(){
		try{
			return rc.readBroadcast(PlayerConstants.STATE_ASSIGNMENT_CHANNEL);
		}catch (GameActionException e){
			e.printStackTrace();
			return 2;
		}
	}

}