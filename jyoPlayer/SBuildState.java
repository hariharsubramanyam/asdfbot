/**
 * State for soldier state machine
 * Behavior - go to closest enemy or rally point
 */
package jyoPlayer;


import battlecode.common.*;

public class SBuildState extends State{
	
	public MapLocation rallyPoint;

	// constructor
	public SBuildState(StateMachine rootSM){
		this.stateID = SMConstants.SBUILDSTATE;
		this.rootSM = rootSM;
		this.rc = rootSM.getRC();
	}

	// when we enter this state, we need to figure out where the rally point is
	@Override
	public void doEntryAct() {
		rallyPoint = findRallyPoint();
	}
	// no exit work
	@Override
	public void doExitAct() {}

	// see SAttackState's doAction - it's very similar. Here, we go to the closest enemy. If there's no nearby enemy, go to the rally point
	@Override
	public void doAction() {
		try{
			if(rc.isActive()){
				MapLocation myLocation = rc.getLocation();
				Robot[] alliedRobots = rc.senseNearbyGameObjects(Robot.class,100000,rc.getTeam());
				Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, 100000,rc.getTeam().opponent());
				Robot[] nearbyEnemyRobots = rc.senseNearbyGameObjects(Robot.class, 14,rc.getTeam().opponent());
				MapLocation[] encamp = rc.senseEncampmentSquares(rc.getLocation(), 10, Team.NEUTRAL);
				MapLocation[] myEncamp = rc.senseAlliedEncampmentSquares();
				MapLocation closestEncamp = null;
				for (MapLocation mL : encamp){
					int closestEncampDis = 100000;
					if(mL.distanceSquaredTo(myLocation)==0){
						rc.captureEncampment(RobotType.ARTILLERY);
						rallyPoint = new MapLocation((3*myLocation.x+rc.senseEnemyHQLocation().x)/4,(3*myLocation.y*rc.senseEnemyHQLocation().y)/4);
						break;
					}
					else{
						if(mL.distanceSquaredTo(myLocation) < closestEncampDis){
							closestEncamp = mL;
						}
					}
				}
/*				System.out.println("Nearby Encampments: "+ encamp.length);
*/				if(enemyRobots.length > 0){
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
				else if (encamp.length>0 && myEncamp.length < 3){
					if(closestEncamp !=null)
						goStraightToLocation(closestEncamp, myLocation);
				}
				else{
					if (goodPlace(myLocation)&&rc.senseMine(myLocation)==null && myLocation.distanceSquaredTo(rc.senseHQLocation())>4)
						rc.layMine();
					else
						freeGo(rallyPoint, alliedRobots, enemyRobots, nearbyEnemyRobots, myLocation);
					
				}
			}
		}
		catch(Exception e){
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
	private MapLocation findRallyPoint() {
		MapLocation enemyLoc = rc.senseEnemyHQLocation();
		MapLocation ourLoc = rc.senseHQLocation();
		int x = (enemyLoc.x + 3*ourLoc.x)/4;
		int y = (enemyLoc.y + 3*ourLoc.y)/4;
		return new MapLocation(x,y);
	}

	private boolean goodPlace(MapLocation location) {
//		return ((3*location.x+location.y)%8==0);//pickaxe with gaps
//		return ((2*location.x+location.y)%5==0);//pickaxe without gaps
		return ((location.x+location.y)%2==0);//checkerboard
	}
	//Movement system
	private void freeGo(MapLocation target, Robot[] allies,Robot[] enemies,Robot[] nearbyEnemies,MapLocation myLocation) throws GameActionException {
		//This robot will be attracted to the goal and repulsed from other things
		Direction toTarget = myLocation.directionTo(target);
		int targetWeighting = targetWeight(myLocation.distanceSquaredTo(target));
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
	private static int targetWeight(int dSquared){
		if (dSquared>100){
			return 5;
		}else if (dSquared>9){
			return 2;
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
				Team teamOfMine = null;
				Direction lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
				if(rc.canMove(lookingAtCurrently)){
					if((teamOfMine = (rc.senseMine(myLocation.add(lookingAtCurrently))))==null){
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