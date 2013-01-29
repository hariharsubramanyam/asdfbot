package smartNuke;

import battlecode.common.*;

public class EncampmentLoc implements Comparable {
	
	public MapLocation location;
	public int distanceFromHQ;
	public int type;
	
	public EncampmentLoc(MapLocation l, int d) {
		location = l;
		distanceFromHQ = d;
		type = 9;
	}
	
	public void setType(int t){
		type = t;
	}
	
	public int compareTo(EncampmentLoc other){
		if(other.distanceFromHQ>distanceFromHQ){
			return -1;
		}
		else if(other.distanceFromHQ<distanceFromHQ){
			return 1;
		}
		else{
			return 0;
		}
	}

	@Override
	public int compareTo(Object other) {
		if (!other.getClass().equals(this.getClass())){
			return 1;
		}
		else{
			if(((EncampmentLoc)other).distanceFromHQ>distanceFromHQ){
				return -1;
			}
			else if(((EncampmentLoc)other).distanceFromHQ<distanceFromHQ){
				return 1;
			}
			else{
				return 0;
			}
		}
	}

}
