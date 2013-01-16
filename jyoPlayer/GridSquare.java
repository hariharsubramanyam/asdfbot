package jyoPlayer;

public class GridSquare implements Comparable<GridSquare>{

	public int distCovered;		// distance reduced to Target
	public int r;				// row (x-coordinate) of square
	public int c;				// column (y-coordinate) of square
	public GridSquare cameFrom; // grid square that brought us to this grid square

	public GridSquare(GridSquare cameFrom, int distCovered, int r, int c){
		this.cameFrom = cameFrom;
		this.distCovered = distCovered;
		this.r = r;
		this.c = c;
	}

	public boolean equals(Object other){
		GridSquare o = (GridSquare)other;
		return (this.r == o.r && this.c == o.c);
	}

	public int compareTo(GridSquare o) {
		if(this.distCovered > o.distCovered)
			return -1;
		else if (this.distCovered < o.distCovered)
			return 1;
		else
			return 0;
	}
}
