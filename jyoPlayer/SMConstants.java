package jyoPlayer;

public class SMConstants {
	
	
	public final static int SWAITSTATE = 1;
	public final static int SATTACKSTATE = 2;
	public final static int SBUILDSTATE = 3;
	
	public final static int HDEFAULTSTATE = 4;
	
	public final static int SROUND200TRANSITION = -1;
	public final static int SNUKETRANSITION = -2;
	public final static int SBUILDNUKETRANSITION = -3;
	public final static int SRALLYTRANSITION = -4;

	public static State getState(StateMachine rootSM, int stateVal){
		switch (stateVal){
		case SWAITSTATE: return new SWaitState(rootSM);
		case SATTACKSTATE: return new SAttackState(rootSM);
		case SBUILDSTATE: return new SBuildState(rootSM);
		case HDEFAULTSTATE: return new HDefaultState(rootSM);
		default: return null;
		}
	}
	
	public static Transition getTransition(StateMachine rootSM, int transitionVal){
		switch(transitionVal){
		case SROUND200TRANSITION: return new SRound200Transition(rootSM);
		case SNUKETRANSITION: return new SNukeTransition(rootSM);
		case SBUILDNUKETRANSITION: return new SBuildNukeTransition(rootSM);
		case SRALLYTRANSITION: return new SRallyTransition(rootSM);
		default: return null;
		}
	}
	
	public static int[] getTransitionsForState(int stateID){
/*		return new int[] {SROUND200TRANSITION,SNUKETRANSITION,SBUILDNUKETRANSITION,SRALLYTRANSITION};
*/		switch(stateID){
		case SWAITSTATE: return new int[]{SROUND200TRANSITION,SNUKETRANSITION};
		case SATTACKSTATE: return new int[]{};
		case SBUILDSTATE: return new int[]{SBUILDNUKETRANSITION};
		case HDEFAULTSTATE: return new int[]{};
		default: return null;
		}
	}
}
