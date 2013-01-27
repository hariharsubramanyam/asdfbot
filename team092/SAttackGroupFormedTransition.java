package team092;

import battlecode.common.Robot;
import battlecode.common.RobotType;

public class SAttackGroupFormedTransition extends Transition{
	public final int ALLIED_HQ_RADIUS_SQUARED = 192;
	public final int SOLDIERS_FOR_ATTACK_GROUP = 10;
	
	public SAttackGroupFormedTransition(StateMachine rootSM){
		this.rootSM = rootSM;
		this.sourceStates = new int[] {SMConstants.SRALLYSTATE,SMConstants.SWAITSTATE};
		this.targetState = SMConstants.SATTACKSTATE;
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
		Robot[] nearbyAllies = this.rootSM.rc.senseNearbyGameObjects(Robot.class, this.rootSM.rc.senseHQLocation(), this.ALLIED_HQ_RADIUS_SQUARED, this.rootSM.rc.getTeam());
		int numSoldiers = 0;
		for(Robot r : nearbyAllies)
			try{
				if(r != null && this.rootSM.rc.senseRobotInfo(r).type == RobotType.SOLDIER)
					numSoldiers++;
			}catch(Exception ex){ex.printStackTrace();}
		return (numSoldiers >= this.SOLDIERS_FOR_ATTACK_GROUP);
	}

}
