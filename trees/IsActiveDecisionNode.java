package trees;

public class IsActiveDecisionNode extends DecisionNode{
	public boolean tc(){
		return this.rc.isActive();
	}
}
