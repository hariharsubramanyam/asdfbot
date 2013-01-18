package minimumPlayer;



import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Direction;

public class HQAI extends AI{
	
	public MapLocation enemyHQLoc;
	public MapLocation location;
	
	public HQAI(RobotController rc){
		this.rc = rc;
		this.location = this.rc.getLocation();
		this.enemyHQLoc = this.rc.senseEnemyHQLocation();
	}

	@Override
	public void act() {
		Direction targetDir = this.location.directionTo(this.enemyHQLoc);
		if(this.rc.canMove(targetDir))
			try {this.rc.spawn(targetDir);} catch (GameActionException e) {e.printStackTrace();}
		
	}

}
