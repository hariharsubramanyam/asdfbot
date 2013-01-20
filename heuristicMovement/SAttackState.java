package heuristicMovement;

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
/*				MapLocation myLocation = rc.getLocation();
*/				MapLocation enemyHQ = rc.senseEnemyHQLocation();
				MapLocation alliedHQ = rc.senseHQLocation();
				Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,100000,rc.getTeam().opponent());
				Robot[] alliedRobots = rc.senseNearbyGameObjects(Robot.class, 100000,rc.getTeam());
				Robot[] nearbyEnemyRobots = rc.senseNearbyGameObjects(Robot.class,14,rc.getTeam().opponent());
				Robot[] nearbyAlliedRobots = rc.senseNearbyGameObjects(Robot.class, 14,rc.getTeam());

				MapLocation[] myEncamp = rc.senseAlliedEncampmentSquares();
				if(enemyRobots.length > 0){
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
					else if(rc.getLocation().distanceSquaredTo(alliedHQ) >= rc.getLocation().distanceSquaredTo(enemyHQ)){
						goToLocation(enemyHQ);
					}
					else{
						if(myEncamp.length > 0){
							int closestDistance = 10000000;
							MapLocation closestEncampment = null;
							for(MapLocation ml : myEncamp){
								int dist = ml.distanceSquaredTo(rc.getLocation());
								if(dist < closestDistance){
									closestDistance = dist;
									closestEncampment = ml;
								}
							}
							goStraightToLocation(closestEnemy, closestEncampment);
						}
					}
				}
				else{
					goToLocation(enemyHQ);
				}
			}
		}catch(Exception e){e.printStackTrace();}
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
			}
		}
	}
	
	private void heuristicGoToLocation(MapLocation place, MapLocation myLocation)
			throws GameActionException {
		int dist = myLocation.distanceSquaredTo(place);
		if(dist > 0){
			int[] directionOffsets = {0,1,-1,2,-2,3,-3,4};
			Direction dir = myLocation.directionTo(place);
			boolean hasMoved = false;
			int weightedDistanceCovered = -100000000;
			Direction bestDir = null;
			Direction defuseDir = null;
			for (int d: directionOffsets){
				Direction lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
				if(rc.canMove(lookingAtCurrently)){
					if((rc.senseMine(myLocation.add(lookingAtCurrently)))==null){
						if (this.movedFrom != lookingAtCurrently.opposite()){
							int distanceCovered = 2*(myLocation.distanceSquaredTo(place) - myLocation.add(lookingAtCurrently).distanceSquaredTo(place));
							if (distanceCovered > weightedDistanceCovered){
								weightedDistanceCovered = distanceCovered;
								bestDir = lookingAtCurrently;
								defuseDir = Direction.values()[lookingAtCurrently.ordinal()];								
							}
						}
						else
							continue;
					}
				}
					else{
						if (this.movedFrom != lookingAtCurrently.opposite()){
							int distanceCovered = myLocation.distanceSquaredTo(place) - myLocation.add(lookingAtCurrently).distanceSquaredTo(place);
							if (distanceCovered > weightedDistanceCovered){
								weightedDistanceCovered = distanceCovered;
								bestDir = lookingAtCurrently;
								defuseDir = Direction.values()[lookingAtCurrently.ordinal()];
							}
						}
						else
							continue;
					}
				}
			
			if (bestDir != null){
				if(rc.senseMine(myLocation.add(bestDir)) == Team.NEUTRAL){
					rc.defuseMine(myLocation.add(defuseDir));
				}
				else{
					rc.move(bestDir);
					hasMoved = true;
					this.movedFrom = bestDir;
				}
			}
			else if (!hasMoved)
				goStraightToLocation(myLocation.add(bestDir), myLocation);
			else
				goToLocation(place);
		}
	}
	
	private void goStraightToLocation(MapLocation place, MapLocation myLocation)
			throws GameActionException {
		int dist = myLocation.distanceSquaredTo(place);
		if(dist > 0){
			int[] directionOffsets = {0,1,-1};
			Direction dir = myLocation.directionTo(place);
			for (int d: directionOffsets){
				Direction lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
				if(rc.canMove(lookingAtCurrently)){
					Team teamOfMine = rc.senseMine(myLocation.add(lookingAtCurrently));
					if(teamOfMine == null || teamOfMine == rc.getTeam()){
						if (this.movedFrom != lookingAtCurrently.opposite()){
							this.movedFrom = lookingAtCurrently;
							rc.move(lookingAtCurrently);
							break;
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
		}
	}
	
}