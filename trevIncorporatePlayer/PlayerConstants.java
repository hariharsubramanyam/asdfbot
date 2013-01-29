package trevIncorporatePlayer;

import battlecode.common.MapLocation;

public class PlayerConstants {
	public static final int NEARBY_ENCAMPMENT_DIST_SQUARED = 10;
	public static final int NEARBY_ALLY_DIST_SQUARED = 196;
	public static final int NEARBY_ENEMY_DIST_SQUARED = 25;
	public static int NUM_ROBOTS_IN_ATTACK_GROUP = 30;
	public static final int NUM_ROBOTS_FOR_RETREAT = 5;
	
	public static final int WITHIN_HQ_RESCUING_RANGE_SQUARED = 400;
	
	// FORMAT: 1yyy0xxx
	public static final int HQ_CENTER_OF_MASS_CHANNEL = 4553;
	public static final int ENCAMPMENT_LOCATION_CHANNEL = 6848;
	public static final int BEING_TAKEN_CHANNEL = 7000;
	public static final int STATE_ASSIGNMENT_CHANNEL = 932;
	public static final int HQ_UNDER_ATTACK_CHANNEL  = 6842;
	public static final int ARTILLERY_IN_SIGHT_MESSAGE = 6853;
	
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
	
	public static int encampmentSquareToInt(EncampmentSquare next){
		int message = 0;
		message = (next.location.x + next.location.y*1000);
		message += next.type*1000000;
		return message;
	}
	
	public static MapLocation intToMapLocation(int loc){
		return new MapLocation(Integer.parseInt((""+loc).substring(5)),Integer.parseInt((""+loc).substring(1,4)));
	}
}
