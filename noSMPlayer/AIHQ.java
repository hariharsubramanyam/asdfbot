package noSMPlayer;

import noSMPlayer.ManagerData;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class AIHQ extends AIRobot{
	
	public ManagerData dataMan;
	
	public AIHQ(RobotController rc){
		this.rc = rc;
		this.dataMan = new ManagerData(rc);
	}
	
	@Override
	public void act() {
		MapLocation HQLoc = dataMan.getLocation();
		MapLocation enemyHQLoc = dataMan.getEnemyHQLoc();
		Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
		if(rc.canMove(dir))
			try {rc.spawn(dir);} catch (GameActionException e) {e.printStackTrace();}
	}
	
	public void updateManagers(){
		dataMan.update(true);
	}
	

}
