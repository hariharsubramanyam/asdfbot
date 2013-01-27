/**
 * Robot execution begins here
 */
package noSMPlayer;


import battlecode.common.*;

public class RobotPlayer {
	public static void run(RobotController rc) throws GameActionException{
		
		AIRobot ai = null;
		RobotType type = rc.getType();
		if(type== RobotType.SOLDIER)
			ai = new AISoldier(rc);
		else if(type == RobotType.HQ)
			ai = new AIHQ(rc);
		
		while(true){
			try{
				if(ai.rc.isActive())
					ai.act();
				ai.rc.yield();
			}
			catch(Exception ex){ex.printStackTrace();};
		}
	}
}
