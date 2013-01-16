package tempPlayer;

public class AStateAutonomous extends State {

	public AStateAutonomous(StateMachine rootSM){
		this.stateID = SMConstants.A_STATE_AUTONOMOUS;
		this.rootSM = rootSM;
		this.rc = rootSM.rc;
	}
	
	@Override
	public void doEntryAct() {}

	@Override
	public void doExitAct() {}

	@Override
	public void doAction() {}

	@Override
	public State checkTransitions() {
		return this;
	}

}
