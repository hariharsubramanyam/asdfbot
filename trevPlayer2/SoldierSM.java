/**
 * State machine for the soldier
 */
package trevPlayer2;
import battlecode.common.*;


public class SoldierSM extends StateMachine {

	// Construct and set initial state
	public SoldierSM(RobotController rc){
		this.rc = rc;
		// SWAITSTATE = hang around near our HQ and group together, SATTACKSTATE = attack nearest enemy or enemy HQ
		this.stateIDs = new int[]{SMConstants.SWAITSTATE, SMConstants.SATTACKSTATE, SMConstants.SBUILDSTATE};
		//try {
			//if(rc.readBroadcast(2345)>999999){
				this.goToState(SMConstants.SBUILDSTATE);	// start in build state
			//}
			//else{
			//	this.goToState(SMConstants.SATTACKSTATE);
			//}
		//} //catch (GameActionException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		//}
	}

}