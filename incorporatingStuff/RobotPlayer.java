/**
 * Robot execution begins here
 */
package incorporatingStuff;

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
				// for now, hard code the HQ behavior 
				if(sm==null){
/*					if (rc.getType()==RobotType.HQ && rc.isActive()) {
						Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
						int[] directionOffsets = {0,1,-1,2,-2,3,-3,4};
						for(int d : directionOffsets){
							Direction lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
							if (rc.canMove(lookingAtCurrently)){
								rc.spawn(lookingAtCurrently);
								break;
							}
						}
					}*/
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
