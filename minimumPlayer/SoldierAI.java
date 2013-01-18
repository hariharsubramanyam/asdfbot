package minimumPlayer;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class SoldierAI extends AI {

	// Never Changes
	public MapLocation HQLoc;
	public MapLocation enemyHQLoc;
	
	// Can Change
	public State state;
	public Robot[] nearbyRobots;
	public MapLocation[] uncapturedEncampments;
	
	public MapLocation goalEncampment;
	
	// Enum
	public enum State{DEFEND, ATTACK, BUILD, SEEK_ENCAMPMENT};
	
	
	// Strategy Specific
	public MapLocation rallyPoint;
	
	public SoldierAI(RobotController rc){
		this.rc = rc;
		this.state = (this.rc.getRobot().getID()%3==1) ? State.SEEK_ENCAMPMENT : State.DEFEND;
		if(this.state == State.SEEK_ENCAMPMENT)
			this.rc.setIndicatorString(0, "SEEKING");
		else
			this.rc.setIndicatorString(0, "DEFENDING");
		this.HQLoc = this.rc.senseHQLocation();
		this.enemyHQLoc = this.rc.senseEnemyHQLocation();
		this.rallyPoint = new MapLocation((3*this.HQLoc.x+this.enemyHQLoc.x)/4,(3*this.HQLoc.y+this.enemyHQLoc.y)/4);
	}
	
	public void act(){
		this.update();
		if(this.state == State.BUILD)
			this.buildAct();
		else if(this.state == State.SEEK_ENCAMPMENT)
			this.seekEncampmentAct();
		else if(this.state == State.DEFEND)
			this.defendAct();
		else if (this.state == State.ATTACK)
			this.attackAct();		
	}
		
	public void buildAct(){
		try{this.rc.captureEncampment(RobotType.ARTILLERY);return;}catch(Exception ex){ex.printStackTrace();}
	}
	
	public void seekEncampmentAct(){
		// find one if we don't have one
		if(this.goalEncampment == null){
			this.goalEncampment = this.getNearbyEncampment();
			if(this.goalEncampment == null){
				this.state = State.DEFEND;
				this.defendAct();
				return;
			}
		}
		
		// if we're at the encampment, say we're capturing an encampment and begin capturing it
		if(this.rc.getLocation().equals(this.goalEncampment)){
			this.state = State.BUILD;
			try {this.rc.captureEncampment(RobotType.ARTILLERY);} catch (GameActionException e) {e.printStackTrace();}
			return;
		}
		// if we're not at encampment, move towards it
		else
			this.moveToLocation(this.goalEncampment);		
	}
	
	public void defendAct(){
		MapLocation enemyLoc = this.closestRobot(this.rc.getTeam().opponent());
		if(enemyLoc != null)
			this.moveToLocation(enemyLoc);
		else
			this.moveToLocation(this.rallyPoint);
	}

	public void attackAct(){
		MapLocation enemyLoc = this.closestRobot(this.rc.getTeam().opponent());
		if(enemyLoc != null)
			this.moveToLocation(enemyLoc);
		else
			this.moveToLocation(this.enemyHQLoc);
	}
	
	public void update(){
		this.nearbyRobots = this.rc.senseNearbyGameObjects(Robot.class);
		if(Clock.getRoundNum() > 200)
			this.state = State.ATTACK;
		else if(Clock.getRoundNum() > 100)
			this.state = State.DEFEND;
	}
	
	public void moveToLocation(MapLocation loc){
		int dist = rc.getLocation().distanceSquaredTo(loc);
		if(dist > 0){
			int[] directionOffsets = {0,1,-1,2,-2};
			Direction dir = rc.getLocation().directionTo(loc);
				
			Direction firstMine = null;
			boolean hasMoved = false;
			
			for (int d: directionOffsets){
				Direction lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
				if(rc.canMove(lookingAtCurrently)){
					if(rc.senseMine(rc.getLocation().add(lookingAtCurrently))==null){
						try {rc.move(lookingAtCurrently);} catch (GameActionException e) {e.printStackTrace();}
						hasMoved = true;
						break;
					}
					else if(firstMine == null){
						firstMine = Direction.values()[lookingAtCurrently.ordinal()];
					}
				}
			}
			
			if(!hasMoved){
				if(firstMine != null){
					try {rc.defuseMine(rc.getLocation().add(firstMine));} catch (GameActionException e) {e.printStackTrace();}
				}
				else{
					try {rc.move(dir.opposite());} catch (GameActionException e) {e.printStackTrace();}
				}
			}
		}
	}

	public MapLocation closestRobot(Team tm){
		MapLocation robotLoc = null;
		int dist;
		
		MapLocation nearestRobotLoc = null;
		int closestDist = 10000000;
		for(Robot r : this.nearbyRobots)
			if(r != null && r.getTeam()==tm){
				try {robotLoc = this.rc.senseRobotInfo(r).location;} catch (GameActionException e) {e.printStackTrace();}
				dist = this.rc.getLocation().distanceSquaredTo(robotLoc);
				if(dist < closestDist){
					nearestRobotLoc = robotLoc;
					closestDist = dist;
				}
			}
		return nearestRobotLoc;
	}
	
	public MapLocation getNearbyEncampment(){
		try {
			MapLocation[] encs = this.rc.senseEncampmentSquares(this.rc.getLocation(), 100, Team.NEUTRAL);
			if(encs != null && encs.length > 0)
				return encs[this.rc.getRobot().getID()%encs.length];
			else
				return null;
		} catch (GameActionException e) {e.printStackTrace();}
		return null;
	}
}
