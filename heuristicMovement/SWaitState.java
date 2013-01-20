/**
 * State for soldier state machine
 * Behavior - go to closest enemy or rally point
 */
package heuristicMovement;


import battlecode.common.*;

public class SWaitState extends State{
	
	public Direction movedFrom;

	// this is where our robots group together
	public MapLocation rallyPoint;

	// constructor
	public SWaitState(StateMachine rootSM){
		this.stateID = SMConstants.SWAITSTATE;
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
				Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, 100000,rc.getTeam().opponent());

				if(enemyRobots.length > 0){
					MapLocation closestEnemy = rallyPoint;
					heuristicGoToLocation(rc.senseEnemyHQLocation(), myLocation);
				}
				else{
					heuristicGoToLocation(rc.senseEnemyHQLocation(), myLocation);
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
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
			rallyPoint = new MapLocation((rc.senseHQLocation().x+2*rc.senseEnemyHQLocation().x)/3,(rc.senseHQLocation().y+2*rc.senseEnemyHQLocation().y)/3);
		return rallyPoint;
	}

	private void heuristicGoToLocation(MapLocation place, MapLocation myLocation)
			throws GameActionException {
		int dist = myLocation.distanceSquaredTo(place);
		if(dist > 0){
			int[] directionOffsets = {0,1,-1,2,-2,3,-3,4};
			Direction dir = myLocation.directionTo(place);
			int weightedDistanceCovered = -100000000;
			Direction bestDir = null;
			Direction defuseDir = null;
			for (int d: directionOffsets){
				Direction lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
				if(rc.canMove(lookingAtCurrently)){
					Team teamOfMine = rc.senseMine(myLocation.add(lookingAtCurrently));
					if(teamOfMine == null || teamOfMine == rc.getTeam()){
						if (this.movedFrom != lookingAtCurrently.opposite()){
							int distanceCovered = 5*(myLocation.distanceSquaredTo(place) - myLocation.add(lookingAtCurrently).distanceSquaredTo(place) + 2);
							if (distanceCovered > weightedDistanceCovered){
								weightedDistanceCovered = distanceCovered;
								bestDir = lookingAtCurrently;
								defuseDir = Direction.values()[lookingAtCurrently.ordinal()];								
							}
						}
						else
							continue;
					}
					else{
						if (this.movedFrom != lookingAtCurrently.opposite()){
							int distanceCovered = myLocation.distanceSquaredTo(place) - myLocation.add(lookingAtCurrently).distanceSquaredTo(place) + 2;
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
			}
			
			if (bestDir != null){
				if(rc.senseMine(myLocation.add(bestDir)) == Team.NEUTRAL){
					rc.setIndicatorString(0, "Defusing. " + myLocation.add(defuseDir).toString());
					rc.defuseMine(myLocation.add(defuseDir));
				}
				else{
					rc.setIndicatorString(0, "Moving. " + myLocation.add(defuseDir).toString());
					rc.move(bestDir);
					this.movedFrom = bestDir;
				}
			}
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