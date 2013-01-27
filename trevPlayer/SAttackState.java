/**
 * State in soldier state machine
 * Behavior - go to closest enemy robot or opponent HQ
 */
package trevPlayer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class SAttackState extends State {

	// basic constructor
	public SAttackState(StateMachine rootSM){
		this.stateID = SMConstants.SATTACKSTATE;
		this.rootSM = rootSM;
		this.rc = rootSM.getRC();
	}
	
	
	// does nothing for entry
	@Override
	public void doEntryAct() {}

	// does nothing for exit
	@Override
	public void doExitAct() {}

	// go to (and attack) the closest enemy in sight. If there's no enemy in sight, go to (and attack) the enemy HQ
	@Override
	public void doAction() {
		// need this try-catch or Eclipse gets mad
		
		try{
			if(rc.isActive()){
				Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,100000,rc.getTeam().opponent());
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
				else{
					// if we can't find enemies, go to enemy HQ
					goToLocation(rc.senseEnemyHQLocation());
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	// try to move in the direction of the target
	// if we can't, see what other spaces are open
	// if our path is blocked entirely by mines, defuse the one that lies on the direct path to the target 
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
					if(rc.senseMine(rc.getLocation().add(lookingAtCurrently))==null){
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
