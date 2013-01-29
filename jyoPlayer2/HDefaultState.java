package jyoPlayer2;


import java.util.ArrayList;
import java.util.Collections;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.Clock;
import battlecode.common.Team;
import battlecode.common.Upgrade;

public class HDefaultState extends State{
	
	public MapLocation myLocation;
	public Robot[] nearbyRobots;
	public boolean nukeMode;
	private ArrayList<EncampmentLoc> encampmentsToCapture;
	private int lastBroadcast;
	private int encampChannel = 9551;
	
	public HDefaultState(StateMachine rootSM){
		this.stateID = SMConstants.HDEFAULTSTATE;
		this.rootSM = rootSM;
		this.rc = rootSM.getRC();
		this.myLocation = rc.getLocation();
		nukeMode = false;
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
			if(rc.readBroadcast(encampChannel) != lastBroadcast){
				lastBroadcast = createNextBroadcast();
				rc.broadcast(encampChannel, lastBroadcast);
			}
			if(rc.senseNearbyGameObjects(Robot.class, rc.getLocation(), 625, rc.getTeam()).length > PlayerConstants.NUM_ROBOTS_IN_ATTACK_GROUP)
				nukeMode = true;
			else
				nukeMode = false;
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
	
	public void setEncampmentsToCapture(){
		encampmentsToCapture = new ArrayList<EncampmentLoc>();
		try{
			MapLocation enemyHQ = rc.senseEnemyHQLocation();
			double hqDis = myLocation.distanceSquaredTo(enemyHQ);
			MapLocation[] encamps = rc.senseEncampmentSquares(myLocation, (Math.round((float)hqDis/4)), Team.NEUTRAL);
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
		if(this.encampmentsToCapture.isEmpty()){
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
