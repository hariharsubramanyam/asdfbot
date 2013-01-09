package controllerapproach;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class HQController implements IController {

	private RobotController rc;
	private RobotData data;
	
	public HQController(RobotController rc){
		this.rc = rc;
		this.data = new RobotData(rc.getRobot().getID(),rc.getLocation(),rc.getEnergon(),rc.getShields(),rc.getType(),rc.isActive());
	}
	
	@Override
	public void act() {
		// TODO Auto-generated method stub

	}

	@Override
	public void yield() {
		this.rc.yield();
	}

}
