package controllerapproach;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class SensorModule implements IModule {

	public final static int UPDATE_DELAY_MINE = 10;
	
	private RobotController rc;
	
	
	private boolean hasActed;
	
	private static long[] teamMemory;
	private static MapLocation hqLoc;
	private static MapLocation enemyhqLoc;
	
	private static double teamPower;
	private static MapLocation[] encampmentSquares;
	
	public SensorModule(RobotController rc){
		this.rc = rc;
		this.hasActed = false;
		
		if(SensorModule.hqLoc == null)
			SensorModule.hqLoc = this.rc.senseHQLocation();
		if(SensorModule.enemyhqLoc == null)
			SensorModule.enemyhqLoc = this.rc.senseEnemyHQLocation();
		if(SensorModule.encampmentSquares == null)
			SensorModule.encampmentSquares = this.rc.senseAllEncampmentSquares();
		if(SensorModule.teamMemory == null)
			SensorModule.teamMemory = this.rc.getTeamMemory();
	}
	
	@Override
	public boolean hasActed() {
		return this.hasActed;
	}

	@Override
	public void act() {
		if(this.hasActed)
			return;
		this.actDependencies();
		this.hasActed = true;

	}

	@Override
	public void actDependencies() {
		// No dependencies
	}

	@Override
	public void endTurn() {
		this.hasActed = false;

	}

}
