package noSMPlayer;

import java.util.Collections;
import java.util.ArrayList;
import java.util.PriorityQueue;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class ManagerNavigation {
	
	private ManagerData dataMan;
	private RobotController rc;
	
	private MapLocation goalLoc;
	
	private ArrayList<MapLocation> path;
	
	private static int MOVE_AROUND_OBJECT_TIMEOUT = 10;
	private int timeAvoidingObstacles;
	
	public ManagerNavigation(ManagerData dataMan, RobotController rc){
		this.dataMan = dataMan;
		this.rc = rc;
		this.timeAvoidingObstacles = 0;
		path = null;
		goalLoc = null;
	}

	public void move(){
		if(this.goalLoc == null)
			return;
		
		MapLocation currLoc = this.dataMan.getLocation();
		MapLocation waypoint = this.goalLoc;
		
		this.stepTowardsLocation(waypoint);
	}
	
	private void stepTowardsLocation(MapLocation loc){
		int dist = this.dataMan.getLocation().distanceSquaredTo(loc);
		if(dist > 0){
			int[] directionOffsets = {0,1,-1,2,-2};	// lower magnitude offsets don't change our direction much
			Direction dir = this.dataMan.getLocation().directionTo(loc);	// get direction straight to target
			Direction firstMine = null;
			boolean hasMoved = false;
			for (int d: directionOffsets){
				// apply the offset to get direction (start with direction straight to target, and change if necessary)
				Direction lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
				if(rc.canMove(lookingAtCurrently)){		// if the path is open, take it
					if(rc.senseMine(this.dataMan.getLocation().add(lookingAtCurrently))==null){
						try {
							rc.move(lookingAtCurrently);
						} catch (GameActionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
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
					try {
						rc.defuseMine(rc.getLocation().add(firstMine));
					} catch (GameActionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public void setGoal(MapLocation goal){
		if(goalLoc != null && (goal.x == this.goalLoc.x && goal.y == this.goalLoc.y))
			return;
		
		this.goalLoc = goal;
		//this.path = (new AStar()).getPath(dataMan.getMapWidth(), dataMan.getMapHeight(), dataMan.getLocation(), goalLoc, dataMan.getEnemyMines(), dataMan.getAllEncampments());
		
		//for(MapLocation ml : this.path)
		//	System.out.print(ml + " ");
		//System.out.println();
	}
	
	public class AStar{
		private final static int TRAVERSIBLE = 0, MINE = 1, UNTRAVERSIBLE = 2;
		int grid[][];
		Square goal;
		PriorityQueue<Square> open, closed;
		
		public class Square implements Comparable<Square>{
			public int x, y;
			public double cost;
			public Square parent;
			public Square(int x, int y, double cost, Square parent){this.x=x;this.y=y;this.cost=cost;this.parent=parent;};
			public int compareTo(Square o) {
				if(cost > o.cost)return 1;
				else if(cost < o.cost) return -1;
				else return 0;
			}
			@Override
			public boolean equals(Object ob){
				Square o = (Square)ob;
				return (x == o.x && y == o.y);
			}
			@Override
			public String toString(){return ("["+this.x+","+this.y+"]");}
		}
		
		public ArrayList<MapLocation>getPath(int mapWidth, int mapHeight, MapLocation start, MapLocation goal, MapLocation[] mines, MapLocation[] encampments){
						
			grid = new int[mapWidth][mapHeight];
			for(MapLocation mine : mines) grid[mine.x][mine.y] = AStar.TRAVERSIBLE;
			for(MapLocation camp : encampments) grid[camp.x][camp.y] = AStar.UNTRAVERSIBLE;
			if(!traversible(goal.x,goal.y))
				goal = this.getOpenAdjacentSquare(goal);
			this.goal = this.mapLocToSquare(goal);
			open = new PriorityQueue<Square>();
			closed = new PriorityQueue<Square>();
			
			open.add(this.mapLocToSquare(start));
			
			Square currSquare = null;
			ArrayList<Square> neighbors;
			int sqCon = 0;
			while(!open.isEmpty()){
				sqCon++;
				currSquare = open.remove();
				if(currSquare.equals(this.goal)){
					System.out.println(Clock.getRoundNum() + "-> Squares considered " + sqCon);
					return this.squareToPath(currSquare);
				}
				closed.add(currSquare);
				neighbors = this.getNeighbors(currSquare);
				for(Square n : neighbors)
					if(closed.contains(n))
						continue;
					else if(open.contains(n))
						for(Square o : open){
							if(o.equals(n) && o.cost > n.cost){
								o.parent = currSquare;
								o.cost = n.cost;
							}
						}
					else
						open.add(n);
						
			}
			return new ArrayList<MapLocation>();
		}
		private Square mapLocToSquare(MapLocation mapLoc){return new Square(mapLoc.x,mapLoc.y,0,null);}
		private ArrayList<Square>getNeighbors(Square sq){
			ArrayList<Square> neighbors = new ArrayList<Square>();
			for(int dx = -1; dx <= 1; dx++)
				for(int dy = -1; dy <= 1; dy++)
					if(!(dx == 0 && dy == 0) && traversible(sq.x+dx,sq.y+dy)){
						Square neighbor = new Square(sq.x+dx,sq.y+dy,sq.cost,sq);
						neighbor.cost += (grid[neighbor.x][neighbor.y]==AStar.MINE) ? GameConstants.MINE_DEFUSE_DELAY : 1;
						neighbor.cost += this.hueristic(neighbor);
						neighbors.add(neighbor);
					}
			return neighbors;
				
		}
		private MapLocation getOpenAdjacentSquare(MapLocation loc){
			int x = loc.x;
			int y = loc.y;
			for(int dx = -1; dx <= 1; dx++)
				for(int dy = -1; dy <= 1; dy++)
					if(traversible(x+dx,y+dy))
						return new MapLocation(x+dx,y+dy);
			try{throw new Exception("Could not find open square adjacent to " + loc);}catch(Exception ex){ex.printStackTrace();};
			return null;
		}
		
		private double hueristic(Square sq){
			double xD = Math.abs(sq.x-goal.x);
			double yD = Math.abs(sq.y-goal.y);
			if(xD > yD)
				return Math.sqrt(2.0)*yD + (xD-yD);
			else
				return Math.sqrt(2.0)*xD + (yD-xD);
		}
		private boolean traversible(int x, int y){
			return (x >= 0 && x < grid.length && y >= 0 && y < grid[0].length && grid[x][y] != AStar.UNTRAVERSIBLE);
		}
		private ArrayList<MapLocation> squareToPath(Square sq){
			ArrayList<MapLocation> path = new ArrayList<MapLocation>();
			while(sq != null){
				path.add(new MapLocation(sq.x,sq.y));
				sq = sq.parent;
			}
			Collections.reverse(path);
			return path;
		}
	}
	
	
}
