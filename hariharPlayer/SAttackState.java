/**
 * State in soldier state machine
 * Behavior - go to closest enemy robot or opponent HQ
 */
package hariharPlayer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;

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
				// get visible enemies
				Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,100000,rc.getTeam().opponent());
				// find the closest enemy
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
					// go to closest enemy
					NavigationManager.goToLocation(closestEnemy,rc);
				}
				else{
					// if we can't find enemies, go to enemy HQ
					NavigationManager.goToLocation(rc.senseEnemyHQLocation(),rc);
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	
	

}
