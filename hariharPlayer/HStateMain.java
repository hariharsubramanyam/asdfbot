package hariharPlayer;

import battlecode.common.Direction;

public class HStateMain extends State{

	public HStateMain(StateMachine rootSM){
		this.stateID = SMConstants.H_STATE_MAIN;
		this.rootSM = rootSM;
		this.rc = rootSM.rc;
	}
	@Override
	public void doEntryAct() {}

	@Override
	public void doExitAct() {}

	@Override
	public void doAction() {
		try{
			Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
			if (rc.canMove(dir))
				rc.spawn(dir);
		}
		catch(Exception ex){ex.printStackTrace();}
	}

	@Override
	public State checkTransitions() {
		return this;
	}

}
