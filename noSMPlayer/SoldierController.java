package noSMPlayer;

import battlecode.common.RobotController;

public class SoldierController {
	
	public RobotController rc;
	
	public SoldierController(RobotController rc){
		this.rc = rc;
	}
	
	public void run(){
		while(true){
			try{
				if(rc.isActive()){
					
					
					rc.yield();
				}
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}

}
