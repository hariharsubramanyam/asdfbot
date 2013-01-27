package team092;


import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.Clock;
import battlecode.common.Upgrade;

public class HDefaultState extends State{
	
	public MapLocation myLocation;
	public Robot[] nearbyRobots;
	public boolean nukeMode;
	
	public HDefaultState(StateMachine rootSM){
		this.stateID = SMConstants.HDEFAULTSTATE;
		this.rootSM = rootSM;
		this.rc = rootSM.getRC();
		this.myLocation = rc.getLocation();
		nukeMode = false;
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
			if(rc.senseNearbyGameObjects(Robot.class, rc.getLocation(), 625, rc.getTeam()).length > PlayerConstants.NUM_ROBOTS_IN_ATTACK_GROUP)
				nukeMode = true;
			if(nukeMode)
				this.rc.researchUpgrade(Upgrade.NUKE);
			else
				spawnSoldier();
		}catch(Exception ex){ex.printStackTrace();}
	}
	
	public void spawnSoldier(){
		Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
		int[] directionOffsets = {0,1,-1,2,-2,3,-3,4};
		for(int d : directionOffsets){
			Direction lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
			if (rc.canMove(lookingAtCurrently) && rc.senseMine(rc.getLocation().add(lookingAtCurrently)) == null &&Clock.getRoundNum()>0){
				try {
					rc.spawn(lookingAtCurrently);
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}
	}
	
}
