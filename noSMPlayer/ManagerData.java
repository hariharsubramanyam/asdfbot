package noSMPlayer;



import java.util.ArrayList;

import battlecode.common.Clock;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class ManagerData {
	
	private RobotController rc;
	private int lastUpdate;
	
	// Never changes
	private RobotType type;
	private Team team;
	private MapLocation HQLoc;
	private MapLocation enemyHQLoc;
	private int mapWidth;
	private int mapHeight;
	private MapLocation[] allEncampments;
	private long[] teamMemory;
	
	// Changes
	private double energon;
	private MapLocation location;
	private double shields;
	private double teamPower;
	private Robot[] nearbyRobots;
	private MapLocation[] alliedEncampments;
	private MapLocation[] enemyMines;
	private MapLocation[] myMines;
	
	public ManagerData(RobotController rc){
		this.rc = rc;
		this.lastUpdate = -1;
		
		this.type = null;
		this.team = null;
		this.HQLoc = null;
		this.enemyHQLoc = null;
		this.mapWidth = -1;
		this.mapHeight = -1;
		this.allEncampments = null;
		this.teamMemory = null;
		
		this.energon = -1;
		this.location = null;
		this.shields = -1;
		this.teamPower = -1;
		this.nearbyRobots = null;
		this.alliedEncampments = null;
		this.enemyMines = null;
		this.myMines = null;
		this.updatePermanents();
		this.update(true);
	}
	
	private void updatePermanents(){
		this.type = this.rc.getType();
		this.team = this.rc.getTeam();
		this.HQLoc = this.rc.senseHQLocation();
		this.enemyHQLoc = this.rc.senseEnemyHQLocation();
		this.mapWidth = this.rc.getMapWidth();
		this.mapHeight = this.rc.getMapHeight();
		this.allEncampments = this.rc.senseAllEncampmentSquares();
		this.teamMemory = this.rc.getTeamMemory();
	}
	
	public void update(boolean forceUpdate){
		int currRound = Clock.getRoundNum();
		
		if(currRound != this.lastUpdate || forceUpdate){
			this.energon = this.rc.getEnergon();
			this.shields = this.rc.getShields();
			this.location = this.rc.getLocation();
			this.teamPower = this.rc.getTeamPower();
			this.nearbyRobots = this.rc.senseNearbyGameObjects(Robot.class);
			ArrayList<Robot> tempRobots = new ArrayList<Robot>();
			for(Robot r : this.nearbyRobots)
				if(r != null)
					tempRobots.add(r);
			this.nearbyRobots = tempRobots.toArray(new Robot[0]);
		}
		
		this.lastUpdate = currRound;
	}

	
	public RobotController getRc() {
		return rc;
	}

	public int getLastUpdate() {
		return lastUpdate;
	}

	public RobotType getType() {
		return type;
	}

	public Team getTeam() {
		return team;
	}

	public MapLocation getHQLoc() {
		return HQLoc;
	}

	public MapLocation getEnemyHQLoc() {
		return enemyHQLoc;
	}

	public int getMapWidth() {
		return mapWidth;
	}

	public int getMapHeight() {
		return mapHeight;
	}

	public MapLocation[] getAllEncampments() {
		return allEncampments;
	}

	public long[] getTeamMemory() {
		return teamMemory;
	}

	public double getEnergon() {
		if(this.lastUpdate != Clock.getRoundNum() || this.energon == -1)this.update(true);
		return energon;
	}

	public MapLocation getLocation() {
		if(this.lastUpdate != Clock.getRoundNum() || this.location == null)this.update(true);
		return location;
	}

	public double getShields() {
		if(this.lastUpdate != Clock.getRoundNum() || this.shields == -1)this.update(true);
		return shields;
	}

	public double getTeamPower() {
		if(this.lastUpdate != Clock.getRoundNum() || this.teamPower == -1)this.update(true);
		return teamPower;
	}

	public Robot[] getNearbyRobots() {
		if(this.lastUpdate != Clock.getRoundNum() || this.nearbyRobots == null)this.update(true);
		return nearbyRobots;
	}

	public MapLocation[] getAlliedEncampments() {
		return this.rc.senseAlliedEncampmentSquares();
	}

	public MapLocation[] getEnemyMines() {
		return this.rc.senseNonAlliedMineLocations(new MapLocation(mapWidth/2,mapHeight/2), 1000);
	}

	public MapLocation[] getMyMines() {
		return this.rc.senseMineLocations(new MapLocation(mapWidth/2,mapHeight/2), 1000, this.team);
	}

	
}
