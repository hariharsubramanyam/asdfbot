package trees;

public class WaitNode extends Node{
	public boolean run(){
		this.rc.yield();
		return true;
	}
}
