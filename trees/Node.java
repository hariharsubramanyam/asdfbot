package trees;

import battlecode.common.RobotController;

public class Node {
	public RobotController rc;
	
	public Node(){}
	
	public Node(RobotController rc){
		this.rc = rc;
	}
	
	public boolean run(){
		return true;
	}
}
