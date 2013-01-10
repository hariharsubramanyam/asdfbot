package SMPlayer;
import battlecode.common.*;


public class SoldierSM extends StateMachine {

	public SoldierSM(RobotController rc){
		this.rc = rc;
		this.stateIDs = new int[]{SMConstants.SWAITSTATE, SMConstants.SATTACKSTATE};
		this.goToState(SMConstants.SWAITSTATE);
	}
	
	@Override
	public void step() {
		
		for(Transition t : this.currentTransitions)
			if(t.isTriggered()){
				currentState.doExitAct();
				this.goToState(t.targetState);
				break;
			}
		currentState.doAction();		
	}

	@Override
	public RobotController getRC() {
		return rc;
	}
	
	@Override
	public void goToState(int stateID){
		this.currentState = SMConstants.getState(this, stateID);
		this.currentState.doEntryAct();
		int[] transitionIDs = SMConstants.getTransitionsForState(stateID);
		Transition[] transitions = new Transition[transitionIDs.length];
		for(int i = 0; i < transitionIDs.length; i++)
			transitions[i] = SMConstants.getTransition(this, transitionIDs[i]);
		this.currentTransitions = transitions;
	}
	

}
