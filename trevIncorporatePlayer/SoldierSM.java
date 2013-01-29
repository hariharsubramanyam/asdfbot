/**
 * State machine for the soldier
 */
package trevIncorporatePlayer;
import battlecode.common.*;


public class SoldierSM extends StateMachine {
	private int encampChannel = 2345;

	// Construct and set initial state
	public SoldierSM(RobotController rc){
		this.rc = rc;
		// SWAITSTATE = hang around near our HQ and group together, SATTACKSTATE = attack nearest enemy or enemy HQ
		this.stateIDs = new int[]{SMConstants.SWAITSTATE, SMConstants.SATTACKSTATE, SMConstants.SBUILDSTATE, SMConstants.SSCOUTSTATE};
		try {
			if(Clock.getRoundNum()<=10){
				this.goToState(SMConstants.SSCOUTSTATE);
			}
			else if (rc.readBroadcast(encampChannel) > 999999){
				this.goToState(SMConstants.SBUILDSTATE);	// start in build state
			}
			else{
				this.goToState(SMConstants.SWAITSTATE);
			}
		} catch (GameActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}