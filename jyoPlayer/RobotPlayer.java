package jyoPlayer;

import battlecode.common.*;

public class RobotPlayer {
	public static StateMachine sm;
	
	public static void run(RobotController rc) throws GameActionException{		
		try{
			if(rc.getType()==RobotType.SOLDIER)
				sm = new SoldierSM(rc);
		}catch(Exception ex){ex.printStackTrace();}
		
		
		while(true){
			try{
				if(rc.getType()==RobotType.HQ){
					if (rc.isActive()) {
						Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
						if (rc.canMove(dir))
							rc.spawn(dir);
					}
				}
				sm.step();
				rc.yield();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
