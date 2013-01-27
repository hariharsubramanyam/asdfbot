package hariharPlayer;

import battlecode.common.MapLocation;

public class SStateBuild extends State{
	
	public MapLocation encampmentSpot;
	
	public SStateBuild(StateMachine rootSM){
		this.stateID = SMConstants.S_STATE_BUILD;
		this.rootSM = rootSM;
		this.rc = rootSM.rc;
	}

	@Override
	public void doEntryAct() {
		DataManager dataManager = ((SoldierSM)rootSM).dataManager;
		MapLocation HQLoc = dataManager.HQLoc;
		
		int minDist = 100000;
		
		int currDist;
		for(MapLocation l : dataManager.allEncampments){
			currDist = this.dSquared(l, HQLoc);
			if(currDist < minDist){
				this.encampmentSpot = l;
				minDist = currDist;
			}
		}
	}

	@Override
	public void doExitAct() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doAction() {
	}

	@Override
	public State checkTransitions() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public int dSquared(MapLocation a, MapLocation b){
		return ((a.x-b.x)*(a.x-b.x)+(a.y-b.y)*(a.y-b.y));
	}

}
