package controllerapproach;

public interface IModule {
	public boolean hasActed();
	public void act();
	public void actDependencies();
	public void endTurn();
	

}
