/**
 * State for soldier state machine
 * Behavior - go to closest enemy or rally point
 */
package trevIncorporatePlayer;


import battlecode.common.*;

public class SWaitState extends State{
	
	public MapLocation rallyPoint;
	public MapLocation enemyHQ;
	public MapLocation alliedHQ;
	public MapLocation myLocation;
	public Robot[] alliedRobots;
	public Robot[] enemyRobots;
	public Robot[] nearbyEnemyRobots;
	public MapLocation[] encamp;

	// constructor
	public SWaitState(StateMachine rootSM){
		this.stateID = SMConstants.SWAITSTATE;
		this.rootSM = rootSM;
		this.rc = rootSM.getRC();
	}

	// when we enter this state, we need to figure out where the rally point is
	@Override
	public void doEntryAct() {}

	// no exit work
	@Override
	public void doExitAct() {}

	// see SAttackState's doAction - it's very similar. Here, we go to the closest enemy. If there's no nearby enemy, go to the rally point
	@Override
	public void doAction() {
		try{
			if(rc.isActive()){
				
				if(enemyHQ == null)
					enemyHQ = rc.senseEnemyHQLocation();
				if(alliedHQ == null)
					alliedHQ = rc.senseHQLocation();
				myLocation = rc.getLocation();
				alliedRobots = rc.senseNearbyGameObjects(Robot.class,100000,rc.getTeam());
				enemyRobots = rc.senseNearbyGameObjects(Robot.class, 100000,rc.getTeam().opponent());
				nearbyEnemyRobots = rc.senseNearbyGameObjects(Robot.class,14,rc.getTeam().opponent());
				encamp = rc.senseEncampmentSquares(rc.getLocation(), 100000, Team.NEUTRAL);
				rc.setIndicatorString(0, "Wait state.");
				
				MapLocation myLocation = rc.getLocation();
				Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, 14,rc.getTeam().opponent());

				if(enemyRobots.length > 0){
					MapLocation closestEnemy = findClosest(enemyRobots);
					MapLocation closestAlly = findClosest(alliedRobots);
					surround(closestEnemy, closestAlly, myLocation);
				}
				
				else{
					if (goodPlace(myLocation, alliedHQ, enemyHQ, encamp)&&rc.senseMine(myLocation)==null && myLocation.distanceSquaredTo(rc.senseHQLocation())>4)
						rc.layMine();
					else
						findPlacesToLayMines(alliedHQ, alliedRobots, enemyRobots, nearbyEnemyRobots, myLocation, enemyHQ);
					
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	private void surround(MapLocation closestEnemy, MapLocation closestAlly, MapLocation myLocation) throws GameActionException {
		MapLocation goalLoc = myLocation;
		
		if (myLocation.distanceSquaredTo(closestEnemy) > 25){
			goalLoc = goalLoc.add(myLocation.directionTo(closestAlly),-1);
			goalLoc = goalLoc.add(myLocation.directionTo(closestEnemy));
		}
		else if (myLocation.distanceSquaredTo(closestEnemy) > 9){
			goalLoc = goalLoc.add(myLocation.directionTo(closestAlly));
			goalLoc = goalLoc.add(myLocation.directionTo(closestEnemy));
		}
		else{
			goalLoc = goalLoc.add(myLocation.directionTo(closestEnemy));
			goalLoc = goalLoc.add(myLocation.directionTo(closestAlly));
		}
		Direction finalDir = myLocation.directionTo(goalLoc);
		goToLocation(myLocation.add(finalDir), myLocation);
	}
	
	private boolean goodPlace(MapLocation location, MapLocation alliedHQ, MapLocation enemyHQ, MapLocation [] encamp) {
//		return ((3*location.x+location.y)%8==0);//pickaxe with gaps
//		return ((2*location.x+location.y)%5==0);//pickaxe without gaps
//		return ((location.x+location.y)%2==0);//checkerboard
		int d2 = location.distanceSquaredTo(alliedHQ);
		boolean isEncamp = false;
		for (MapLocation mL : encamp){
			if(mL.equals(myLocation)){
				isEncamp = true;
				break;
			}
		}
		return (d2>4 && d2<Math.min((int)(0.04*alliedHQ.distanceSquaredTo(enemyHQ)),192) && !isEncamp);
	}
	
	//Movement system
	private void findPlacesToLayMines(MapLocation alliedHQ, Robot[] allies,Robot[] enemies,Robot[] nearbyEnemies,MapLocation myLocation, MapLocation enemyHQ) throws GameActionException {
		//This robot will be attracted to the goal and repulsed from other things
		Direction toTarget = myLocation.directionTo(alliedHQ).opposite();
		int targetWeighting = targetWeight(myLocation.distanceSquaredTo(alliedHQ), enemyHQ, alliedHQ);
		MapLocation goalLoc = myLocation.add(toTarget,targetWeighting);//toward target, TODO weighted by the distance?
		goalLoc = goalLoc.add(myLocation.directionTo(enemyHQ));
/*		goalLoc = goalLoc.add(myLocation.directionTo(enemyHQ),3);
*/		MapLocation closestAlly = findClosest(allies);
		goalLoc = goalLoc.add(myLocation.directionTo(closestAlly), -8);
		//TODO repel from allied mines?
		//now use that direction
		Direction finalDir = myLocation.directionTo(goalLoc);
/*		if (Math.random()<.1)
			finalDir = finalDir.rotateRight();*/
		goToLocation(myLocation.add(finalDir), myLocation);
	}
	
	private static int targetWeight(int dSquared, MapLocation enemyHQ, MapLocation alliedHQ){
		return 5;
/*		int HQseparation = enemyHQ.distanceSquaredTo(alliedHQ);*/
/*		if (dSquared>100){
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
		}*/
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

}