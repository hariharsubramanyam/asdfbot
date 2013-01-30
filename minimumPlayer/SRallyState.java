package minimumPlayer;

import battlecode.common.*;

public class SRallyState extends State {

	public Direction movedFrom;
	public MapLocation enemyHQ, alliedHQ, myLocation;
	public Robot[] alliedRobots, enemyRobots, nearbyEnemyRobots, nearbyAlliedRobots;
	public MapLocation[] myEncamp;
	public MapLocation[] encamp;
	public MapLocation cm;
	public boolean inGroup;
	public MapLocation traditionalRallyPoint;
	
	public SRallyState(StateMachine rootSM){
		this.stateID = SMConstants.SRALLYSTATE;
		this.rootSM = rootSM;
		this.rc = rootSM.getRC();
		inGroup = false;
		enemyHQ = rc.senseEnemyHQLocation();
		alliedHQ = rc.senseHQLocation();
		this.traditionalRallyPoint = new MapLocation((int)(this.alliedHQ.x*.9+this.enemyHQ.x*.1),(int)(this.alliedHQ.y*.9+this.enemyHQ.y*.1));
	}
	@Override
	public void doEntryAct(){}

	@Override
	public void doExitAct(){}

	@Override
	public void doAction(){
		try{
			if(rc.isActive()){
/*				rc.setIndicatorString(1, "got message.");

				if (rootSM.rc.readBroadcast(39842) == 186254){
					PlayerConstants.NUM_ROBOTS_IN_ATTACK_GROUP = 10;
					this.rootSM.goToState(SMConstants.SATTACKSTATE);
				}*/
				rc.setIndicatorString(0, "Rally State.");
				myLocation = rc.getLocation();
				alliedRobots = rc.senseNearbyGameObjects(Robot.class,100000,rc.getTeam());
				enemyRobots = rc.senseNearbyGameObjects(Robot.class, 100000,rc.getTeam().opponent());
				nearbyEnemyRobots = rc.senseNearbyGameObjects(Robot.class, PlayerConstants.NEARBY_ENEMY_DIST_SQUARED,rc.getTeam().opponent());
				nearbyAlliedRobots = rc.senseNearbyGameObjects(Robot.class, PlayerConstants.NEARBY_ALLY_DIST_SQUARED,rc.getTeam());
				int numSols = this.numSoldier(nearbyAlliedRobots);
				encamp = rc.senseEncampmentSquares(rc.getLocation(), 100000, Team.NEUTRAL);
				myEncamp = rc.senseAlliedEncampmentSquares();
				if(this.isHQUnderAttack() && rc.getLocation().distanceSquaredTo(alliedHQ) < PlayerConstants.WITHIN_HQ_RESCUING_RANGE_SQUARED){
					this.goStraightToLocation(alliedHQ, myLocation);
					return;
				}

				int artilleryInRangeMsg = this.getHQArtilleryMessage();
				if(artilleryInRangeMsg != 0){
					this.goStraightToLocation(PlayerConstants.intToMapLocation(artilleryInRangeMsg), myLocation);
					return;
				}

				if(!inGroup && numSols > PlayerConstants.NUM_ROBOTS_IN_ATTACK_GROUP){
					inGroup = true;
				}

				if(!inGroup && numSols < PlayerConstants.NUM_ROBOTS_IN_ATTACK_GROUP){
					MapLocation closestEnemy = this.findClosest(nearbyEnemyRobots, this.rc.getTeam().opponent());
					if(closestEnemy != null){
						this.goToLocation(closestEnemy);
						this.rc.setIndicatorString(1, "Going to enemy " + closestEnemy.toString());
					}
					else if(myLocation.distanceSquaredTo(this.traditionalRallyPoint) < 192 && rc.hasUpgrade(Upgrade.PICKAXE) && goodPlace(myLocation, alliedHQ, encamp) && rc.senseMine(myLocation)==null)
						rc.layMine();
					else
						this.goToLocation(this.traditionalRallyPoint);
					return;
				}

				if(inGroup){
					MapLocation closestEnemy = this.findClosest(nearbyEnemyRobots, this.rc.getTeam().opponent());
					if(closestEnemy != null){
						this.goToLocation(closestEnemy);
						this.rc.setIndicatorString(1, "Going to enemy " + closestEnemy.toString());
					}
					else{
						if(myLocation.distanceSquaredTo(this.traditionalRallyPoint) < 192 && rc.hasUpgrade(Upgrade.PICKAXE) && goodPlace(myLocation, alliedHQ, encamp) && rc.senseMine(myLocation)==null)
							rc.layMine();
						else
							this.goToLocation(this.traditionalRallyPoint);
/*							this.moveTogether(this.traditionalRallyPoint, alliedHQ, alliedRobots, enemyRobots, nearbyEnemyRobots, myLocation, enemyHQ);
*/						this.rc.setIndicatorString(1,"Going to enemyHQ " + enemyHQ.toString());
					}
					return;
				}
			}
			
		}catch(Exception e){e.printStackTrace();}
	}
	
