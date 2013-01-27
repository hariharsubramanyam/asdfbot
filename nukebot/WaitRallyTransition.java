package nukebot;

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
		return (this.rootSM.rc.getLocation().distanceSquaredTo(this.rootSM.rc.senseHQLocation()) > 192);
	}

}