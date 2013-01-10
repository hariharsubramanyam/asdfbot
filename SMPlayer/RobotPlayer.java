package SMPlayer;

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
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
