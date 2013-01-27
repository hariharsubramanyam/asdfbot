package minimumPlayer;

import battlecode.common.GameActionException;
import battlecode.common.Robot;
import battlecode.common.RobotController;

public class ArtilleryAI extends AI{

	public ArtilleryAI(RobotController rc){
		this.rc = rc;
	}
	@Override
	public void act() {
		Robot[] nearby = this.rc.senseNearbyGameObjects(Robot.class, 20, this.rc.getTeam().opponent());
		if(nearby != null && nearby.length != 0)
			try {
				this.rc.attackSquare(this.rc.senseRobotInfo(nearby[0]).location);
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	}

}
