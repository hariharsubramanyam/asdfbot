package jyoPlayer;

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
							closestDist = dist;;
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