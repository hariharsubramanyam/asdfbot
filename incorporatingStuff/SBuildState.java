/**
 * State for soldier state machine
 * Behavior - go to closest enemy or rally point
 */
package incorporatingStuff;


import java.util.ArrayList;

import nukebot.SMConstants;

import battlecode.common.*;

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
	public int encampType;

	// constructor
	public SBuildState(StateMachine rootSM){
		this.stateID = SMConstants.SBUILDSTATE;
		this.rootSM = rootSM;
		this.rc = rootSM.getRC();
	}

	public MapLocation getTarget(){
		try {
			int msgInt = this.rc.readBroadcast(PlayerConstants.ENCAMPMENT_LOCATION_CHANNEL);
			return PlayerConstants.intToMapLocation(msgInt);
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

	// when we enter this state, we need to figure out where the rally point is
	@Override
	public void doEntryAct() {
		try {
			if (rc.readBroadcast(PlayerConstants.ENCAMPMENT_LOCATION_CHANNEL) > 999999){
				String broadcast = rc.readBroadcast(PlayerConstants.ENCAMPMENT_LOCATION_CHANNEL)+"";
				target = new MapLocation(Integer.parseInt(broadcast.substring(4, 7)),Integer.parseInt(broadcast.substring(1, 4)));
				encampType = Integer.parseInt(broadcast.substring(0,1));
			}
		} catch (GameActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (target == null)
			this.rootSM.goToState(SMConstants.SWAITSTATE);
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
				int channel = 13*target.x*target.x+5*target.y+3+PlayerConstants.BEING_TAKEN_CHANNEL;
				this.rc.broadcast(channel, 19541235);

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
					this.rc.broadcast(channel, 19541275);
					if(encampType == 2)
						rc.captureEncampment(RobotType.ARTILLERY);
					else if(encampType == 3)
						rc.captureEncampment(RobotType.GENERATOR);
					else if(encampType == 4)
						rc.captureEncampment(RobotType.SUPPLIER);
					else if(encampType == 9)
						rc.captureEncampment(RobotType.GENERATOR);
				}
				
				else if(this.rc.getLocation().isAdjacentTo(this.target) && !this.rc.canMove(this.rc.getLocation().directionTo(this.target)))
					this.rootSM.goToState(SMConstants.SATTACKSTATE);

				else{
					goStraightToLocation(target,myLocation);
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
					Team teamOfMine = rc.senseMine(myLocation.add(lookingAtCurrently));
					if(teamOfMine == null || teamOfMine == rc.getTeam()){
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