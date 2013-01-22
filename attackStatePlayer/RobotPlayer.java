/**
 * Robot execution begins here
 */
package attackStatePlayer;

import battlecode.common.*;

public class RobotPlayer {
	
	// Must be either HQSM, SoldierSM, or ArtillerySM
	public static StateMachine sm;
	
	public static void run(RobotController rc) throws GameActionException{		
		
		// Determine the type of robot and assign the proper state machine		
		try{
			if(rc.getType()==RobotType.SOLDIER)
				sm = new SoldierSM(rc);
			else if(rc.getType() == RobotType.HQ)
				sm = new HQSM(rc);
		}catch(Exception ex){ex.printStackTrace();}
		
		
		// game loop and exception handling
		while(true){
			try{
				if(rc.getType()==RobotType.ARTILLERY && rc.isActive()){
					Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,63,rc.getTeam().opponent());
					// find the closest enemy
					if(enemyRobots.length > 0){
						int closestDist = 10000000;
						MapLocation closestEnemy = null;
						for(int i = 0; i < enemyRobots.length; i++){
							Robot aRobot = enemyRobots[i];
							RobotInfo aRobotInfo = rc.senseRobotInfo(aRobot);
							int dist = aRobotInfo.location.distanceSquaredTo(rc.getLocation());
							if(dist < closestDist){
								closestDist = dist;;
								closestEnemy = aRobotInfo.location;
							}
						}
						if(closestEnemy != null){
							rc.attackSquare(closestEnemy);
						}
					}	
				}
				else if (rc.getType() == RobotType.SOLDIER || rc.getType() == RobotType.HQ){
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
