/**
 * State for soldier state machine
 * Behavior - go to closest enemy or rally point
 */
package incorporatingStuff;


import java.util.ArrayList;

import battlecode.common.*;
import battlecode.engine.instrumenter.lang.System;

public class SBuildState extends State{

	public MapLocation rallyPoint;
	public MapLocation enemyHQ;
	public MapLocation alliedHQ;
	public MapLocation myLocation;
	public Robot[] alliedRobots;
	public Robot[] enemyRobots;
	public Robot[] nearbyEnemyRobots;

	//public MapLocation[] closestEncamps;
	public ArrayList<MapLocation> closestEncamps;
	public MapLocation target;
	
	// constructor
	public SBuildState(StateMachine rootSM){
		this.stateID = SMConstants.SBUILDSTATE;
		this.rootSM = rootSM;
		this.rc = rootSM.getRC();
	}
	
	public MapLocation getTarget(){
		try {
			int msgInt = this.rc.readBroadcast(PlayerConstants.ENCAMPMENT_LOCATION_CHANNEL);
			String msg = "" + msgInt;
			MapLocation square = new MapLocation(Integer.parseInt(msg.substring(5)),Integer.parseInt(msg.substring(1,4)));
			rc.setIndicatorString(0, square.toString());
			return square;
		} catch (GameActionException e) {
			e.printStackTrace();
			return null;
		}
/*		try {
			String msg = "" + this.rc.readBroadcast(PlayerConstants.ENCAMPMENT_LOCATION_CHANNEL);
			return new MapLocation(Integer.parseInt(msg.substring(1, 4)),Integer.parseInt(msg.substring(5)));
		} catch (GameActionException e) {
			e.printStackTrace();
			return null;
		}*/
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
/*		this.rc.setIndicatorString(0, msg);
*/		try {
			int channel = 2*loc.x+loc.y+PlayerConstants.BEING_TAKEN_CHANNEL;
			this.rc.broadcast(channel, Integer.parseInt(msg));
		} catch (GameActionException e) {e.printStackTrace();}
	}
	
	// when we enter this state, we need to figure out where the rally point is
	@Override
	public void doEntryAct() {
		target = getTarget();
	}
	// no exit work
	@Override
	public void doExitAct() {}