	public int numSoldier(Robot[] robs){
		int ns = 0;
		RobotInfo robInf;
		for(Robot r : robs){
			try{
				robInf = rc.senseRobotInfo(r);
				if(robInf.type == RobotType.SOLDIER)
					ns++;
			}catch(Exception ex){ex.printStackTrace();}
		}
		return ns;
	}
	
	private boolean isHQUnderAttack(){
		try{
			return this.rc.readBroadcast(PlayerConstants.HQ_UNDER_ATTACK_CHANNEL) == 1;
		}catch(Exception ex){ex.printStackTrace();return false; }
	}

	//Movement system

	private int getHQArtilleryMessage(){
		try{
			return this.rc.readBroadcast(PlayerConstants.ARTILLERY_IN_SIGHT_MESSAGE);
		}
		catch(Exception ex){ ex.printStackTrace(); return 0;}
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
		return (d2>4 && d2<192 && !isEncamp && (2*location.x+location.y)%5==0);
	}
	
	private void moveTogether(MapLocation toGo, MapLocation alliedHQ, Robot[] allies,Robot[] enemies,Robot[] nearbyEnemies,MapLocation myLocation, MapLocation enemyHQ) throws GameActionException {
		//This robot will be attracted to the goal and repulsed from other things
		Direction toTarget = myLocation.directionTo(toGo);
		int targetWeighting = 10;
		MapLocation goalLoc = myLocation.add(toTarget,targetWeighting);//toward target, TODO weighted by the distance?
		goalLoc = goalLoc.add(myLocation.directionTo(enemyHQ));
/*		goalLoc = goalLoc.add(myLocation.directionTo(enemyHQ),3);
*/		MapLocation closestAlly = findClosest(allies, rc.getTeam());
		goalLoc = goalLoc.add(myLocation.directionTo(closestAlly), 5);
		//TODO repel from allied mines?
		//now use that direction
		Direction finalDir = myLocation.directionTo(goalLoc);
/*		if (Math.random()<.1)
			finalDir = finalDir.rotateRight();*/
		goToLocation(myLocation.add(finalDir));
	}
	
	boolean hasMoved = false;
	
	private void goToLocation(MapLocation place)
			throws GameActionException {
		int dist = rc.getLocation().distanceSquaredTo(place);
		if(dist > 0){
			int[] directionOffsets = {0,1,-1,2,-2};
			Direction dir = rc.getLocation().directionTo(place);
			Direction firstMine = null;
			MapLocation hqloc = rc.senseHQLocation();
			for (int d: directionOffsets){
				Direction lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
				Team teamOfMine = rc.senseMine(rc.getLocation().add(lookingAtCurrently));
				if(rc.canMove(lookingAtCurrently) && !rc.getLocation().add(lookingAtCurrently).isAdjacentTo(hqloc)){
					if(teamOfMine == null || teamOfMine == this.rc.getTeam()){
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
	
	private void goStraightToLocation(MapLocation place, MapLocation myLocation)
			throws GameActionException {
		int dist = myLocation.distanceSquaredTo(place);
		if(dist > 0){
			int[] directionOffsets = {0,1,-1};
			Direction dir = myLocation.directionTo(place);
/*			boolean hasMoved = false;
*/			for (int d: directionOffsets){
				Direction lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
				if(rc.canMove(lookingAtCurrently) && !rc.getLocation().add(lookingAtCurrently).isAdjacentTo(alliedHQ)){
					Team teamOfMine = rc.senseMine(myLocation.add(lookingAtCurrently));
					if(teamOfMine==null || teamOfMine == rc.getTeam()){
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


	private MapLocation findClosest(Robot[] robots, Team tm) throws GameActionException {
		int closestDist = 1000000;
		MapLocation closestBotLoc=null;
		
		int dist;
		MapLocation loc;
		for(Robot r : robots)
			if(r == null)
				continue;
			else if(tm != null && r.getTeam() != tm)
				continue;
			else{
				loc = this.rc.senseRobotInfo(r).location;
				dist = loc.distanceSquaredTo(this.rc.getLocation());
				if(dist < closestDist){
					closestDist = dist;
					closestBotLoc = loc;
				}
			}
		return closestBotLoc;
	}	
}