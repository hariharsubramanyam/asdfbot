package team092;

import battlecode.common.*;

public class SAttackState extends State {

	public Direction movedFrom;
	public MapLocation enemyHQ, alliedHQ, myLocation;
	public Robot[] alliedRobots, enemyRobots, nearbyEnemyRobots, nearbyAlliedRobots;
	public MapLocation[] myEncamp;
	private int attackChannel = 9559;
	public MapLocation cm;
	public boolean inGroup;
	public boolean attackNow;
	public MapLocation traditionalRallyPoint;
	public MapLocation toGo;
	
	public SAttackState(StateMachine rootSM){
		this.stateID = SMConstants.SATTACKSTATE;
		this.rootSM = rootSM;
		this.rc = rootSM.getRC();
		inGroup = false;
		enemyHQ = rc.senseEnemyHQLocation();
		alliedHQ = rc.senseHQLocation();
		this.traditionalRallyPoint = new MapLocation((int)(this.alliedHQ.x*.75+this.enemyHQ.x*.25),(int)(this.alliedHQ.y*.75+this.enemyHQ.y*.25));
	}
	@Override
	public void doEntryAct(){}

	@Override
	public void doExitAct(){}

	@Override
	public void doAction(){
		try{
			if(rc.isActive()){
				this.toGo = PlayerConstants.intToMapLocation(rc.readBroadcast(attackChannel));
				rc.setIndicatorString(0, "Attack State.");
				myLocation = rc.getLocation();
				alliedRobots = rc.senseNearbyGameObjects(Robot.class,100000,rc.getTeam());
				enemyRobots = rc.senseNearbyGameObjects(Robot.class, 100000,rc.getTeam().opponent());
				nearbyEnemyRobots = rc.senseNearbyGameObjects(Robot.class, 4,rc.getTeam().opponent());
				nearbyAlliedRobots = rc.senseNearbyGameObjects(Robot.class, PlayerConstants.NEARBY_ALLY_DIST_SQUARED,rc.getTeam());
				/*int numSols = this.numSoldier(nearbyAlliedRobots);*/
				myEncamp = rc.senseAlliedEncampmentSquares();
				if(rc.readBroadcast(58621) == 498)
					attackNow = true;
				if (attackNow){
					goStraightToLocation(enemyHQ, myLocation);
					return;
				}
				else if (this.toGo.equals(enemyHQ))
					this.goToLocation(toGo);
				else
					this.goStraightToLocation(toGo, myLocation);
					
/*				if(this.isHQUnderAttack() && rc.getLocation().distanceSquaredTo(alliedHQ) < PlayerConstants.WITHIN_HQ_RESCUING_RANGE_SQUARED){
					this.goToLocation(alliedHQ);
					return;
				}
				
				int artilleryInRangeMsg = this.getHQArtilleryMessage();
				if(artilleryInRangeMsg != 0){
					this.goToLocation(PlayerConstants.intToMapLocation(artilleryInRangeMsg));
					return;
				}
				
				if(!inGroup && numSols > PlayerConstants.NUM_ROBOTS_IN_ATTACK_GROUP){
					inGroup = true;
				}
				
				if(!inGroup && numSols < PlayerConstants.NUM_ROBOTS_IN_ATTACK_GROUP){
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
						this.goToLocation(enemyHQ);
						this.rc.setIndicatorString(1,"Going to enemyHQ " + enemyHQ.toString());
					}
					return;
				}*/
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