/**
 * State for soldier state machine
 * Behavior - go to closest enemy or rally point
 */
package attackStatePlayer;

/*
 * If I'm the closest to an encampment:
 * 	Go to the encampment
 * else:
 * 	Free-go and plant mine
 */

import battlecode.common.*;

public class SBuildState extends State{
	
	public boolean findingEncampment;
	public MapLocation rallyPoint;
	
	public MapLocation goalEncampment;
	public RobotType encampmentType;
	
	public int turnsWithoutMineLaying;
	// constructor
	public SBuildState(StateMachine rootSM){
		this.stateID = SMConstants.SBUILDSTATE;
		this.goalEncampment = null;
		this.rootSM = rootSM;
		this.rc = rootSM.getRC();
	}

	// Find rally point
	@Override
	public void doEntryAct() {
		this.rallyPoint = findRallyPoint();

		this.encampmentType = ((rc.getRobot().getID()/4)%3==0) ? RobotType.GENERATOR : RobotType.SUPPLIER;
		this.findingEncampment = rc.getRobot().getID()%4 == 0;
	}
	
	// no exit work
	@Override
	public void doExitAct() {}

	// see SAttackState's doAction -  it's very similar. Here, we go to the closest enemy. If there's no nearby enemy, go to the rally point
	@Override
	public void doAction() {
		try{
			if(rc.isActive()){
				if(this.findingEncampment){
					if(this.goalEncampment == null){
						MapLocation[] nearbyLocs = this.rc.senseEncampmentSquares(rc.senseHQLocation(), 225, Team.NEUTRAL);
						if(nearbyLocs != null && nearbyLocs.length > 0)
							this.goalEncampment = nearbyLocs[(this.rc.getRobot().getID()/2)%nearbyLocs.length];
						if(this.goalEncampment == null || this.goalEncampment.equals(rc.senseHQLocation().add(Direction.SOUTH))){
							this.goalEncampment = null;
							this.findingEncampment = false;
							return;
						}
					}
					if(this.rc.getLocation().equals(this.goalEncampment)){
						this.rc.captureEncampment(this.encampmentType);
					}
					else if(this.rc.getLocation().isAdjacentTo(this.goalEncampment) && !this.rc.canMove(this.rc.getLocation().directionTo(this.goalEncampment)))
						this.rootSM.goToState(SMConstants.SATTACKSTATE);
					else
						this.goToLocation(this.goalEncampment, this.rc.getLocation());
				}
				else{
					if(this.turnsWithoutMineLaying > 10)
						this.rootSM.goToState(SMConstants.SATTACKSTATE);
					if (goodPlace(rc.getLocation())&&rc.senseMine(rc.getLocation())==null && rc.getLocation().distanceSquaredTo(rc.senseHQLocation())>4){
						rc.layMine();
						this.turnsWithoutMineLaying = 0;
					}
					else{
						freeGo(rallyPoint, rc.senseNearbyGameObjects(Robot.class, 100000, rc.getTeam()), rc.senseNearbyGameObjects(Robot.class, 100000, rc.getTeam().opponent()), rc.senseNearbyGameObjects(Robot.class, 25, rc.getTeam().opponent()), rc.getLocation(), rc.senseEnemyHQLocation(), rc.senseHQLocation());					
						this.turnsWithoutMineLaying++;
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
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
		int d2 = location.distanceSquaredTo(rc.senseHQLocation());
		return (d2>1 && d2<=100);
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
			}
		}
	}

}