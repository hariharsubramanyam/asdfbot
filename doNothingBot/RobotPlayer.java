/**
 * Robot execution begins here
 */
package doNothingBot;

import battlecode.common.*;

public class RobotPlayer {
	public static void run(RobotController rc) throws GameActionException{
		while(true){
			try{
				if(rc.isActive())
					rc.yield();
			}
			catch(Exception ex){ex.printStackTrace();};
		}
	}
}