	// see SAttackState's doAction -  it's very similar. Here, we go to the closest enemy. If there's no nearby enemy, go to the rally point
	@Override
	public void doAction() {
		try{
			if(rc.isActive()){
/*				if(enemyHQ == null)
					enemyHQ = rc.senseEnemyHQLocation();
				if(alliedHQ == null)
					alliedHQ = rc.senseHQLocation();*/
				myLocation = rc.getLocation();
				alliedRobots = rc.senseNearbyGameObjects(Robot.class,100000,rc.getTeam());
				enemyRobots = rc.senseNearbyGameObjects(Robot.class, 100000,rc.getTeam().opponent());
				nearbyEnemyRobots = rc.senseNearbyGameObjects(Robot.class,14,rc.getTeam().opponent());
				rc.setIndicatorString(0, "Build State. " + target.toString());
				sendEncampmentLocation(target);
			
				if(nearbyEnemyRobots.length > 0){
					int closestDistance = 10000000;
					MapLocation closestEnemy = null;
					for(int i = 0; i < enemyRobots.length; i++){
						Robot aRobot = enemyRobots[i];
						RobotInfo aRobotInfo = rc.senseRobotInfo(aRobot);
						int dist = aRobotInfo.location.distanceSquaredTo(rc.getLocation());
						if(dist < closestDistance){
							closestDistance = dist;
							closestEnemy = aRobotInfo.location;
						}
					}
					goToLocation(closestEnemy, myLocation);
				}
				
				else if(target.equals(myLocation)){
					rc.captureEncampment(RobotType.ARTILLERY);
				}

				else{
					goToLocation(target,myLocation);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public MapLocation closestRobot(Robot[] Robots, MapLocation myLocation) throws GameActionException{
		int closestDist = 10000000;
		MapLocation closestRobot = null;
		for(int i = 0; i < Robots.length; i++){
			Robot aRobot = Robots[i];
			RobotInfo aRobotInfo = rc.senseRobotInfo(aRobot);
			int dist = aRobotInfo.location.distanceSquaredTo(myLocation);
			if(dist < closestDist){
				closestDist = dist;
				closestRobot = aRobotInfo.location;
			}
		}
		return closestRobot;
	}
	// rally point is a weighted average of our HQ position and opponent HQ position
	private MapLocation findRallyPoint(){
		MapLocation[] myEncamp = rc.senseAlliedEncampmentSquares();
		MapLocation rallyPoint = null;
		if(myEncamp.length > 0){
			MapLocation enemyHQ = rc.senseEnemyHQLocation();
			int closestDist = 10000000;
			MapLocation closestEncampment = null;
			for(MapLocation ml : myEncamp){
				int dist = ml.distanceSquaredTo(rc.getLocation());
				if(dist < closestDist){
					closestDist = dist;
					closestEncampment = ml;
				}
			}
			rallyPoint = new MapLocation((3*closestEncampment.x+enemyHQ.x)/4,(3*closestEncampment.y+enemyHQ.y)/4);
		}
		else if(rallyPoint == null)
			rallyPoint = new MapLocation((3*rc.senseHQLocation().x+rc.senseEnemyHQLocation().x)/4,(3*rc.senseHQLocation().y+rc.senseEnemyHQLocation().y)/4);
		return rallyPoint;
	}

	private boolean goodPlace(MapLocation location) {
//		return ((3*location.x+location.y)%8==0);//pickaxe with gaps
//		return ((2*location.x+location.y)%5==0);//pickaxe without gaps
//		return ((location.x+location.y)%2==0);//checkerboard
		int d2 = location.distanceSquaredTo(alliedHQ);
		return (d2>1 && d2<=64);
	}
	//Movement system
	private void freeGo(MapLocation target, Robot[] allies,Robot[] enemies,Robot[] nearbyEnemies,MapLocation myLocation, MapLocation enemyHQ, MapLocation alliedHQ) throws GameActionException {
		//This robot will be attracted to the goal and repulsed from other things
		Direction toTarget = myLocation.directionTo(target);
		int targetWeighting = targetWeight(myLocation.distanceSquaredTo(target), enemyHQ, alliedHQ);
		MapLocation goalLoc = myLocation.add(toTarget,targetWeighting);//toward target, TODO weighted by the distance?

		if (enemies.length==0){
			//find closest allied robot. repel away from that robot.
			if(allies.length>0){
				MapLocation closestAlly = findClosest(allies);
				goalLoc = goalLoc.add(myLocation.directionTo(closestAlly),-3);
			}
		}else if (allies.length<nearbyEnemies.length+3){
			if(allies.length>0){//find closest allied robot. attract to that robot.
				MapLocation closestAlly = findClosest(allies);
				goalLoc = goalLoc.add(myLocation.directionTo(closestAlly),5);
			}
			if(nearbyEnemies.length>0){//avoid enemy
				MapLocation closestEnemy = findClosest(nearbyEnemies);
				goalLoc = goalLoc.add(myLocation.directionTo(closestEnemy),-10);
			}
		}else if (allies.length>=nearbyEnemies.length+3){
			if(allies.length>0){
				MapLocation closestAlly = findClosest(allies);
				goalLoc = goalLoc.add(myLocation.directionTo(closestAlly),5);
			}
			if(nearbyEnemies.length>0){
				MapLocation closestEnemy = findClosest(nearbyEnemies);
				goalLoc = goalLoc.add(myLocation.directionTo(closestEnemy),10);
			}else{// no nearby enemies; go toward far enemy
				MapLocation closestEnemy = findClosest(enemies);
				goalLoc = goalLoc.add(myLocation.directionTo(closestEnemy),10);
			}
		}
		//TODO repel from allied mines?
		//now use that direction
		Direction finalDir = myLocation.directionTo(goalLoc);
		if (Math.random()<.1)
			finalDir = finalDir.rotateRight();
		goToLocation(myLocation.add(finalDir), myLocation);
	}
	
	private static int targetWeight(int dSquared, MapLocation enemyHQ, MapLocation alliedHQ){
		int HQseparation = enemyHQ.distanceSquaredTo(alliedHQ);
		if (dSquared>100){
			if(Clock.getRoundNum()<1000){
				if (HQseparation>900)
					return 5;
				else if (HQseparation>400)
					return 10;
				else
					return 15;
			}
			else
				return 15;
		}else if (dSquared>9){
			if(Clock.getRoundNum()<1000){
				if (HQseparation>900)
					return 2;
				else if (HQseparation>400)
					return 4;
				else
					return 6;
			}
			else
				return 6;
		}else{
			return 1;
		}
	}

	private MapLocation findClosest(Robot[] enemyRobots) throws GameActionException {
		int closestDist = 1000000;
		MapLocation closestEnemy=null;
		for (int i=0;i<enemyRobots.length;i++){
			Robot arobot = enemyRobots[i];
			RobotInfo arobotInfo = rc.senseRobotInfo(arobot);
			int dist = arobotInfo.location.distanceSquaredTo(rc.getLocation());
			if (dist<closestDist){
				closestDist = dist;
				closestEnemy = arobotInfo.location;
			}
		}
		return closestEnemy;
	}	
	// see SAttackState's goToLocation method - it is identical
	public Direction movedFrom = null;

	private void goToLocation(MapLocation place, MapLocation myLocation)
			throws GameActionException {
		int dist = myLocation.distanceSquaredTo(place);
		if(dist > 0){
			int[] directionOffsets = {0,1,-1,2,-2};
			Direction dir = myLocation.directionTo(place);
			Direction firstMine = null;
			boolean hasMoved = false;
			for (int d: directionOffsets){
				Direction lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
				if(rc.canMove(lookingAtCurrently)){
					Team teamOfMine = rc.senseMine(myLocation.add(lookingAtCurrently));
					if(teamOfMine == null || teamOfMine == rc.getTeam()){
						if (this.movedFrom != lookingAtCurrently.opposite()){
							this.movedFrom = lookingAtCurrently;
							rc.move(lookingAtCurrently);
							hasMoved = true;
							break;
						}
						else{
							continue;
						}
					}

					else if(firstMine == null && teamOfMine!=rc.getTeam()){
						firstMine = Direction.values()[lookingAtCurrently.ordinal()];
					}
				}
			}
			if(!hasMoved){
				if(firstMine != null){
					rc.defuseMine(myLocation.add(firstMine));
				}
/*				else if (place.distanceSquaredTo(myLocation)>4){
					rc.layMine();
				}*/
			}
		}
	}

	private void goStraightToLocation(MapLocation place, MapLocation myLocation)
			throws GameActionException {
		int dist = myLocation.distanceSquaredTo(place);
		if(dist > 0){
			int[] directionOffsets = {0,1,-1};
			Direction dir = myLocation.directionTo(place);
/*			boolean hasMoved = false;
*/			for (int d: directionOffsets){
				Direction lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
				if(rc.canMove(lookingAtCurrently)){
					if((rc.senseMine(myLocation.add(lookingAtCurrently)))==null){
						if (this.movedFrom != lookingAtCurrently.opposite()){
							this.movedFrom = lookingAtCurrently;
							rc.move(lookingAtCurrently);
/*							hasMoved = true;
*/							break;
						}
						else{
							continue;
						}
					}
					else{
						if (this.movedFrom != lookingAtCurrently.opposite()){
							rc.defuseMine(myLocation.add(lookingAtCurrently));
						}
						else{
							continue;
						}
					}
				}
			}
/*			if(!hasMoved){
					rc.layMine();
			}*/
		}
	}

}