package awesomeRobotPlayer;
import battlecode.common.*;

public class RobotPlayer {
	
	public static RobotController rc;
	public static MapLocation rallyPoint;
	
	public static void run(RobotController rc) throws GameActionException{
		RobotPlayer.rc = rc;
		rallyPoint = findRallyPoint();
		while(true){
			try{
				if(rc.getType() == RobotType.SOLDIER && rc.isActive()){
					Robot[] enemyRobots = (Robot[]) rc.senseNearbyGameObjects(Robot.class,100000,rc.getTeam().opponent());
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
						goToLocation(closestEnemy);
					}
					else if(Clock.getRoundNum()<200){
						goToLocation(rallyPoint);
					}
					else{
						goToLocation(rc.senseEnemyHQLocation());
					}
				}
				else{
					if(rc.getType() == RobotType.HQ){
						if (rc.isActive()) {
							Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
							if (rc.canMove(dir))
								rc.spawn(dir);
						}
					}
				}
				rc.yield();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	private static void goToLocation(MapLocation place)
			throws GameActionException {
		int dist = rc.getLocation().distanceSquaredTo(place);
		if(dist > 0){
			int[] directionOffsets = {0,1,-1,2,-2};
			Direction dir = rc.getLocation().directionTo(place);
			for (int d: directionOffsets){
				Direction lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
				if(rc.canMove(lookingAtCurrently)){
					rc.move(lookingAtCurrently);
					break;
				}
			}
		}
	}

	private static MapLocation findRallyPoint() {
		MapLocation enemyLoc = rc.senseEnemyHQLocation();
		MapLocation ourLoc = rc.senseHQLocation();
		int x = (enemyLoc.x + 3*ourLoc.x)/4;
		int y = (enemyLoc.y + 3*ourLoc.y)/4;
		return new MapLocation(x,y);
	}


	


}
