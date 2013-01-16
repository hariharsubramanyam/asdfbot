/**
 * constants for state ids and transition ids
 */
package tempPlayer;

public class SMConstants {
	// State ids
	public final static int S_STATE_DEFEND = 0;
	public final static int S_STATE_BUILD = 1;
	public final static int S_STATE_ATTACK = 2;
	
	public final static int H_STATE_MAIN = 100;

	public final static int A_STATE_AUTONOMOUS = 200;
	
	// Return a new state object corresponding to an id
	public static State getState(StateMachine rootSM, int stateVal){
		switch (stateVal){
		case S_STATE_DEFEND: return new SStateDefend((SoldierSM)rootSM); 
		case S_STATE_ATTACK: return new SStateAttack((SoldierSM)rootSM); 
		case H_STATE_MAIN: return new HStateMain(rootSM); 
		case A_STATE_AUTONOMOUS: return new AStateAutonomous(rootSM);
		default: return null;
		}
	}
}
