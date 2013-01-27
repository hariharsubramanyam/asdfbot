/**
 * Creates controller based on robot type and runs it
 */
package controllerapproach;
import battlecode.common.*;

public class RobotPlayer {
	public static IController controller;
	public static void run(RobotController rc) throws GameActionException{
		try{
			switch(rc.getType()){
				//case SOLDIER: controller = new SoldierController(rc); break;
				case HQ: controller = new HQController(rc); break;
				//case ARTILLERY: controller = new ArtilleryController(rc); break;
			}//endswitch
		}//endtry
		catch(Exception e){e.printStackTrace();}//endcatch
		
		while(true){
			try{
				controller.act();
				controller.yield();
			}//endtry
			catch(Exception ex){ex.printStackTrace();}//endcatch
		}//endwhile
		
	}//endmethod
	
}//endclass
