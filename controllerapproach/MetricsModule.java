package controllerapproach;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class MetricsModule implements IModule {

	private boolean hasActed;
	private RobotController rc;
	
	public static Team team;
	
	private double energon;
	private MapLocation location;
	private double shields;
	private RobotType robotType;
	private boolean robotActive;
	

	public MetricsModule(RobotController rc){
		this.rc = rc;
		this.hasActed = false;
		if(MetricsModule.team == null)
			MetricsModule.team = this.rc.getTeam();
		
		this.robotType = this.rc.getType();
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
		this.energon = this.rc.getEnergon();
		this.location = this.rc.getLocation();
		this.shields = this.rc.getShields();
		//this.teamPower = this.rc.getTeamPower();
		this.robotActive = this.rc.isActive();
		this.hasActed = true;
	}

	@Override
	public void actDependencies() {
		// has no dependencies
	}

	@Override
	public void endTurn() {
		this.hasActed = false;
		
	}
	
	public static Team getTeam() {
		return team;
	}


	public boolean isHasActed() {
		return hasActed;
	}

	public RobotController getRc() {
		return rc;
	}

	public double getEnergon() {
		return energon;
	}

	public MapLocation getLocation() {
		return location;
	}

	public double getShields() {
		return shields;
	}

	//public double getTeamPower() {
	//	return teamPower;
	//}

	public RobotType getRobotType() {
		return robotType;
	}

	public boolean isRobotActive() {
		return robotActive;
	}

	
	
}
