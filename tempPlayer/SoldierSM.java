/**
 * State machine for the soldier
 */
package tempPlayer;
import battlecode.common.*;


public class SoldierSM extends StateMachine {
	
	public DataManager dataManager;
	public NavigationManager aStarManager;
	
	public SoldierSM(RobotController rc){
		this.rc = rc;
		this.dataManager = new DataManager(this);
		this.aStarManager = new NavigationManager(this);
		this.enterInitialState();
	}
	
	@Override
	public void step(){
		dataManager.update(false, false, false);
		aStarManager.update();
		currentState = currentState.checkTransitions(); // perform any necessary transitions and update the state
		currentState.doAction();			// perform the behavior described by this state
	}

	@Override
	public void enterInitialState() {
		this.currentState = SMConstants.getState(this, SMConstants.S_STATE_DEFEND);
		this.currentState.doEntryAct();
	}

}
