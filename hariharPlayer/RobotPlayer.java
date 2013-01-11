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
		try{
			if(rc.getType()==RobotType.SOLDIER)
				sm = new SoldierSM(rc);
		}catch(Exception ex){ex.printStackTrace();}
		
		
		// game loop and exception handling
		while(true){
			try{
				// for now, hard code the HQ behavior 
				if(sm==null){
					if (rc.isActive()) {
						Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
						if (rc.canMove(dir)){
							rc.spawn(dir);
						}
					}
				}
				else{
					// if it's a soldier, run the state machine for the turn
					sm.step();
				}
				
				// end turn
				rc.yield();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
