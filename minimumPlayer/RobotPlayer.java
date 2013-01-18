package minimumPlayer;

import battlecode.common.RobotController;
import battlecode.common.RobotType;


public class RobotPlayer {
	public static void run(RobotController rc){
		AI ai = null;
		RobotType type = rc.getType();
		
		if(type== RobotType.SOLDIER)
			ai = new SoldierAI(rc);
		else if(type == RobotType.HQ)
			ai = new HQAI(rc);
		else if(type == RobotType.ARTILLERY)
			ai = new ArtilleryAI(rc);
		
		while(true){
			try{
				if(ai == null)
					rc.yield();
				else if(ai.rc.isActive()){
					ai.act();
					ai.rc.yield();
				}
			}
			catch(Exception ex){ex.printStackTrace();};
		}
	}

}
