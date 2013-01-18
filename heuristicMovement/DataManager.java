package heuristicMovement;

import java.util.ArrayList;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class DataManager {
	
	public StateMachine sm;
	public RobotController rc;
	public int lastUpdate;
	
	// Never changes
/*	public RobotType type;
	public Team team;
	public MapLocation HQLoc;
	public MapLocation enemyHQLoc;
	public int mapWidth;
	public int mapHeight;
	public MapLocation[] allEncampments;
	public long[] teamMemory;*/
	public int birth;
	
	// Changes
/*	public double energon;
	public MapLocation location;
	public double shields;
	public double teamPower;
	public Robot[] nearbyRobots;
	public MapLocation[] alliedEncampments;
	public MapLocation[] enemyMines;
	public MapLocation[] myMines;*/
	public int age;
	
	public DataManager(StateMachine sm){
		this.sm = sm;
		this.rc = sm.rc;
		this.lastUpdate = 0;
		
/*		this.type = null;
		this.team = null;
		this.HQLoc = null;
		this.enemyHQLoc = null;
		this.mapWidth = 0;
		this.mapHeight = 0;
		this.allEncampments = null;
		this.teamMemory = null;*/
		this.birth = 0;
	}
	
	public void update(boolean shouldGetAlliedEncampments, boolean shouldGetMyMines, boolean shouldGetEnemyMines){
		int currRound = Clock.getRoundNum();
		
/*		if(this.type == null)
			this.type = this.rc.getType();
		if(this.team == null)
			this.team = this.rc.getTeam();
		if(this.HQLoc == null)
			this.HQLoc = this.rc.senseHQLocation();
		if(this.enemyHQLoc == null)
			this.enemyHQLoc = this.rc.senseEnemyHQLocation();
		if(this.mapWidth == 0)
			this.mapWidth = this.rc.getMapWidth();
		if(this.mapHeight == 0)
			this.mapHeight = this.rc.getMapHeight();
		if(this.allEncampments == null)
			this.allEncampments = this.rc.senseAllEncampmentSquares();
		if(this.teamMemory == null)
			this.teamMemory = this.rc.getTeamMemory();*/
		if(this.birth == 0)
			this.birth = Clock.getRoundNum();
		
		if(currRound != this.lastUpdate){
/*			this.energon = this.rc.getEnergon();
			this.shields = this.rc.getShields();
			this.location = this.rc.getLocation();
			this.teamPower = this.rc.getTeamPower();
			this.nearbyRobots = this.rc.senseNearbyGameObjects(Robot.class);*/
			this.age = Clock.getRoundNum() - this.birth;
		}
		
		
/*		if(shouldGetAlliedEncampments)
			this.alliedEncampments = this.rc.senseAlliedEncampmentSquares();
		if(shouldGetMyMines)
			this.myMines = this.rc.senseMineLocations(new MapLocation(this.mapWidth/2,this.mapHeight/2), 100, this.team);
		if(shouldGetEnemyMines)
			this.enemyMines = this.rc.senseNonAlliedMineLocations(new MapLocation(this.mapWidth/2,this.mapHeight/2), 100);*/
	
		this.lastUpdate = currRound;
	}

	public int timeSinceLastUpdate(){
		return Clock.getRoundNum() - this.lastUpdate;
	}
	
/*	public Robot[] getNearbyEnemies(){
		ArrayList<Robot> enemies = new ArrayList<Robot>();
		for(Robot r : this.nearbyRobots)
			if(r.getTeam() == this.team.opponent())
				enemies.add(r);
		return enemies.toArray(new Robot[0]);
	}*/

/*	public MapLocation getLocationOfNearestEnemy(int distThreshold){
		Robot[] enemies = this.getNearbyEnemies();
		
		MapLocation nearestEnemyLoc = null;
		int closestDist = 100000;
		
		MapLocation enemyLoc;
		int dist;
		
		for(Robot e : enemies)
			try {
				enemyLoc = rc.senseRobotInfo(e).location;
				dist = enemyLoc.distanceSquaredTo(this.location);
				if(dist < closestDist){
					closestDist = dist;
					nearestEnemyLoc = enemyLoc;
				}
			} catch (GameActionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		if(closestDist > distThreshold)
			return null;
		return nearestEnemyLoc;
	}*/
}
