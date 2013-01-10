package controllerapproachTrev;

public interface State {
	
	public void doEntryAct();
	public void doExitAct();
	public void doAction();
	public void checkTransitions();

}
