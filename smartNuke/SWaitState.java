/**
 * State for soldier state machine
 * Behavior - go to closest enemy or rally point
 */
package smartNuke;


import battlecode.common.*;

import java.util.ArrayList;

public class SWaitState extends State{
	
	public MapLocation rallyPoint;
	public MapLocation enemyHQ;
	public MapLocation alliedHQ;
	public MapLocation myLocation;
	public int mapWidth;
	public int mapHeight;
	public boolean edge;
	public Robot[] alliedRobots;
	public Robot[] enemyRobots;
	public Robot[] nearbyEnemyRobots;
	public MapLocation[] encamp;
	public Direction lastDirection;
	public MapLocation[] nearbyAlliedMines;
	public MapLocation[] nearbyNeutralMines;
	public ArrayList<MapLocation> nearbyMines;
	public boolean outward;
	// constructor
	public SWaitState(StateMachine rootSM){
		this.stateID = SMConstants.SWAITSTATE;
		this.rootSM = rootSM;
		this.rc = rootSM.getRC();
		this.lastDirection = rc.getLocation().directionTo(rc.senseHQLocation());
		this.mapHeight = rc.getMapHeight();
		this.mapWidth = rc.getMapWidth();
		this.outward = true;
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
/*				if (rootSM.rc.readBroadcast(39842) == 186254){
					PlayerConstants.NUM_ROBOTS_IN_ATTACK_GROUP = 10;
					this.rootSM.goToState(SMConstants.SATTACKSTATE);
				}*/
				this.rc.setIndicatorString(0, "Wait State.");
				if(enemyHQ == null)
					enemyHQ = rc.senseEnemyHQLocation();
				if(alliedHQ == null)
					alliedHQ = rc.senseHQLocation();
				myLocation = rc.getLocation();
				if (myLocation.x == 0 || myLocation.x == this.mapWidth-1 || myLocation.y == 0 || myLocation.y == this.mapHeight-1)
					this.edge = true;
				else
					this.edge = false;
				if (this.outward && (myLocation.distanceSquaredTo(alliedHQ) > 192 || this.edge))
					this.outward = false;
				if (!this.outward && myLocation.distanceSquaredTo(alliedHQ) < 16)
					this.outward = true;
				alliedRobots = rc.senseNearbyGameObjects(Robot.class,100000,rc.getTeam());
				enemyRobots = rc.senseNearbyGameObjects(Robot.class, 100000,rc.getTeam().opponent());
				nearbyEnemyRobots = rc.senseNearbyGameObjects(Robot.class,14,rc.getTeam().opponent());
				encamp = rc.senseEncampmentSquares(rc.getLocation(), 100000, Team.NEUTRAL);
				rc.setIndicatorString(0, "Wait state.");
				nearbyAlliedMines = rc.senseMineLocations(myLocation, 2, rc.getTeam());
				nearbyNeutralMines = rc.senseMineLocations(myLocation, 2, Team.NEUTRAL);
				nearbyMines = new ArrayList<MapLocation>();
				for(MapLocation mL: nearbyAlliedMines)
					nearbyMines.add(mL);
				for(MapLocation mL: nearbyNeutralMines)
					nearbyMines.add(mL);
				MapLocation myLocation = rc.getLocation();
				Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class, 14,rc.getTeam().opponent());

				if(enemyRobots.length > 0){
					MapLocation closestEnemy = findClosest(enemyRobots);
					MapLocation closestAlly = findClosest(alliedRobots);
					surround(closestEnemy, closestAlly, myLocation);
				}
				
