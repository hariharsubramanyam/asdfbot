package controllerapproach;

import battlecode.common.MapLocation;
import battlecode.common.RobotType;

public class RobotData implements Comparable{
	
	public int id;
	public MapLocation location;
	public double shields;
	public RobotType type;
	public boolean active;
	public double energon;
	
	public RobotData(int id, MapLocation location, double energon, double shields, RobotType type, boolean active){
		this.id = id;
		this.location = location;
		this.energon = energon;
		this.shields = shields;
		this.type = type;
		this.active = active;
	}

	@Override
	public int compareTo(Object other) {
		RobotData otherbot = (RobotData)other;
		if(this.id < otherbot.id)
			return 1;
		else if(this.id > otherbot.id)
			return -1;
		else 
			return 0;
	}
}
