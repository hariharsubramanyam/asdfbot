package jyoPlayer;

import battlecode.common.*;

public class SWaitState extends State{
	
	MapLocation rallyPoint;
	
	public SWaitState(StateMachine rootSM){
		this.stateID = SMConstants.SWAITSTATE;
		this.rootSM = rootSM;
		this.rc = rootSM.getRC();
	}

	@Override
	public void doEntryAct() {
		rallyPoint = findRallyPoint();
	}

	@Override
	public void doExitAct() {}

	@Override
	public void doAction() {
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
					}
					goToLocation(closestEnemy);
				}

				else{
					goToLocation(rallyPoint);
				}
			}
			rc.yield();
		}
		catch(Exception e){
			e.printStackTrace();
		}
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
				Direction lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
				if(rc.canMove(lookingAtCurrently)){
					if(rc.senseMine(rc.getLocation().add(lookingAtCurrently))==null){
						rc.move(lookingAtCurrently);
						hasMoved = true;
						break;
					}
					else if(firstMine == null){
						firstMine = Direction.values()[lookingAtCurrently.ordinal()];
					}
				}
			}
			if(!hasMoved){
				if(firstMine != null){
					rc.defuseMine(rc.getLocation().add(firstMine));
				}
				else{
					rc.move(dir.opposite());
				}
			}
		}
	}

	private MapLocation findRallyPoint() {
		MapLocation enemyLoc = rc.senseEnemyHQLocation();
		MapLocation ourLoc = rc.senseHQLocation();
		int x = (enemyLoc.x + 3*ourLoc.x)/4;
		int y = (enemyLoc.y + 3*ourLoc.y)/4;
		return new MapLocation(x,y);
	}


	
	
}
