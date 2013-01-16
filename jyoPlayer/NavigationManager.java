package jyoPlayer;

import battlecode.common.*;

import java.util.Collections;
import java.util.PriorityQueue;
import java.util.LinkedList;

public class NavigationManager {
	
	public PriorityQueue<GridSquare> opensquares;
	public LinkedList<MapLocation> closedsquares;
	public LinkedList<MapLocation> pathtotake;
	
	public void goToLocation(MapLocation place, RobotController rc)
			throws GameActionException {
		int dist = 100000000;
		if(dist > 0){
			MapLocation whereiam = rc.getLocation();
			this.closedsquares.add(whereiam);
			dist = whereiam.distanceSquaredTo(place);
			GridSquare currSquare = new GridSquare(null,0,whereiam.x,whereiam.y);
			int[] directionOffsets = {0,1,-1,2,-2,3,-3,4};	// lower magnitude offsets don't change our direction much
			Direction dir = rc.getLocation().directionTo(place);	// get direction straight to target
/*			Direction firstMine = null;
			boolean hasMoved = false;*/
			for (int d: directionOffsets){
				// apply the offset to get direction (start with direction straight to target, and change if necessary)
				Direction lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
				if(rc.canMove(lookingAtCurrently)){	// if the path is open, take it
					if(rc.senseMine(rc.getLocation().add(lookingAtCurrently))==null){
						GridSquare potentialSquare = new GridSquare(currSquare,5*(whereiam.distanceSquaredTo(place)-whereiam.add(lookingAtCurrently).distanceSquaredTo(place)),whereiam.add(lookingAtCurrently).x,whereiam.add(lookingAtCurrently).y);
						this.opensquares.add(potentialSquare);
/*						rc.move(lookingAtCurrently);
						hasMoved = true;
						break;*/
					}
/*					else if(firstMine == null){	// detect the first mine
						firstMine = Direction.values()[lookingAtCurrently.ordinal()];
					}*/
					else{
						GridSquare potentialSquare = new GridSquare(currSquare,whereiam.distanceSquaredTo(place)-whereiam.add(lookingAtCurrently).distanceSquaredTo(place),whereiam.add(lookingAtCurrently).x,whereiam.add(lookingAtCurrently).y);
						this.opensquares.add(potentialSquare);
					}
				}
			}
			for (GridSquare sq: this.opensquares){
				if(!this.closedsquares.contains(new MapLocation(sq.r,sq.c))){
					this.pathtotake.add(new MapLocation(sq.r,sq.c));
				}
			}
			this.opensquares.clear();
			Collections.reverse(pathtotake);
			Direction immediatedir = whereiam.directionTo(pathtotake.remove());
			pathtotake.clear();
			if(rc.senseMine(whereiam.add(immediatedir))==null){
				rc.move(immediatedir);
			}
			else{
				rc.defuseMine(whereiam.add(immediatedir));
			}
			
/*			if(!hasMoved){	// if we haven't moved at all, defuse the mine on the most direct path to the target
				if(firstMine != null){
					rc.defuseMine(rc.getLocation().add(firstMine));
				}
			}*/
		}
	}
}