				else{
					boolean northmine = false;
					boolean eastmine = false;
					boolean southmine = false;
					boolean westmine = false;
					boolean pickaxeEfficient = false;
					if(rc.hasUpgrade(Upgrade.PICKAXE)){
						pickaxeEfficient = true;
						for (MapLocation mine: nearbyMines){
							if (myLocation.directionTo(mine) == Direction.NORTH){
								northmine = true;
								continue;
							}
							if (myLocation.directionTo(mine) == Direction.EAST){
								eastmine = true;
								continue;
							}
							if (myLocation.directionTo(mine) == Direction.SOUTH){
								southmine = true;
								continue;
							}
							if (myLocation.directionTo(mine) == Direction.WEST){
								westmine = true;
								continue;
							}
						}
						if(northmine&&eastmine&&southmine&&westmine)
							pickaxeEfficient = false;
					}
					if (goodPlace(myLocation, alliedHQ, encamp)&&(rc.senseMine(myLocation)==null || pickaxeEfficient)&& myLocation.distanceSquaredTo(rc.senseHQLocation())>4 && !edge)
						rc.layMine();
					else
						findPlacesToLayMines(alliedHQ, alliedRobots, enemyRobots, nearbyEnemyRobots, myLocation, enemyHQ, this.outward);
					
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
	
	private boolean goodPlace(MapLocation location, MapLocation alliedHQ, MapLocation [] encamp) {
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
		if (rc.hasUpgrade(Upgrade.PICKAXE))
			return (d2>4 && d2<192 && !isEncamp && (2*location.x+location.y)%5==0);
		else
			return (d2>4 && d2<192 && !isEncamp);
	}
	
	//Movement system
	private void findPlacesToLayMines(MapLocation alliedHQ, Robot[] allies,Robot[] enemies,Robot[] nearbyEnemies,MapLocation myLocation, MapLocation enemyHQ, boolean outward) throws GameActionException {
		//This robot will be attracted to the goal and repulsed from other things
/*		MapLocation[] alliedMines = rc.senseMineLocations(myLocation, 600, rc.getTeam());
		MapLocation closestMine = null;
		if (alliedMines.length > 0)
			closestMine = findClosestMine(alliedMines);*/
		Direction toTarget;
		Direction going;
		if (outward){
			rc.setIndicatorString(1, "outward");
			toTarget = myLocation.directionTo(alliedHQ).opposite();
			int targetWeighting = targetWeight(myLocation.distanceSquaredTo(alliedHQ), enemyHQ, alliedHQ);
			MapLocation goalLoc = myLocation.add(toTarget,targetWeighting);//toward target, TODO weighted by the distance?
			goalLoc = goalLoc.add(myLocation.directionTo(enemyHQ),5);
			MapLocation closestAlly = findClosest(allies);
			goalLoc = goalLoc.add(myLocation.directionTo(closestAlly), -8);
			//TODO repel from allied mines?
			//now use that direction
			going = myLocation.directionTo(goalLoc);
			/*			int patienceCounter = 0;*/
			int[] directionOffsets = {0,1,-1,2,-2,3,-3,4};
			for (int d: directionOffsets){
				boolean emptySquare = true;
				Direction lookingAtCurrently = Direction.values()[(myLocation.directionTo(goalLoc).ordinal()+d+8)%8];
				for (MapLocation mine: nearbyMines){
					if (lookingAtCurrently == myLocation.directionTo(mine)){
						emptySquare = false;
						break;
					}
				}
				if (emptySquare == true && rc.canMove(lookingAtCurrently) && !rc.getLocation().add(lookingAtCurrently).isAdjacentTo(alliedHQ)){
					going = lookingAtCurrently;
					break;
				}
			}
			/*			while(going != lastDirection && rc.canMove(going)){
				int randNum = (int) ((Math.round(Math.random()*20))-1);
				if(randNum == 8 || randNum == 9){
					going = myLocation.directionTo(enemyHQ);
				}
				else{
					going = Direction.values()[randNum];
				}
				if(patienceCounter > 8){
					return;
				}
				patienceCounter++;
			}*/
			/*			lastDirection = going.opposite();*/
		}
		else{
			rc.setIndicatorString(1, "inward");
			going = myLocation.directionTo(alliedHQ);
		}
		goStraightToLocation(myLocation.add(going), myLocation);

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
	
	private MapLocation findClosestMine(MapLocation[] mines) throws GameActionException {
		int closestDist = 1000000;
		MapLocation closestMine=null;
		for (int i=0;i<mines.length;i++){
			int dist = mines[i].distanceSquaredTo(rc.getLocation());
			if (dist<closestDist){
				closestDist = dist;
				closestMine = mines[i];
			}
		}
		return closestMine;
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
	
	private void goStraightToLocation(MapLocation place, MapLocation myLocation)
			throws GameActionException {
		int dist = myLocation.distanceSquaredTo(place);
		if(dist > 0){
			int[] directionOffsets = {0,1,-1};
			Direction dir = myLocation.directionTo(place);
			boolean hasMoved = false;
			for (int d: directionOffsets){
				Direction lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
				if(rc.canMove(lookingAtCurrently) && !rc.getLocation().add(lookingAtCurrently).isAdjacentTo(alliedHQ)){
					Team teamOfMine = rc.senseMine(myLocation.add(lookingAtCurrently));
					if(teamOfMine==null || teamOfMine == rc.getTeam()){
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