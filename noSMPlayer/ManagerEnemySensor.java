package noSMPlayer;

import java.util.ArrayList;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;

public class ManagerEnemySensor {
	private ManagerData dataMan;
	private RobotController rc;
	
	public ManagerEnemySensor(ManagerData dataMan, RobotController rc){
		this.dataMan = dataMan;
		this.rc = rc;
	}
	
	public MapLocation getClosestEnemy(int distanceThreshold){
			Robot[] enemies = this.getNearbyEnemies();
			MapLocation nearestEnemyLoc = null;
			int closestDist = Integer.MAX_VALUE-1;
			distanceThreshold *= distanceThreshold;
			MapLocation enemyLoc;
			int dist;
			for(Robot e : enemies)
				try {
					enemyLoc = rc.senseRobotInfo(e).location;
					dist = enemyLoc.distanceSquaredTo(dataMan.getLocation());
					if(dist < closestDist){
						closestDist = dist;
						nearestEnemyLoc = enemyLoc;
					}
				} catch (GameActionException e1) {e1.printStackTrace();}
			
			if(closestDist > distanceThreshold)
				return null;
				
			return nearestEnemyLoc;
	}
	
	public Robot[] getNearbyEnemies(){
		ArrayList<Robot> enemies = new ArrayList<Robot>();
		try{
		for(Robot r : dataMan.getNearbyRobots()){
			if(r == null)
				continue;
			if(r.getTeam() == dataMan.getTeam().opponent())
				enemies.add(r);
		}}
		catch(Exception ex){
			System.out.println("FAILED HERE AT ManagerEnemySensor");
		}
		return enemies.toArray(new Robot[0]);
	}
}
