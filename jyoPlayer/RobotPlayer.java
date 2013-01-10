package jyoPlayer;

import battlecode.common.*;

public class RobotPlayer {
	
	public static RobotController rc;
	public static MapLocation rallyPoint;
	public static StateMachine sm;
	
	public static void run(RobotController rc) throws GameActionException{
		
		RobotPlayer.rc = rc;
		rallyPoint = findRallyPoint();
		
		try{
			if(rc.getType()==RobotType.SOLDIER){
				sm = new SoldierSM(rc);
			}
		}catch(Exception ex){ex.printStackTrace();}
		
		
		while(true){
			try{
				if(sm==null){
					if (rc.isActive()) {
						Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
						if (rc.canMove(dir))
							rc.spawn(dir);
					}
				}
				else{
					sm.step();
				}
				rc.yield();
			}
			catch(Exception e){
				e.printStackTrace();
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
