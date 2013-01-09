package trees;
import java.util.PriorityQueue;

public class AStarPathFinder {
	
	// Nodes on the open list (ordered by ascending cost)
	public PriorityQueue<GridSquare> open;
	
	// Nodes on the closed list (ordered by ascending cost)
	public PriorityQueue<GridSquare> closed;
	
	// Class for each square
	public class GridSquare implements Comparable{
		public int costSoFar;		// recorded cost of node
		public int r;				// row (x-coordinate) of square
		public int c;				// column (y-coordinate) of square
		public GridSquare cameFrom; // grid square that brought us to this grid square
		
		public GridSquare(GridSquare cameFrom, int costSoFar, int r, int c){
			this.cameFrom = cameFrom;
			this.costSoFar = costSoFar;
			this.r = r;
			this.c = c;
		}
		
		// gridSquareA > gridSquareB compares their costSoFar
		@Override
		public int compareTo(Object other) {
			GridSquare o = (GridSquare)other;
			if(this.costSoFar > o.costSoFar)
				return 1;
			else if (this.costSoFar < o.costSoFar)
				return -1;
			else
				return 0;
		}
		
		// gridSquareA.equals(gridSquareB) if they have the same coordinates
		@Override
		public boolean equals(Object other){
			GridSquare o = (GridSquare)other;
			return (this.r == o.r && this.c == o.c);
		}
	}
	
	public AStarPathFinder(){
		this.open = new PriorityQueue<GridSquare>();
		this.closed = new PriorityQueue<GridSquare>();
	}
	
	// true if we can move to the square with the given row and column
	public boolean isOpenSquare(int r, int c){
		return true;
	}
	
	// estimated cost of getting from current square to goal square
	public int hueristic(GridSquare current, GridSquare goal){
		int xD = Math.abs(current.r - goal.r);
		int yD = Math.abs(current.r - goal.c);
		if(xD > yD)
			return 14*yD + 10*(xD-yD);
		else
			return 14*xD + 10*(yD-xD);
	}
	
	// returns all open neighbors of a given square ordered by ascending cost
	public PriorityQueue<GridSquare> getNeighbors(GridSquare current, GridSquare goal){
		int newr,newc;
		
		PriorityQueue<GridSquare> neighbors = new PriorityQueue<GridSquare>();
		// iterate through all 8 directions
		for(int dr = -1; dr <= 1; dr++)
			for(int dc = -1; dc <= 1; dc++){
				
				if(dr == 0 && dc == 0){continue;}	// don't travel from this node to itself
				
				newr = current.r + dr;
				newc = current.c + dc;
				
				// check if the neighbor can be reached
				if(this.isOpenSquare(newr, newc)){
					
					// initialize the neighbor with our current node as the parent and the costSoFar as the parrent
					GridSquare neighbor = new GridSquare(current,current.costSoFar,newr,newc);
					neighbor.costSoFar += this.hueristic(neighbor, goal);
					neighbors.add(neighbor);
				}
			}
		return neighbors;
	}

	public GridSquare pathfind(GridSquare start, GridSquare goal){
		
	}
}
