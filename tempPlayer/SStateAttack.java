/**
 * State in soldier state machine
 * Behavior - go to closest enemy robot or opponent HQ
 */
package tempPlayer;

import battlecode.common.MapLocation;


public class SStateAttack extends State {

	// basic constructor
	public SStateAttack(StateMachine rootSM){
		this.stateID = SMConstants.S_STATE_ATTACK;
		this.rootSM = rootSM;
		this.rc = rootSM.rc;
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
				MapLocation nearestEnemyLoc = ((SoldierSM)rootSM).dataManager.getLocationOfNearestEnemy(20);
				if(nearestEnemyLoc == null)
					((SoldierSM)rootSM).aStarManager.setTarget(((SoldierSM)rootSM).dataManager.enemyHQLoc);
				else
					((SoldierSM)rootSM).aStarManager.setTarget(nearestEnemyLoc);
				((SoldierSM)rootSM).aStarManager.move();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}


	@Override
	public State checkTransitions() {
		return this;
	}
	
	
	
	

}
