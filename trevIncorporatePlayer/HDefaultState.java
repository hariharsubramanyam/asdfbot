package trevIncorporatePlayer;

import java.util.ArrayList;
import java.util.Collections;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.Clock;
import battlecode.common.Team;

public class HDefaultState extends State{
	
	private ArrayList<EncampmentLoc> encampmentsToCapture;
	public MapLocation myLocation;
	public Robot[] nearbyRobots;
	private int lastBroadcast;
	private int encampChannel = 2345;
	public HDefaultState(StateMachine rootSM){
		this.stateID = SMConstants.SATTACKSTATE;
		this.rootSM = rootSM;
		this.rc = rootSM.getRC();
		this.myLocation = rc.getLocation();
		setEncampmentsToCapture();
		lastBroadcast = 9999999;
		
		
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
			if(this.rc.isActive()){
				for (int i = 0; i<encampmentsToCapture.size(); i++){
					System.out.println("Distance: "+encampmentsToCapture.get(i).distanceFromHQ);
				}
				if(rc.readBroadcast(encampChannel) != lastBroadcast){
					lastBroadcast = createNextBroadcast();
					rc.broadcast(encampChannel, lastBroadcast);
				}
//				MapLocation teamCM = this.getTeamCenterOfMass();
//				this.rc.setIndicatorString(0, teamCM.toString());
//				if(teamCM != null)
//					this.sendCenterOfMassMessage(teamCM);
				Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
				int[] directionOffsets = {0,1,-1,2,-2,3,-3,4};
				for(int d : directionOffsets){
					Direction lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
					if (rc.canMove(lookingAtCurrently) && rc.senseMine(rc.getLocation().add(lookingAtCurrently)) == null &&Clock.getRoundNum()>0){
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
	
	public void setEncampmentsToCapture(){
		encampmentsToCapture = new ArrayList<EncampmentLoc>();
		try{
			MapLocation enemyHQ = rc.senseEnemyHQLocation();
			double hqDis = myLocation.distanceSquaredTo(enemyHQ);
			MapLocation[] encamps = rc.senseEncampmentSquares(myLocation, (Math.round((float)hqDis/5)), Team.NEUTRAL);
			for(MapLocation e : encamps){
				if (e.distanceSquaredTo(myLocation)>4){
					encampmentsToCapture.add(new EncampmentLoc(e, e.distanceSquaredTo(myLocation)));
				}
			}
			Collections.sort(encampmentsToCapture);
			while(encampmentsToCapture.size()>7){
				encampmentsToCapture.remove(7);
			}
			/*ArrayList<EncampmentLoc> mid = new ArrayList<EncampmentLoc>();
			for(int i=encampmentsToCapture.size()-1; i>=0; i--){
				mid.add(encampmentsToCapture.get(i));
			}*/
			//encampmentsToCapture = mid;
			int artCount = 0;
			int encCount = 0;
			int maxArts = Math.min(6, Math.round((float)encampmentsToCapture.size()/5));
			for (EncampmentLoc e: encampmentsToCapture){
				int locVsEnemyHQ = e.location.distanceSquaredTo(enemyHQ);
				if(e.distanceFromHQ<100 && artCount<maxArts && locVsEnemyHQ<hqDis+8){
					artCount++;
					encCount++;
					e.setType(2);
				}
				else{
					if(encCount%2 == 0){
						e.setType(3);
					}
					else{
						e.setType(4);
					}
					encCount++;
				}
			}
		
		} catch (GameActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int createNextBroadcast(){
		if(encampmentsToCapture.isEmpty()){
			return 0;
		}
		else{
			int message = 0;
			EncampmentLoc next = encampmentsToCapture.remove(0);
			message = (next.location.x + next.location.y*1000);
			message += next.type*1000000;
			return message;
		}
	}
	
}
