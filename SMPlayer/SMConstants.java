package SMPlayer;

public class SMConstants {
	public final static int SWAITSTATE = 1;
	public final static int SATTACKSTATE = 2;
	
	public final static int SROUND200TRANSITION = -1;

	public static State getState(StateMachine rootSM, int stateVal){
		switch (stateVal){
		case SWAITSTATE: return new SWaitState(rootSM);
		case SATTACKSTATE: return new SAttackState(rootSM);
		default: return null;
		}
	}
	
	public static Transition getTransition(StateMachine rootSM, int transitionVal){
		switch(transitionVal){
		case SROUND200TRANSITION: return new SRound200Transition(rootSM);
		default: return null;
		}
	}
	
	public static int[] getTransitionsForState(int stateID){
		switch(stateID){
		case SWAITSTATE: return new int[]{SROUND200TRANSITION};
		case SATTACKSTATE: return new int[]{};
		default: return null;
		}
	}
}
