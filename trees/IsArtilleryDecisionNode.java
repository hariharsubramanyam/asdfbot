package trees;

import battlecode.common.RobotType;

public class IsArtilleryDecisionNode extends DecisionNode{
	public boolean tc(){
		return (this.rc.getType() == RobotType.ARTILLERY);
	}
}
