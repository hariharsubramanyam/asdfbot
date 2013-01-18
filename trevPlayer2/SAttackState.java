package trevPlayer2;

import battlecode.common.*;

public class SAttackState extends State {

	public Direction movedFrom;

	public SAttackState(StateMachine rootSM){
		this.stateID = SMConstants.SATTACKSTATE;
		this.rootSM = rootSM;
		this.rc = rootSM.getRC();
	}



	@Override
	public void doEntryAct(){}

	@Override
	public void doExitAct(){}

	@Override
	public void doAction(){
		try{
			if(rc.isActive()){
				MapLocation enemyHQ = rc.senseEnemyHQLocation();
				MapLocation alliedHQ = rc.senseHQLocation();
				MapLocation myLocation = rc.getLocation();
				Robot[] alliedRobots = rc.senseNearbyGameObjects(Robot.class,100000,rc.getTeam());
				Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, 100000,rc.getTeam().opponent());
				Robot[] nearbyEnemyRobots = rc.senseNearbyGameObjects(Robot.class, PlayerConstants.NEARBY_ENEMY_DIST,rc.getTeam().opponent());
				Robot[] nearbyAlliedRobots = rc.senseNearbyGameObjects(Robot.class, PlayerConstants.NEARBY_ALLY_DIST,rc.getTeam());
				MapLocation[] myEncamp = rc.senseAlliedEncampmentSquares();
				if(nearbyEnemyRobots.length > 0){
					int closestDist = 10000000;
					MapLocation closestEnemy = null;
					MapLocation closestAlly = null;
					for(Robot r : enemyRobots){
						RobotInfo aRobotInfo = rc.senseRobotInfo(r);
						int dist = aRobotInfo.location.distanceSquaredTo(rc.getLocation());
						if(dist < closestDist){
							closestDist = dist;
							closestEnemy = aRobotInfo.location;
						}
					}
					closestDist = 10000000;
					for(Robot r : alliedRobots){
						RobotInfo aRobotInfo = rc.senseRobotInfo(r);
						int dist = aRobotInfo.location.distanceSquaredTo(rc.getLocation());
						if(dist < closestDist){
							closestDist = dist;;
							closestAlly = aRobotInfo.location;
						}
					}
					closestDist = 10000000;
					if(nearbyAlliedRobots.length >= nearbyEnemyRobots.length){
						goToLocation(closestEnemy);
					}
					else if(myLocation.distanceSquaredTo(alliedHQ) >= myLocation.distanceSquaredTo(enemyHQ)){
						MapLocation closestEncampment = null;
						for(MapLocation ml : myEncamp){
							int dist = ml.distanceSquaredTo(rc.getLocation());
							if(dist < closestDist){
								closestDist = dist;;
								closestEncampment = ml;
							}
						}
						goToLocation(closestEncampment);
						//goToLocation(getHQCenterOfMassBroadcast());
					}
					else{
						goToLocation(closestEnemy);
					}
				}
				else{
					goToLocation(enemyHQ);
				}
			}
		}catch(Exception e){e.printStackTrace();}
	}
	
	public MapLocation getHQCenterOfMassBroadcast(){
		try {
			String msg = "" + this.rc.readBroadcast(PlayerConstants.HQ_CENTER_OF_MASS_CHANNEL);
			return new MapLocation(Integer.parseInt(msg.substring(1, 5)),Integer.parseInt(msg.substring(6)));
		} catch (GameActionException e) {
			e.printStackTrace();
			return null;
		}
		
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
		goToLocation(myLocation.add(finalDir));
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

	private void goToLocation(MapLocation place)
			throws GameActionException {
		int dist = rc.getLocation().distanceSquaredTo(place);
		if(dist > 0){
			int[] directionOffsets = {0,1,-1,2,-2};
			Direction dir = rc.getLocation().directionTo(place);
			Direction firstMine = null;
			boolean hasMoved = false;
			for (int d: directionOffsets){
				Team teamOfMine = null;
				Direction lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
				if(rc.canMove(lookingAtCurrently)){
					if((teamOfMine = (rc.senseMine(rc.getLocation().add(lookingAtCurrently))))==null){
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
					rc.defuseMine(rc.getLocation().add(firstMine));
				}
/*				else if (place.distanceSquaredTo(rc.getLocation())>4){
					rc.layMine();
				}*/
			}
		}
	}

	private void flank(MapLocation closestEnemy, MapLocation closestAlly)
			throws GameActionException{
		Direction direnemy = rc.getLocation().directionTo(closestEnemy);
		Direction dirally = rc.getLocation().directionTo(closestAlly);
		int[] directionOffsets = {2,-2,1,-1,0};
		boolean hasmoved = false;
		for(int d : directionOffsets){
			Direction lookingAtCurrently = Direction.values()[(direnemy.ordinal()+d+8)%8];
			if(rc.canMove(lookingAtCurrently)){
				if(((rc.senseMine(rc.getLocation().add(lookingAtCurrently))))==null && lookingAtCurrently != dirally){
					rc.move(lookingAtCurrently);
					hasmoved = true;
				}
				else
					continue;
			}
			else
				continue;
		}
		if(!hasmoved){
			goToLocation(closestEnemy);
		}
	}

	private void retreat(MapLocation closestEnemy, MapLocation closestEncampment)
			throws GameActionException{
		Direction direnemy = rc.getLocation().directionTo(closestEnemy);
		int[] directionOffsets = {4,-3,3,-2,2};
		boolean hasmoved = false;
		for(int d : directionOffsets){
			Direction lookingAtCurrently = Direction.values()[(direnemy.ordinal()+d+8)%8];
			if(rc.canMove(lookingAtCurrently)){
				if(((rc.senseMine(rc.getLocation().add(lookingAtCurrently))))==null){
					rc.move(lookingAtCurrently);
					hasmoved = true;
				}
				else
					continue;
			}
			else
				continue;
		}
		if(!hasmoved){
			goToLocation(closestEncampment);
		}
	}
}