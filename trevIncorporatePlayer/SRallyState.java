/**
 * State for soldier state machine
 * Behavior - go to closest enemy or rally point
 */
package trevIncorporatePlayer;


import battlecode.common.*;

public class SRallyState extends State{

	// this is where our robots group together
	public MapLocation rallyPoint;

	// constructor
	public SRallyState(StateMachine rootSM){
		this.stateID = SMConstants.SRALLYSTATE;
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
				rc.setIndicatorString(0, "Rally State.");
				MapLocation myLocation = rc.getLocation();
				Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, 100000,rc.getTeam().opponent());

				if(enemyRobots.length > 0){
					MapLocation closestEnemy = closestRobot(enemyRobots);
					goToLocation(closestEnemy, myLocation);
				}
				else{
					rc.setIndicatorString(1, rallyPoint.toString());
					goToLocation(rallyPoint, myLocation);
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	private MapLocation findRallyPoint(){
/*		MapLocation[] myEncamp = rc.senseAlliedEncampmentSquares();
		MapLocation rallyPoint = null;
		if(myEncamp.length > 0){
			MapLocation enemyHQ = rc.senseEnemyHQLocation();
			int closestDist = 10000000;
			MapLocation closestEncampment = null;
			for(MapLocation ml : myEncamp){
				int dist = ml.distanceSquaredTo(enemyHQ);
				if(dist < closestDist){
					closestDist = dist;
					closestEncampment = ml;
				}
			}
			rallyPoint = new MapLocation((3*closestEncampment.x+enemyHQ.x)/4,(3*closestEncampment.y+enemyHQ.y)/4);
		}
		else if(rallyPoint == null)*/
		MapLocation alliedHQ = rc.senseHQLocation();
		MapLocation enemyHQ = rc.senseEnemyHQLocation();
		MapLocation rallyPt = new MapLocation((int)(alliedHQ.x*.9+enemyHQ.x*.1),(int)(alliedHQ.y*.9+enemyHQ.y*.1));
/*		MapLocation rallyPoint = alliedHQ.add(alliedHQ.directionTo(enemyHQ),(int)(0.8*alliedHQ.distanceSquaredTo(enemyHQ)));*/
//		rallyPoint = new MapLocation((int)(0.7*alliedHQ.x+0.3*enemyHQ.x),(int)(0.7*alliedHQ.y+0.3*enemyHQ.y));
		rc.setIndicatorString(2, rallyPt.toString());
		return rallyPt;
	}

/*	private boolean goodPlace(MapLocation location) {
//		return ((3*location.x+location.y)%8==0);//pickaxe with gaps
		return ((2*location.x+location.y)%5==0);//pickaxe without gaps
		return ((location.x+location.y)%2==0);//checkerboard
	}*/
	// rally point is a weighted average of our HQ position and opponent HQ position
/*	private MapLocation findRallyPoint() {
		MapLocation enemyLoc = rc.senseEnemyHQLocation();
		MapLocation ourLoc = rc.senseHQLocation();
		int x = (enemyLoc.x + 2*ourLoc.x)/3;
		int y = (enemyLoc.y + 2*ourLoc.y)/3;
		return new MapLocation(x,y);
	}*/

	public MapLocation closestRobot(Robot[] Robots) throws GameActionException{
		int closestDist = 10000000;
		MapLocation closestRobot = null;
		for(int i = 0; i < Robots.length; i++){
			Robot aRobot = Robots[i];
			RobotInfo aRobotInfo = rc.senseRobotInfo(aRobot);
			int dist = aRobotInfo.location.distanceSquaredTo(rc.getLocation());
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
}