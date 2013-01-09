package trees;

import battlecode.common.RobotType;

public class IsHQDecisionNode extends DecisionNode {
	public boolean tc(){
		return (this.rc.getType() == RobotType.HQ);
	}
}
