package smartNuke;

import battlecode.common.MapLocation;

public class EncampmentSquare implements Comparable<EncampmentSquare>{
	public int distance;		// distance to square
	public MapLocation location;
	public int type;
	
	public EncampmentSquare(int distance, MapLocation mL){
		this.distance = distance;
		this.location = mL;
		this.type = 9;
	}
	
	public void setType(int t){
		this.type = t;
	}
	
	// gridSquareA.equals(gridSquareB) if they have the same coordinates
	@Override
	public boolean equals(Object other){
		EncampmentSquare o = (EncampmentSquare)other;
		return (this.location == o.location);
	}

	public int compareTo(EncampmentSquare o) {
		if(this.distance > o.distance)
			return 1;
		else if (this.distance < o.distance)
			return -1;
		else
			return 0;
	}
}