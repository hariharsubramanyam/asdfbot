package trees;

import battlecode.common.RobotType;

public class IsSoldierDecisionNode extends DecisionNode{
	public boolean tc(){
		return (this.rc.getType() == RobotType.SOLDIER);
	}
}
