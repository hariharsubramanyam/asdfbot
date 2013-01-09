package trees;

import battlecode.common.RobotController;

public class DecisionNode extends Node {
	
	public Node trueNode;
	public Node falseNode;
	
	public DecisionNode(){}
	
	public DecisionNode(RobotController rc, Node trueNode, Node falseNode){
		this.rc = rc;
		this.trueNode = trueNode;
		this.falseNode = falseNode;
	}
	
	public boolean run(){
		if(tc())
			return this.trueNode.run();
		else
			return this.falseNode.run();
	}
	
	// Override this
	public boolean tc(){
		if(Math.random() < 0.5)
			return true;
		else
			return false;
	}
}
