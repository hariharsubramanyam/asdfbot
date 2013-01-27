package attackStatePlayer;

import battlecode.common.MapLocation;

public class PlayerConstants {
	public static final int NEARBY_ENCAMPMENT_DIST_SQUARED = 10;
	public static final int NEARBY_ALLY_DIST_SQUARED = 196;
	public static final int NEARBY_ENEMY_DIST_SQUARED = 25;
	public static final int NUM_ROBOTS_IN_ATTACK_GROUP = 30;
	public static final int NUM_ROBOTS_FOR_RETREAT = 5;
	
	public static final int WITHIN_HQ_RESCUING_RANGE_SQUARED = 400;
	
	// FORMAT: 1yyy0xxx
	public static final int HQ_CENTER_OF_MASS_CHANNEL = 1337;
	public static final int HQ_UNDER_ATTACK_CHANNEL  = 1234;
	public static final int ARTILLERY_IN_SIGHT_MESSAGE = 2334;
	
	public static int mapLocationToInt(MapLocation loc){
		// FORMAT 1yyy0xxx
		
		int x = loc.x;
		int y = loc.y;
		String msg = "1";
		if(y < 10)
			msg += "00"+y;
		else if(y < 100)
			msg += "0"+y;
		else
			msg += y;
		
		msg += "0";
		
		if(x < 10)
			msg += "00" + x;
		else if(x < 100)
			msg += "0" + x;
		else
			msg += x;
		return Integer.parseInt(msg);
	}

	public static MapLocation intToMapLocation(int loc){
		return new MapLocation(Integer.parseInt((""+loc).substring(5)),Integer.parseInt((""+loc).substring(1,4)));
	}

	
}
