/**
 * State machine for the soldier
 */
package trevPlayer;
import battlecode.common.*;


public class SoldierSM extends StateMachine {

	// Construct and set initial state
	public SoldierSM(RobotController rc){
		this.rc = rc;
		// SWAITSTATE = hang around near our HQ and group together, SATTACKSTATE = attack nearest enemy or enemy HQ
		this.stateIDs = new int[]{SMConstants.SWAITSTATE, SMConstants.SATTACKSTATE};
		this.goToState(SMConstants.SWAITSTATE);	// start in wait state
	}
	
	// Every turn, the robot takes a step in the state machine
	@Override
	public void step() {
		
		// see if we have to leave this current state
		for(Transition t : this.currentTransitions)
			if(t.isTriggered()){
				currentState.doExitAct();	// if we must leave, finish up any work in this state
				this.goToState(t.targetState);	// and go to the next one
				break;
			}
		currentState.doAction();			// finally, perform the behavior described by this state
	}

	// getter
	@Override
	public RobotController getRC() {
		return rc;
	}
	
	// transition to a new state, given its id
	@Override
	public void goToState(int stateID){
		this.currentState = SMConstants.getState(this, stateID); // create the state object using its id
		this.currentState.doEntryAct();							// perform any initialization behavior
		int[] transitionIDs = SMConstants.getTransitionsForState(stateID);		// get a list of transition ids for the transitions relating to this state
		Transition[] transitions = new Transition[transitionIDs.length];
		for(int i = 0; i < transitionIDs.length; i++)			// create transition objects from the transition ids
			transitions[i] = SMConstants.getTransition(this, transitionIDs[i]);
		this.currentTransitions = transitions;					
	}
	

}
