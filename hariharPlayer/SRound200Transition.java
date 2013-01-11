/**
 * From: SWAITSTATE
 * To: SATTACKSTATE
 * Condition: RoundNum > 200
 */
package hariharPlayer;

import battlecode.common.Clock;

public class SRound200Transition extends Transition {
	
	// construct and set up our state ids
	public SRound200Transition(StateMachine rootSM){
		this.rootSM = rootSM;
		this.sourceState = SMConstants.SWAITSTATE;
		this.targetState = SMConstants.SATTACKSTATE;
	}
	
	

	//getter for source state id
	@Override
	public int getSourceStateID() {
		// TODO Auto-generated method stub
		return sourceState;
	}

	//getter for target state object
	@Override
	public State getTargetState() {
		return SMConstants.getState(rootSM, targetState);
	}

	
	//check if the transition is triggered
	@Override
	public boolean isTriggered() {
		return (Clock.getRoundNum() > 200);
	}

}
