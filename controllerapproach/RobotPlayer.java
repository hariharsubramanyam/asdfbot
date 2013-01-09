package controllerapproach;
import battlecode.common.*;

public class RobotPlayer {
	public static GeneralController controller;
	public static void run(RobotController rc) throws GameActionException{
		while(true){
			try{
				switch(rc.getType()){
				case SOLDIER:
					controller = new SoldierController(rc);
					break;
				case HQ:
					controller = new HQController(rc);
					break;
					
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	


}
