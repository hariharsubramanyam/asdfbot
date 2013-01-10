package controllerapproachTrev;
import battlecode.common.*;

public class HQController implements GeneralController{
	
	public final RobotController rc;
	private String state;
	
	public HQController(RobotController rc){
		this.state = "spawn";
		this.rc = rc;
		
		while(true){
			try{
				/*switch(state){
				case "spawn":
					new spawnStateHQ(this, rc);
					rc.yield();
					break;
				case "research":
					rc.yield();
					break;
					
				}*/
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		
	}

}
