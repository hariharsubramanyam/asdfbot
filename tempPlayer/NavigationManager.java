package tempPlayer;


import java.util.Collections;
import java.util.LinkedList;
import java.util.PriorityQueue;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class NavigationManager {
	
	public static final int DATA_MANAGER_LATENCY = 10;
	
	SoldierSM sm;
	
	MapLocation currLoc;
	MapLocation targetLoc;
	LinkedList<MapLocation> path;
	
	AStar pathPlanner;
	
	public NavigationManager(SoldierSM sm){
		this.sm = sm;
		if(sm.dataManager == null)
			sm.dataManager.update(false, false, true);
		this.currLoc = sm.dataManager.location;
	}
	
	public void update(){
		this.currLoc = sm.dataManager.location;
	}
	
	public void setTarget(MapLocation target){
		if(this.targetLoc != null)
			if(this.targetLoc.equals(target))
				return;
		
		this.targetLoc = target;
		if(sm.dataManager.enemyMines == null || sm.dataManager.timeSinceLastUpdate() > NavigationManager.DATA_MANAGER_LATENCY)
			sm.dataManager.update(false, false, true);
		this.pathPlanner = new AStar(sm.dataManager.enemyMines,sm.dataManager.allEncampments,sm.dataManager.mapWidth,sm.dataManager.mapHeight);
		this.path = this.pathPlanner.planPath(this.currLoc, this.targetLoc);
	}
	
	public void deleteTarget(){
		this.targetLoc = null;
		this.path.clear();
	}
	
	public void move(){
		if(path.size() == 0){return;}
		//Direction dirToMove = Direction.SOUTH;
		Direction dirToMove = this.sm.dataManager.location.directionTo(path.remove());
		if(this.sm.rc.canMove(dirToMove))
			try {
				this.sm.rc.move(dirToMove);
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else if(this.sm.rc.senseMine(currLoc.add(dirToMove)) == this.sm.dataManager.team)
			try {
				this.sm.rc.defuseMine(currLoc.add(dirToMove));
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else{
			this.setTarget(this.targetLoc);
			this.move();
		}
	}

	public class AStar{
			// Nodes on the open list (ordered by ascending cost)
			public PriorityQueue<GridSquare> open;
			
			// Nodes on the closed list (ordered by ascending cost)
			public PriorityQueue<GridSquare> closed;
			
			public static final int OPEN = 0;
			public static final int MINE = 1;
			public static final int ENCAMPMENT = 2;
			
			public int[][] grid;
			
			// Class for each square
			public class GridSquare implements Comparable<GridSquare>{
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
				
				// gridSquareA.equals(gridSquareB) if they have the same coordinates
				@Override
				public boolean equals(Object other){
					GridSquare o = (GridSquare)other;
					return (this.r == o.r && this.c == o.c);
				}

				public int compareTo(GridSquare o) {
					if(this.costSoFar > o.costSoFar)
						return 1;
					else if (this.costSoFar < o.costSoFar)
						return -1;
					else
						return 0;
				}
			}
			
			public LinkedList<MapLocation> planPath(MapLocation startLoc, MapLocation goalLoc){
				GridSquare start = new GridSquare(null, 0, startLoc.x, startLoc.y);
				GridSquare end = new GridSquare(null,-1,goalLoc.x,goalLoc.y);
				this.open.clear();
				this.closed.clear();
				this.open.add(start);
				
				GridSquare currSquare;
				while(open.size() > 0){
					currSquare = open.remove();
					if(currSquare.r == goalLoc.x && currSquare.c==goalLoc.y)
						return this.gridSquaresToPath(currSquare);
					PriorityQueue<GridSquare> neighbors = this.getNeighbors(currSquare,end);
					for(GridSquare g : neighbors)
						if(!this.open.contains(g))
							if(!this.closed.contains(g))
								this.open.add(g);
						else
							for(GridSquare o : this.open)
								if(o.equals(g)){
									if(o.costSoFar > g.costSoFar){
										o.costSoFar = g.costSoFar;
										o.cameFrom = g.cameFrom;
									}
								}
					this.closed.add(currSquare);
				}
				return null;
			}
			
			private LinkedList<MapLocation> gridSquaresToPath(GridSquare sq){
				LinkedList<MapLocation> path = new LinkedList<MapLocation>();
				while(sq != null){
					path.add(new MapLocation(sq.r,sq.c));
					System.out.println("("+sq.r+","+sq.c+")");
					sq = sq.cameFrom;
				}
				Collections.reverse(path);
				return path;
			}
			
			public AStar(MapLocation[] mineSquares, MapLocation[] encampmentSquares, int mapWidth, int mapHeight){
				this.open = new PriorityQueue<GridSquare>();
				this.closed = new PriorityQueue<GridSquare>();
				
				this.grid = new int[mapWidth][mapHeight];
				for(MapLocation l : mineSquares)
					grid[l.x][l.y] = MINE;
				for(MapLocation l : encampmentSquares)
					grid[l.x][l.y] = ENCAMPMENT;
				
			}
			
			// true if we can move to the square with the given row and column
			public boolean isOpenSquare(int r, int c){
				return (r >= 0 && r < grid.length && c >= 0 && c < grid[0].length && (grid[r][c] == OPEN || grid[r][c] == MINE));
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
							neighbor.costSoFar += (grid[newr][newc] == MINE) ? GameConstants.MINE_DEFUSE_DELAY * 10: 10;	// done because our hueristic works with ints, so it has multiplied everything by 10
							neighbors.add(neighbor);
						}
					}
				return neighbors;
			}
	}
	
	// try to move in the direction of the target
	// if we can't, see what other spaces are open
	// if our path is blocked entirely by mines, defuse the one that lies on the direct path to the target 
	public static void goToLocation(MapLocation place, RobotController rc)
			throws GameActionException {
		int dist = rc.getLocation().distanceSquaredTo(place);
		if(dist > 0){
			int[] directionOffsets = {0,1,-1,2,-2};	// lower magnitude offsets don't change our direction much
			Direction dir = rc.getLocation().directionTo(place);	// get direction straight to target
			Direction firstMine = null;
			boolean hasMoved = false;
			for (int d: directionOffsets){
				// apply the offset to get direction (start with direction straight to target, and change if necessary)
				Direction lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
				if(rc.canMove(lookingAtCurrently)){		// if the path is open, take it
					if(rc.senseMine(rc.getLocation().add(lookingAtCurrently))==null){
						rc.move(lookingAtCurrently);
						hasMoved = true;
						break;
					}
					else if(firstMine == null){	// detect the first mine
						firstMine = Direction.values()[lookingAtCurrently.ordinal()];
					}
				}
			}
			if(!hasMoved){	// if we haven't moved at all, defuse the mine on the most direct path to the target
				if(firstMine != null){
					rc.defuseMine(rc.getLocation().add(firstMine));
				}
			}
		}
	}

}
