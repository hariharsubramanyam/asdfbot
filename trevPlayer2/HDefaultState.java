package trevPlayer2;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;

public class HDefaultState extends State{

	public Robot[] nearbyRobots;
	public HDefaultState(StateMachine rootSM){
		this.stateID = SMConstants.SATTACKSTATE;
		this.rootSM = rootSM;
		this.rc = rootSM.getRC();
	}
	
	public void sendCenterOfMassMessage(MapLocation loc){
		int x = loc.x;
		int y = loc.y;
		String msg = "1";
		if(y < 10)
			msg += "00"+y;
		else if(y < 100)
			msg += "0"+y;
		else
			msg += y;
		
		msg += "0";
		
		if(x < 10)
			msg += "00" + x;
		else if(x < 100)
			msg += "0" + x;
		else
			msg += x;
		this.rc.setIndicatorString(0, msg);
		try {
			this.rc.broadcast(PlayerConstants.HQ_CENTER_OF_MASS_CHANNEL, Integer.parseInt(msg));
		} catch (GameActionException e) {e.printStackTrace();}
	}
	
	@Override
	public void doEntryAct() {}

	@Override
	public void doExitAct() {}

	@Override
	public void doAction() {
		try{
			if(this.rc.isActive()&&Clock.getRoundNum()>0){
				MapLocation teamCM = this.getTeamCenterOfMass();
				this.rc.setIndicatorString(0, teamCM.toString());
				if(teamCM != null)
					this.sendCenterOfMassMessage(teamCM);
				Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
				int[] directionOffsets = {0,1,-1,2,-2,3,-3,4};
				for(int d : directionOffsets){
					Direction lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
					if (rc.canMove(lookingAtCurrently)){
						rc.spawn(lookingAtCurrently);
						break;
					}
				}
			}
		}catch(Exception ex){ex.printStackTrace();}
	}
	
	public MapLocation getTeamCenterOfMass(){
		this.nearbyRobots = this.rc.senseNearbyGameObjects(Robot.class);
		if(this.nearbyRobots == null)
			return this.rc.getLocation();
		int x = 0, y = 0;
		int count = 0;
		MapLocation robotLoc;
		for(Robot r : this.nearbyRobots)
			if(r != null && r.getTeam() == this.rc.getTeam()){
				try {
					robotLoc = this.rc.senseRobotInfo(r).location;
					x += robotLoc.x;
					y += robotLoc.y;
					count++;
				} catch (GameActionException e) {e.printStackTrace();}
			}
		if(x == 0 && y == 0)
			return this.rc.getLocation();
		MapLocation res = new MapLocation((int)(1.0*x/count),(int)(1.0*y/count));
		return res;
	}
	
}
