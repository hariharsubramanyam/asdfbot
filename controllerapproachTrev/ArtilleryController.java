package controllerapproachTrev;

import battlecode.common.*;

public class ArtilleryController implements GeneralController{
	
	
	public final RobotController rc;

	// Constructor, call the parent constructor
	public ArtilleryController(RobotController rc){
		this.rc = rc;
	}
}
