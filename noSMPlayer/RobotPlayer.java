/**
 * Robot execution begins here
 */
package noSMPlayer;

import battlecode.common.*;

public class RobotPlayer {
	public static void run(RobotController rc) throws GameActionException{
		
		if(rc.getType() == RobotType.SOLDIER){
			SoldierController sol = new SoldierController(rc);
			sol.run();
		}
		
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
