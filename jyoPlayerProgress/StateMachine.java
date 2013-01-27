/**
 * This is an abstract class, so don't instantiate it.
 * State machine. HQSM, SoldierSM, and ArtillerySM should derive from this
 */
package jyoPlayerProgress;
import battlecode.common.*;

public abstract class StateMachine {
	
	RobotController rc;
	int[] stateIDs;
	State currentState;
	Transition[] currentTransitions;
	
	public void step(){
		// see if we have to leave this current state
		for(Transition t : this.currentTransitions)
			if(t.isTriggered() && t.sourceState == currentState.stateID){
				currentState.doExitAct();	// if we must leave, finish up any work in this state
				this.goToState(t.targetState);	// and go to the next one
				break;
			}
		currentState.doAction();			// finally, perform the behavior described by this state
	}
	
	public RobotController getRC(){
		return rc;
	}
	
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
