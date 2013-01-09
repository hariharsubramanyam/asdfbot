package controllerapproach;

import battlecode.common.RobotController;

public class SpawnerModule implements IModule {

	private boolean hasActed;
	private MetricsModule metrics;
	private RobotController rc;
	
	public SpawnerModule(MetricsModule metrics, RobotController rc){
		this.metrics = metrics;
		this.rc = rc;
		this.hasActed = false;
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
		
		if(this.metrics.isRobotActive())
		this.hasActed = true;

	}

	@Override
	public void actDependencies() {
		if(!this.metrics.hasActed())
			this.metrics.act();

	}

	@Override
	public void endTurn() {
		this.hasActed = false;

	}

}
