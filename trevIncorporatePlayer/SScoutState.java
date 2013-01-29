package trevIncorporatePlayer;

import battlecode.common.*;

public class SScoutState extends State {

	public Direction movedFrom;
	public MapLocation enemyHQ, alliedHQ, myLocation;
	public Robot[] alliedRobots, enemyRobots, nearbyEnemyRobots, nearbyAlliedRobots;
	public MapLocation[] myEncamp;
	
	public MapLocation cm;
	public boolean inGroup;
	public MapLocation traditionalRallyPoint;

	public SScoutState(StateMachine rootSM){
		this.stateID = SMConstants.SSCOUTSTATE;
		this.rootSM = rootSM;
		this.rc = rootSM.getRC();
		inGroup = false;
		enemyHQ = rc.senseEnemyHQLocation();
		alliedHQ = rc.senseHQLocation();
		movedFrom = rc.getLocation().directionTo(enemyHQ).opposite();
		this.traditionalRallyPoint = new MapLocation((int)(this.alliedHQ.x*.9+this.enemyHQ.x*.1),(int)(this.alliedHQ.y*.9+this.enemyHQ.y*.1));
	}
	@Override
	public void doEntryAct(){rc.setIndicatorString(0, "ScoutState");}

	@Override
	public void doExitAct(){}

	@Override
	public void doAction(){
		try{
			if(rc.isActive()){
				myLocation = rc.getLocation();
				this.goStraightToLocation(enemyHQ, myLocation);
			}
		}catch(Exception e){e.printStackTrace();}
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
					Team teamOfMine = rc.senseMine(myLocation.add(lookingAtCurrently));
					if(teamOfMine == null || teamOfMine == rc.getTeam()){
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
