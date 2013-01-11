/**
 * State for soldier state machine
 * Behavior - go to closest enemy or rally point
 */
package trevPlayer;


import battlecode.common.*;


public class SWaitState extends State{
	
	// this is where our robots group together
	MapLocation rallyPoint;

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
			/*if(rc.isActive()){
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
			}*/
			if(rc.isActive()){
				Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,100000,rc.getTeam().opponent());
				MapLocation[] encamp = rc.senseEncampmentSquares(rc.getLocation(), 10, Team.NEUTRAL);
				MapLocation[] myEncamp = rc.senseAlliedEncampmentSquares();
				System.out.println("Nearby Encampments: "+ encamp.length);
				if(enemyRobots.length > 0){
					int closestDist = 10000000;
					MapLocation closestEnemy = null;
					for(int i = 0; i < enemyRobots.length; i++){
						Robot aRobot = enemyRobots[i];
						RobotInfo aRobotInfo = rc.senseRobotInfo(aRobot);
						int dist = aRobotInfo.location.distanceSquaredTo(rc.getLocation());
						if(dist < closestDist){
							closestDist = dist;
							closestEnemy = aRobotInfo.location;
						}
					}
					goToLocation(closestEnemy);

					
				}
				else if(encamp.length>0 && myEncamp.length < 3){
					MapLocation myLocation = rc.getLocation();
					MapLocation closestEncamp = null;
					for (MapLocation mL : encamp){
						int closestEncampDis = 100000;
						if(mL.distanceSquaredTo(myLocation)==0){
							rc.captureEncampment(RobotType.ARTILLERY);
							break;
						}
						else{
							if(mL.distanceSquaredTo(myLocation) < closestEncampDis){
								closestEncamp = mL;
							}
						}
					}
					if(closestEncamp !=null){
						goToLocation(closestEncamp);
					}
				}
				else{
					goToLocation(rallyPoint);
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	// rally point is a weighted average of our HQ position and opponent HQ position
	private MapLocation findRallyPoint() {
		MapLocation enemyLoc = rc.senseEnemyHQLocation();
		MapLocation ourLoc = rc.senseHQLocation();
		int x = (enemyLoc.x + 3*ourLoc.x)/4;
		int y = (enemyLoc.y + 3*ourLoc.y)/4;
		return new MapLocation(x,y);
	}

	
	// see SAttackState's goToLocation method - it is identical
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
						rc.move(lookingAtCurrently);
						hasMoved = true;
						break;
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