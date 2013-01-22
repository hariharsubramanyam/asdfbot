package incorporatingStuff;

import java.util.ArrayList;
import java.util.PriorityQueue;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.Clock;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class HDefaultState extends State{

	public Robot[] nearbyRobots;
	public MapLocation rallyPoint;
	public MapLocation enemyHQ;
	public MapLocation alliedHQ;
	public Robot[] alliedRobots;
	public Robot[] nearbyAlliedRobots;
	public Robot[] enemyRobots;
	public Robot[] nearbyEnemyRobots;
	public MapLocation[] encamp;
	public PriorityQueue<EncampmentSquare> openencamps;
	public int numWaitBots = 0;
	public double previousEnergon;
	public boolean underAttack;
	public int valOnArtilleryChannel;
	
	public HDefaultState(StateMachine rootSM){
		this.stateID = SMConstants.SATTACKSTATE;
		this.rootSM = rootSM;
		this.rc = rootSM.getRC();
	}
		
	public PriorityQueue<EncampmentSquare> sortencamps(MapLocation[] encamp){
		PriorityQueue<EncampmentSquare> sortencamps = new PriorityQueue<EncampmentSquare>();
		for(MapLocation mL : encamp){
			EncampmentSquare currEncamp = new EncampmentSquare(mL.distanceSquaredTo(alliedHQ), mL);
			if(!isTaken(currEncamp) && mL.distanceSquaredTo(alliedHQ) > 3 && mL.distanceSquaredTo(enemyHQ) >= mL.distanceSquaredTo(alliedHQ)){
				sortencamps.add(currEncamp);
			}
		}
/*		this.rc.setIndicatorString(0,sortencamps.toString());
*/		return sortencamps;
	}
	
	public boolean isTaken(EncampmentSquare target){
		try {
			int msgInt = 0;
			int channel = 13*target.location.x*target.location.x+5*target.location.y+3+PlayerConstants.BEING_TAKEN_CHANNEL;
			msgInt = this.rc.readBroadcast(channel);
			if(msgInt == 19541235){
				return true;
/*				MapLocation targLoc = PlayerConstants.intToMapLocation(msgInt);
				return target.equals(new EncampmentSquare(targLoc.distanceSquaredTo(alliedHQ),targLoc));*/
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
			sortedEncamps.add(e.location);
		return sortedEncamps;
	}
	
	public void setUnderAttackMessage(boolean underAttack){
		try{
		if(underAttack)
			this.rc.broadcast(PlayerConstants.HQ_UNDER_ATTACK_CHANNEL,1);
		else
			this.rc.broadcast(PlayerConstants.HQ_UNDER_ATTACK_CHANNEL, 0);
		}catch(Exception ex){ex.printStackTrace();}
	}
	
	public void sendEnemyArtilleryInRangeMessage(int loc){
		try{this.rc.broadcast(PlayerConstants.ARTILLERY_IN_SIGHT_MESSAGE,loc);}catch(Exception ex){ex.printStackTrace();}
	}
	
	public MapLocation enemyArtilleryInShootingRange(){
		int rangeBuffer = 100; // sense out sqrt(rangeBuffer) squares beyond artillery range 
		Robot[] robs = this.rc.senseNearbyGameObjects(Robot.class, rc.getLocation(), RobotType.ARTILLERY.attackRadiusMaxSquared+rangeBuffer, rc.getTeam().opponent());
		RobotInfo robInf;
		for(Robot r : robs){
			if(r == null)
				continue;
			try {
				robInf = rc.senseRobotInfo(r);
				if(robInf.type == RobotType.ARTILLERY)
					return robInf.location;
			} catch (GameActionException ex) {ex.printStackTrace();}
		}
		return null;
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
*/							
				double hqDis = Math.sqrt(alliedHQ.distanceSquaredTo(enemyHQ));
				alliedRobots = rc.senseNearbyGameObjects(Robot.class,100000,rc.getTeam());
				nearbyAlliedRobots = rc.senseNearbyGameObjects(Robot.class,14,rc.getTeam());
				enemyRobots = rc.senseNearbyGameObjects(Robot.class, 100000,rc.getTeam().opponent());
				nearbyEnemyRobots = rc.senseNearbyGameObjects(Robot.class,14,rc.getTeam().opponent());
				encamp = rc.senseEncampmentSquares(rc.getLocation(), 100000, Team.NEUTRAL);
				openencamps = sortencamps(encamp);
/*				rc.setIndicatorString(0, sortedEncamps.toString());
*//*				ArrayList<MapLocation> encamps = new ArrayList<MapLocation>();
				for(MapLocation camp : encamp){
					encamps.add(camp);
				}*/
//				MapLocation teamCM = this.getTeamCenterOfMass();
//				this.rc.setIndicatorString(0, teamCM.toString());
//				if(teamCM != null)
//					this.sendCenterOfMassMessage(teamCM);
				if(this.previousEnergon > this.rc.getEnergon() && !this.underAttack){
					if(Clock.getRoundNum() < GameConstants.ROUND_MIN_LIMIT){
						this.setUnderAttackMessage(true);
						this.underAttack = true;
					}
				}
				
				else if(this.previousEnergon == this.rc.getEnergon() && this.underAttack){
					this.rc.setIndicatorString(0, "UNDER ATTACK!");
					this.underAttack = false;
					this.setUnderAttackMessage(false);
				}
				
				this.previousEnergon = this.rc.getEnergon();
				
				if(!this.underAttack){
					MapLocation artilleryLoc = this.enemyArtilleryInShootingRange();
					int convertedArtLoc = 0;
					if(artilleryLoc != null)
						convertedArtLoc = PlayerConstants.mapLocationToInt(artilleryLoc);
					
					if(this.valOnArtilleryChannel != convertedArtLoc){
						this.valOnArtilleryChannel = convertedArtLoc;
						this.sendEnemyArtilleryInRangeMessage(convertedArtLoc);
					}
				}
				
				
				int artCount = 0;
				int encCount = 0;
				int maxArts = Math.min(6, Math.round((float)openencamps.size()/5));
				
				for (EncampmentSquare e: openencamps){
					int locVsEnemyHQ = e.location.distanceSquaredTo(enemyHQ);
					if(e.distance<100 && artCount<maxArts && locVsEnemyHQ<(Math.pow((hqDis),2)+8)){
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
				
				if(openencamps.size() > 0){
					this.rc.broadcast(PlayerConstants.ENCAMPMENT_LOCATION_CHANNEL, PlayerConstants.encampmentSquareToInt(openencamps.remove()));
				}
				
				Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
				int[] directionOffsets = {0,1,-1,2,-2,3,-3,4};
				for(int d : directionOffsets){
					Direction lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
					if (rc.canMove(lookingAtCurrently) && Clock.getRoundNum()>0){
						rc.spawn(lookingAtCurrently);
						if (numWaitBots >= 4 && openencamps.size() > 0){
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
