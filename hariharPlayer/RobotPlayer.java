/**
* Robot execution begins here
 */
package hariharPlayer;

import battlecode.common.*;

public class RobotPlayer {
	
	// Must be either HQSM, SoldierSM, or ArtillerySM
	public static StateMachine sm;
	
	public static void run(RobotController rc) throws GameActionException{
		
		// Determine the type of robot and assign the proper state machine
		while(true){
			try{
				if(sm != null){
					sm.step();
					sm.rc.yield();
				}
				else{
					if(rc.getType() == RobotType.SOLDIER)
						sm = new SoldierSM(rc);
					else if(rc.getType() == RobotType.HQ)
						sm = new HQSM(rc);
					else if(rc.getType() == RobotType.ARTILLERY)
						sm = new ArtillerySM(rc);
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
