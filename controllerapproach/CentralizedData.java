package controllerapproach;

import java.util.ArrayList;
import java.util.SortedSet;

import battlecode.common.Clock;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Team;

public class CentralizedData {
	
	public static ArrayList<RobotData> robotData;
	public static Team team;
	public static long[] teamMemory;
	public static MapLocation hqLoc;
	public static MapLocation enemyhqLoc;
	public static int mapWidth;
	public static int mapHeight;
	public static double teamPower;
	public static MapLocation encampmentSquares;
	public static int lastUpdate;
	
	
	public CentralizedData(){
		this.robotData = new ArrayList<RobotData>();
		this.team = null;
		this.teamMemory = null;
		this.hqLoc = null;
		this.enemyhqLoc = null;
		this.mapWidth = -1;
		this.mapHeight = -1;
		this.teamPower = -1;
		this.encampmentSquares = null;
		this.lastUpdate = -1;
	}
	
	public void update(RobotController rc){
		int currRound = Clock.getRoundNum();
		if(currRound == this.lastUpdate){
			this.updateRobotData(rc);
		}
		else{
			this.updateRobotData(rc);
			if(this.team==null)
				this.team=rc.getTeam();
			if(this.teamMemory==null)
				this.teamMemory=rc.getTeamMemory();
			if(this.hqLoc==null)
				this.hqLoc=rc.senseHQLocation();
			if(this.enemyhqLoc==null)
				this.enemyhqLoc=rc.senseEnemyHQLocation();
			if(this.mapHeight==-1)
				this.mapHeight = rc.getMapHeight();
			if(this.mapWidth==-1)
				this.mapWidth = rc.getMapWidth();
			this.teamPower = rc.getTeamPower();
			this.lastUpdate = currRound;
		}
	}
	
	public void updateRobotData(RobotController rc){
		boolean found = false;
		for(RobotData r : this.robotData)
			if(r.id==rc.getRobot().getID()){
				r.active = rc.isActive();
				r.energon = rc.getEnergon();
				r.location = rc.getLocation();
				r.shields = rc.getShields();
				found = true;
			}
		if(!found)
			this.robotData.add(new RobotData(rc.getRobot().getID(),rc.getLocation(),rc.getEnergon(),rc.getShields(),rc.getType(),rc.isActive()));
	}

}
