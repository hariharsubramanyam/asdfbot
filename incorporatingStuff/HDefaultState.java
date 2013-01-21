package incorporatingStuff;

import java.util.ArrayList;
import java.util.PriorityQueue;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.Clock;
import battlecode.common.Team;
import battlecode.engine.instrumenter.lang.System;

public class HDefaultState extends State{

/*	public Robot[] nearbyRobots;*/
	public MapLocation rallyPoint;
	public MapLocation enemyHQ;
	public MapLocation alliedHQ;
	public MapLocation myLocation;
	public Robot[] alliedRobots;
	public Robot[] nearbyAlliedRobots;
	public Robot[] enemyRobots;
	public Robot[] nearbyEnemyRobots;
	public MapLocation[] encamp;
	public PriorityQueue<EncampmentSquare> openencamps;
	public ArrayList<MapLocation> sortedEncamps;
	public int numWaitBots = 0;
	
	public HDefaultState(StateMachine rootSM){
		this.stateID = SMConstants.SATTACKSTATE;
		this.rootSM = rootSM;
		this.rc = rootSM.getRC();
	}
	
	public class EncampmentSquare implements Comparable<EncampmentSquare>{
		public int distance;		// distance to square
		public int r;				// row (x-coordinate) of square
		public int c;				// column (y-coordinate) of square
		
		public EncampmentSquare(int distance, int r, int c){
			this.distance = distance;
			this.r = r;
			this.c = c;
		}
		
		// gridSquareA.equals(gridSquareB) if they have the same coordinates
		@Override
		public boolean equals(Object other){
			EncampmentSquare o = (EncampmentSquare)other;
			return (this.r == o.r && this.c == o.c);
		}

		public int compareTo(EncampmentSquare o) {
			if(this.distance > o.distance)
				return 1;
			else if (this.distance < o.distance)
				return -1;
			else
				return 0;
		}
	}
	
	public PriorityQueue<EncampmentSquare> sortencamps(MapLocation[] encamp){
		PriorityQueue<EncampmentSquare> sortencamps = new PriorityQueue<EncampmentSquare>();
		for(MapLocation mL : encamp){
			EncampmentSquare currEncamp = new EncampmentSquare(mL.distanceSquaredTo(alliedHQ),mL.x,mL.y);
			if(!isTaken(mL) && mL.distanceSquaredTo(alliedHQ) > 3){
				sortencamps.add(currEncamp);
			}
		}
/*		this.rc.setIndicatorString(0,sortencamps.toString());
*/		return sortencamps;
	}
	
	public boolean isTaken(MapLocation encamp){
		try {
			int msgInt = 0;
			int channel = 2*encamp.x+encamp.y+PlayerConstants.BEING_TAKEN_CHANNEL;
			msgInt = this.rc.readBroadcast(channel);
			if(msgInt != 0){
				String msg = "" + msgInt;
/*				rc.setIndicatorString(0, msg);
*/				MapLocation square = new MapLocation(Integer.parseInt(msg.substring(5)),Integer.parseInt(msg.substring(1,4)));
				return square.equals(encamp);
			}
			else
				return false;
		} catch (GameActionException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public ArrayList<MapLocation> sortedEncamps (PriorityQueue<EncampmentSquare> sortencamps){
		ArrayList<MapLocation> sortedEncamps = new ArrayList<MapLocation>();
		for (EncampmentSquare e : sortencamps)
			sortedEncamps.add(new MapLocation(e.r,e.c));
		return sortedEncamps;
	}

	public void sendEncampmentLocation(MapLocation loc){
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
/*		this.rc.setIndicatorString(0, msg.toString());
*/		try {
			this.rc.broadcast(PlayerConstants.ENCAMPMENT_LOCATION_CHANNEL, Integer.parseInt(msg));
		} catch (GameActionException e) {e.printStackTrace();}
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
/*		this.rc.setIndicatorString(0, msg);
*/		try {
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
				if(enemyHQ == null)
					enemyHQ = rc.senseEnemyHQLocation();
				if(alliedHQ == null)
					alliedHQ = rc.senseHQLocation();
/*				rc.setIndicatorString(0, "" + numWaitBots);
*/				myLocation = rc.getLocation();
				alliedRobots = rc.senseNearbyGameObjects(Robot.class,100000,rc.getTeam());
				nearbyAlliedRobots = rc.senseNearbyGameObjects(Robot.class,14,rc.getTeam());
				enemyRobots = rc.senseNearbyGameObjects(Robot.class, 100000,rc.getTeam().opponent());
				nearbyEnemyRobots = rc.senseNearbyGameObjects(Robot.class,14,rc.getTeam().opponent());
				encamp = rc.senseEncampmentSquares(rc.getLocation(), 100000, Team.NEUTRAL);
				openencamps = sortencamps(encamp);
				sortedEncamps = sortedEncamps(openencamps);
/*				rc.setIndicatorString(0, sortedEncamps.toString());
*//*				ArrayList<MapLocation> encamps = new ArrayList<MapLocation>();
				for(MapLocation camp : encamp){
					encamps.add(camp);
				}*/
//				MapLocation teamCM = this.getTeamCenterOfMass();
//				this.rc.setIndicatorString(0, teamCM.toString());
//				if(teamCM != null)
//					this.sendCenterOfMassMessage(teamCM);
				Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
				int[] directionOffsets = {0,1,-1,2,-2,3,-3,4};
				for(int d : directionOffsets){
					Direction lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
					if (rc.canMove(lookingAtCurrently) && Clock.getRoundNum()>0){
						rc.spawn(lookingAtCurrently);
						if (numWaitBots >= 4){
							rc.broadcast(PlayerConstants.STATE_ASSIGNMENT_CHANNEL,1);
							numWaitBots = 0;
						}
						else{
							rc.broadcast(PlayerConstants.STATE_ASSIGNMENT_CHANNEL,2);
							numWaitBots++;
						}
						break;
					}
				}
				rc.setIndicatorString(0, sortedEncamps.get(0).toString());
				sendEncampmentLocation(sortedEncamps.get(0));
			}
		}catch(Exception ex){ex.printStackTrace();}
	}
	
/*	public MapLocation getTeamCenterOfMass(){
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
*/	
}
