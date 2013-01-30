package smartNukeExperiment;

public class SMConstants {
	
	
	public final static int SWAITSTATE = 1;
	public final static int SATTACKSTATE = 2;
	public final static int SBUILDSTATE = 3;
	public final static int SRALLYSTATE = 4;
	
	public final static int HDEFAULTSTATE = 5;
	
	public final static int SROUND200TRANSITION = -1;
	public final static int SNUKETRANSITION = -2;
	public final static int SBUILDNUKETRANSITION = -3;
	public final static int SRALLYTRANSITION = -4;
	public final static int SATTACKGROUPTRANSITION = -5;
	public final static int WAITRALLYTRANSITION = -6;

	public static State getState(StateMachine rootSM, int stateVal){
		switch (stateVal){
		case SWAITSTATE: return new SWaitState(rootSM);
		case SATTACKSTATE: return new SAttackState(rootSM);
		case SBUILDSTATE: return new SBuildState(rootSM);
		case HDEFAULTSTATE: return new HDefaultState(rootSM);
		case SRALLYSTATE: return new SRallyState(rootSM);
		default: return null;
		}
	}
	
	public static Transition getTransition(StateMachine rootSM, int transitionVal){
		switch(transitionVal){
		case SROUND200TRANSITION: return new SRound200Transition(rootSM);
		case SNUKETRANSITION: return new SNukeTransition(rootSM);
		case SATTACKGROUPTRANSITION: return new SAttackGroupFormedTransition(rootSM);
		case WAITRALLYTRANSITION: return new WaitRallyTransition(rootSM);
		default: return null;
		}
	}
	
	public static int[] getTransitionsForState(int stateID){
		switch(stateID){
		case SATTACKSTATE: return new int[]{};
		case SBUILDSTATE: return new int[]{SNUKETRANSITION};
		case SWAITSTATE: return new int[]{SNUKETRANSITION,SROUND200TRANSITION,WAITRALLYTRANSITION};
		case HDEFAULTSTATE: return new int[]{};
		case SRALLYSTATE: return new int[]{SNUKETRANSITION};
		default: return null;
		}
	}
}
