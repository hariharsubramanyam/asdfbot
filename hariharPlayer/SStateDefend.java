/**
 * State for soldier state machine
 * Behavior - go to closest enemy or rally point
 */
package hariharPlayer;

import battlecode.common.Clock;
import battlecode.common.MapLocation;


public class SStateDefend extends State{
	
	// this is where our robots group together
	MapLocation rallyPoint;

	// constructor
	public SStateDefend(StateMachine rootSM){
		this.stateID = SMConstants.S_STATE_DEFEND;
		this.rootSM = rootSM;
		this.rc = rootSM.rc;
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
				MapLocation nearestEnemyLoc = ((SoldierSM)rootSM).dataManager.getLocationOfNearestEnemy(20);
				if(nearestEnemyLoc == null)
					((SoldierSM)rootSM).aStarManager.setTarget(rallyPoint);
				else
					((SoldierSM)rootSM).aStarManager.setTarget(nearestEnemyLoc);
				((SoldierSM)rootSM).aStarManager.move();
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

	@Override
	public State checkTransitions() {
		int newStateID = -1;
		
		if(Clock.getRoundNum() > 200)
			newStateID = SMConstants.S_STATE_ATTACK;
		
		if(newStateID != -1)
			return this.changeState(newStateID);
		else
			return this;
	}



}