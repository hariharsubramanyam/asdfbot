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
				Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,100000,rc.getTeam().opponent());
				if(enemyRobots.length > 0){
					int closestDist = 10000000;
					MapLocation closestEnemy = null;
					for(int i = 0; i < enemyRobots.length; i++){
						Robot aRobot = enemyRobots[i];
						RobotInfo aRobotInfo = rc.senseRobotInfo(aRobot);
						int dist = aRobotInfo.location.distanceSquaredTo(rc.getLocation());
						if(dist < closestDist){
							closestDist = dist;;
							closestEnemy = aRobotInfo.location;
						}
					goToLocation(closestEnemy);
					}
				}
				else{
					goToLocation(rc.senseEnemyHQLocation());
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
				else if (place.distanceSquaredTo(rc.getLocation())>4){
					rc.layMine();
				}
			}
		}
	}
}
