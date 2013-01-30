package smartNukeExperiment;

import battlecode.common.MapLocation;
import battlecode.common.Robot;

public class WaitRallyTransition extends Transition {

	public WaitRallyTransition(StateMachine rootSM){
		this.rootSM = rootSM;
		this.sourceStates = new int[] {SMConstants.SWAITSTATE};
		this.targetState = SMConstants.SRALLYSTATE;
	}



	@Override
	public int[] getSourceStateID() {
		// TODO Auto-generated method stub
		return sourceStates;
	}

	@Override
	public State getTargetState() {
		return SMConstants.getState(rootSM, targetState);
	}

	@Override
	public boolean isTriggered() {
		MapLocation ours = this.rootSM.rc.senseHQLocation();
		int distance = this.rootSM.rc.senseEnemyHQLocation().distanceSquaredTo(ours);
		return (this.rootSM.rc.senseNearbyGameObjects(Robot.class, ours, distance/5, this.rootSM.rc.getTeam().opponent()).length>3);//||(this.rootSM.rc.getLocation().distanceSquaredTo(this.rootSM.rc.senseHQLocation()) > 192);
		
	}

}