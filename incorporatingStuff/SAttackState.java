package incorporatingStuff;

import battlecode.common.*;

public class SAttackState extends State {

	public Direction movedFrom;
	public MapLocation enemyHQ, alliedHQ, myLocation;
	public Robot[] alliedRobots, enemyRobots, nearbyEnemyRobots, nearbyAlliedRobots;
	public MapLocation[] myEncamp;
	
	public MapLocation cm;
	public boolean inGroup;
	public MapLocation traditionalRallyPoint;
	
	public SAttackState(StateMachine rootSM){
		this.stateID = SMConstants.SATTACKSTATE;
		this.rootSM = rootSM;
		this.rc = rootSM.getRC();
		inGroup = false;
		enemyHQ = rc.senseEnemyHQLocation();
		alliedHQ = rc.senseHQLocation();
/*		this.traditionalRallyPoint = alliedHQ.add(alliedHQ.directionTo(enemyHQ),(int)(0.08*alliedHQ.distanceSquaredTo(enemyHQ)));
*/		this.traditionalRallyPoint = new MapLocation((int)(this.alliedHQ.x*.67+this.enemyHQ.x*.33),(int)(this.alliedHQ.y*.67+this.enemyHQ.y*.33));
	}
	@Override
	public void doEntryAct(){}

	@Override
	public void doExitAct(){}

	@Override
	public void doAction(){
		try{
			if(rc.isActive()){
				myLocation = rc.getLocation();
				alliedRobots = rc.senseNearbyGameObjects(Robot.class,100000,rc.getTeam());
				enemyRobots = rc.senseNearbyGameObjects(Robot.class, 100000,rc.getTeam().opponent());
				nearbyEnemyRobots = rc.senseNearbyGameObjects(Robot.class, PlayerConstants.NEARBY_ENEMY_DIST_SQUARED,rc.getTeam().opponent());
				nearbyAlliedRobots = rc.senseNearbyGameObjects(Robot.class, PlayerConstants.NEARBY_ALLY_DIST_SQUARED,rc.getTeam());
				myEncamp = rc.senseAlliedEncampmentSquares();
				rc.setIndicatorString(0, "Attack State.");
				if(this.isHQUnderAttack() && rc.getLocation().distanceSquaredTo(alliedHQ) < PlayerConstants.WITHIN_HQ_RESCUING_RANGE_SQUARED){
					this.goToLocation(alliedHQ);
					return;
				}
				
				int artilleryInRangeMsg = this.getHQArtilleryMessage();
				if(artilleryInRangeMsg != 0){
					this.goToLocation(PlayerConstants.intToMapLocation(artilleryInRangeMsg));
					return;
				}
				
				if(!inGroup && nearbyAlliedRobots.length < PlayerConstants.NUM_ROBOTS_IN_ATTACK_GROUP){
					MapLocation closestEnemy = this.findClosest(nearbyEnemyRobots, this.rc.getTeam().opponent());
/*					MapLocation closestAlly = this.findClosest(nearbyAlliedRobots, this.rc.getTeam());
*/					if(closestEnemy != null){
/*						this.surround(closestEnemy, closestAlly, myLocation);*/
						goToLocation(closestEnemy);
						this.rc.setIndicatorString(1, "Going to enemy " + closestEnemy.toString());
					}
					else{
						this.goToLocation(this.traditionalRallyPoint);
						return;
					}
				}
				
				if(!inGroup && nearbyAlliedRobots.length > PlayerConstants.NUM_ROBOTS_IN_ATTACK_GROUP){
					inGroup = true;
				}
				
				if(inGroup){
					MapLocation closestEnemy = this.findClosest(nearbyEnemyRobots, this.rc.getTeam().opponent());
/*					MapLocation closestAlly = this.findClosest(nearbyAlliedRobots, this.rc.getTeam());
*/					if(closestEnemy != null){
/*						this.surround(closestEnemy, closestAlly, myLocation);
*/						goToLocation(closestEnemy);
						this.rc.setIndicatorString(1, "Going to enemy " + closestEnemy.toString());
					}
					else{
						this.goToLocation(enemyHQ);
						this.rc.setIndicatorString(1,"Going to enemyHQ " + enemyHQ.toString());
					}
					return;
				}
			}
		}catch(Exception e){e.printStackTrace();}
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
		goToLocation(myLocation.add(finalDir));
	}
	
	private void goToLocation(MapLocation place)
			throws GameActionException {
		int dist = rc.getLocation().distanceSquaredTo(place);
		if(dist > 0){
			int[] directionOffsets = {0,1,-1,2,-2};
			Direction dir = rc.getLocation().directionTo(place);
			Direction firstMine = null;
			MapLocation hqloc = rc.senseHQLocation();
			boolean hasMoved = false;
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