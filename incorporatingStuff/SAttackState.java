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
				this.rc.setIndicatorString(0, "Attack State");
				
				myLocation = rc.getLocation();
				alliedRobots = rc.senseNearbyGameObjects(Robot.class,100000,rc.getTeam());
				enemyRobots = rc.senseNearbyGameObjects(Robot.class, 100000,rc.getTeam().opponent());
				nearbyEnemyRobots = rc.senseNearbyGameObjects(Robot.class, PlayerConstants.NEARBY_ENEMY_DIST_SQUARED,rc.getTeam().opponent());
				nearbyAlliedRobots = rc.senseNearbyGameObjects(Robot.class, PlayerConstants.NEARBY_ALLY_DIST_SQUARED,rc.getTeam());
				myEncamp = rc.senseAlliedEncampmentSquares();
				this.rc.setIndicatorString(2, ""+this.nearbyAlliedRobots.length);
				
				if(!inGroup){
					cm = this.getCenterOfMass((rc.senseNearbyGameObjects(Robot.class,1000000,rc.getTeam())));
					this.goToLocation(cm);
				}
				if(!inGroup && nearbyAlliedRobots.length > PlayerConstants.NUM_ROBOTS_IN_ATTACK_GROUP){
					inGroup = true;
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
				}
				
//				if(nearbyEnemyRobots.length > 0){
//					int closestDist = 10000000;
//					MapLocation closestEnemy = null;
//					MapLocation closestAlly = null;
//					for(Robot r : enemyRobots){
//						RobotInfo aRobotInfo = rc.senseRobotInfo(r);
//						int dist = aRobotInfo.location.distanceSquaredTo(rc.getLocation());
//						if(dist < closestDist){
//							closestDist = dist;;
//							closestEnemy = aRobotInfo.location;
//						}
//					}
//					closestDist = 10000000;
//					for(Robot r : alliedRobots){
//						RobotInfo aRobotInfo = rc.senseRobotInfo(r);
//						int dist = aRobotInfo.location.distanceSquaredTo(rc.getLocation());
//						if(dist < closestDist){
//							closestDist = dist;;
//							closestAlly = aRobotInfo.location;
//						}
//					}
//					if(nearbyAlliedRobots.length >= nearbyEnemyRobots.length){
//						goToLocation(closestEnemy);
//					}
//					else if(myLocation.distanceSquaredTo(alliedHQ) >= myLocation.distanceSquaredTo(enemyHQ)){
//						MapLocation closestEncampment = null;
//						for(MapLocation ml : myEncamp){
//							int dist = ml.distanceSquaredTo(rc.getLocation());
//							if(dist < closestDist){
//								closestDist = dist;;
//								closestEncampment = ml;
//							}
//						}
//						goToLocation(closestEncampment);
//						//goToLocation(getHQCenterOfMassBroadcast());
//					}
//					else{
//						goToLocation(closestEnemy);
//					}
//				}
//				else{
//					goToLocation(enemyHQ);
//				}
			}
		}catch(Exception e){e.printStackTrace();}
	}
	
	public MapLocation getCenterOfMass(Robot[] robots){
		int x = 10*this.traditionalRallyPoint.x, y = 10*this.traditionalRallyPoint.y;
		int count = 10;
		MapLocation robotLoc;
		for(Robot r : robots)
			if(r != null && r.getTeam() == this.rc.getTeam()){
				try {
					robotLoc = this.rc.senseRobotInfo(r).location;
					x += robotLoc.x;
					y += robotLoc.y;
					count++;
				} catch (GameActionException e) {e.printStackTrace();}
			}
		if(x == 0 && y == 0)
			return this.rc.getLocation();
		MapLocation res = new MapLocation((int)(1.0*x/count),(int)(1.0*y/count));
		return res;
	}
	
	public MapLocation getHQCenterOfMassBroadcast(){
		try {
			String msg = "" + this.rc.readBroadcast(PlayerConstants.HQ_CENTER_OF_MASS_CHANNEL);
			return new MapLocation(Integer.parseInt(msg.substring(1, 4)),Integer.parseInt(msg.substring(5)));
		} catch (GameActionException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//Movement system

	private void goToLocation(MapLocation place)
			throws GameActionException {
		int dist = rc.getLocation().distanceSquaredTo(place);
		if(dist > 0){
			int[] directionOffsets = {0,1,-1,2,-2};
			Direction dir = rc.getLocation().directionTo(place);
			Direction firstMine = null;
			boolean hasMoved = false;
			for (int d: directionOffsets){
				Direction lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
				Team teamOfMine = rc.senseMine(rc.getLocation().add(lookingAtCurrently));
				if(rc.canMove(lookingAtCurrently)){
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
/*				else if (place.distanceSquaredTo(rc.getLocation())>4){
					rc.layMine();
				}*/
			}
		}
	}

	private void flank(MapLocation closestEnemy, MapLocation closestAlly)
			throws GameActionException{
		Direction direnemy = rc.getLocation().directionTo(closestEnemy);
		Direction dirally = rc.getLocation().directionTo(closestAlly);
		int[] directionOffsets = {1,-1,0};
		boolean hasmoved = false;
		for(int d : directionOffsets){
			Direction lookingAtCurrently = Direction.values()[(direnemy.ordinal()+d+8)%8];
			if(rc.canMove(lookingAtCurrently)){
				if(((rc.senseMine(rc.getLocation().add(lookingAtCurrently))))==null && lookingAtCurrently != dirally){
					rc.move(lookingAtCurrently);
					hasmoved = true;
				}
				else
					continue;
			}
			else
				continue;
		}
		if(!hasmoved){
			goToLocation(closestEnemy);
		}
	}

	private void retreat(MapLocation closestEnemy, MapLocation closestEncampment)
			throws GameActionException{
		Direction direnemy = rc.getLocation().directionTo(closestEnemy);
		int[] directionOffsets = {4,-3,3,-2,2};
		boolean hasmoved = false;
		for(int d : directionOffsets){
			Direction lookingAtCurrently = Direction.values()[(direnemy.ordinal()+d+8)%8];
			if(rc.canMove(lookingAtCurrently)){
				if(((rc.senseMine(rc.getLocation().add(lookingAtCurrently))))==null){
					rc.move(lookingAtCurrently);
					hasmoved = true;
				}
				else
					continue;
			}
			else
				continue;
		}
		if(!hasmoved){
			goToLocation(closestEncampment);
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