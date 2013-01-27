package noSMPlayer;

import noSMPlayer.ManagerData;
import noSMPlayer.ManagerEnemySensor;
import noSMPlayer.ManagerNavigation;
import battlecode.common.Clock;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class AISoldier extends AIRobot {
	
	public enum State{
		DEFEND,
		BUILD,
		ATTACK
	}
	
	public final static int CLOSE_ENEMY_THRESHOLD = 40;
	
	private ManagerData dataMan;
	private ManagerNavigation navMan;
	private ManagerEnemySensor enemySenseMan;
	private State state;
	
	private MapLocation rallyPoint;
	
	public AISoldier(RobotController rc){
		this.rc = rc;
		dataMan = new ManagerData(rc);
		navMan = new ManagerNavigation(dataMan,rc);
		enemySenseMan = new ManagerEnemySensor(dataMan,rc);
		this.state = State.DEFEND;
		
		this.rallyPoint = getRallyPoint();
	}
	
	public void act(){
		this.checkStateTransitions();
		this.updateManagers();
		if(this.state == State.DEFEND || this.state == State.ATTACK){
			MapLocation closestEnemy = enemySenseMan.getClosestEnemy(AISoldier.CLOSE_ENEMY_THRESHOLD);
			if(closestEnemy != null){
				this.navMan.setGoal(closestEnemy);
				//System.out.println(Clock.getRoundNum() + "-> FOUND CLOSEST ENEMY AT " + closestEnemy);
			}
			else{
				if(this.state == State.ATTACK){
					this.navMan.setGoal(dataMan.getEnemyHQLoc());
					//System.out.println(Clock.getRoundNum() + "-> ATTACKING ENEMY HQ AT " + dataMan.getEnemyHQLoc());
				}
				else
				{
					this.navMan.setGoal(this.rallyPoint);
					//System.out.println(Clock.getRoundNum() + "-> GOING TO RALLY POINT AT " + this.rallyPoint);
				}
			}
			this.navMan.move();
		}
	}
	
	private void updateManagers(){
		dataMan.update(true);
	}
	
	private void checkStateTransitions(){
		if(Clock.getRoundNum() > 200)
			this.state = State.ATTACK;
	}
	
	private MapLocation getRallyPoint(){
		MapLocation hqLoc = dataMan.getHQLoc();
		MapLocation enemyHQLoc = dataMan.getEnemyHQLoc();
		return new MapLocation((int)(0.75*hqLoc.x+0.25*enemyHQLoc.x),(int)(0.75*hqLoc.y+0.25*enemyHQLoc.y));
	}

}
