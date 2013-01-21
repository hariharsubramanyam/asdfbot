package attackStatePlayer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.Clock;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;

public class HDefaultState extends State{

	public Robot[] nearbyRobots;
	public double previousEnergon;
	public boolean underAttack;
	public int valOnArtilleryChannel;
	
	public HDefaultState(StateMachine rootSM){
		this.stateID = SMConstants.SATTACKSTATE;
		this.rootSM = rootSM;
		this.rc = rootSM.getRC();
	}
	
	public void setUnderAttackMessage(boolean underAttack){
		try{
		if(underAttack)
			this.rc.broadcast(PlayerConstants.HQ_UNDER_ATTACK_CHANNEL,1);
		else
			this.rc.broadcast(PlayerConstants.HQ_UNDER_ATTACK_CHANNEL, 0);
		}catch(Exception ex){ex.printStackTrace();}
	}
	
	public void sendEnemyArtilleryInRangeMessage(int loc){
		try{this.rc.broadcast(PlayerConstants.ARTILLERY_IN_SIGHT_MESSAGE,loc);}catch(Exception ex){ex.printStackTrace();}
	}
	
	@Override
	public void doEntryAct() {
		this.underAttack = false;
		this.previousEnergon = RobotType.HQ.maxEnergon;
		this.valOnArtilleryChannel = 0;
	}

	@Override
	public void doExitAct() {}

	public MapLocation enemyArtilleryInShootingRange(){
		int rangeBuffer = 100; // sense out sqrt(rangeBuffer) squares beyond artillery range 
		Robot[] robs = this.rc.senseNearbyGameObjects(Robot.class, rc.getLocation(), RobotType.ARTILLERY.attackRadiusMaxSquared+rangeBuffer, rc.getTeam().opponent());
		RobotInfo robInf;
		for(Robot r : robs){
			if(r == null)
				continue;
			try {
				robInf = rc.senseRobotInfo(r);
				if(robInf.type == RobotType.ARTILLERY)
					return robInf.location;
			} catch (GameActionException ex) {ex.printStackTrace();}
		}
		return null;
	}
	@Override
	public void doAction() {
		try{
			if(this.rc.isActive()){
//				MapLocation teamCM = this.getTeamCenterOfMass();
//				this.rc.setIndicatorString(0, teamCM.toString());
//				if(teamCM != null)
//					this.sendCenterOfMassMessage(teamCM);
				if(this.previousEnergon > this.rc.getEnergon() && !this.underAttack){
					if(Clock.getRoundNum() < GameConstants.ROUND_MIN_LIMIT){
						this.setUnderAttackMessage(true);
						this.underAttack = true;
					}
				}
				else if(this.previousEnergon == this.rc.getEnergon() && this.underAttack){
					this.rc.setIndicatorString(0, "UNDER ATTACK!");
					this.underAttack = false;
					this.setUnderAttackMessage(false);
				}
				this.previousEnergon = this.rc.getEnergon();
				
				if(!this.underAttack){
					MapLocation artilleryLoc = this.enemyArtilleryInShootingRange();
					int convertedArtLoc = 0;
					if(artilleryLoc != null)
						convertedArtLoc = PlayerConstants.mapLocationToInt(artilleryLoc);
					
					if(this.valOnArtilleryChannel != convertedArtLoc){
						this.valOnArtilleryChannel = convertedArtLoc;
						this.sendEnemyArtilleryInRangeMessage(convertedArtLoc);
					}
				}
				
				Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
				int[] directionOffsets = {0,1,-1,2,-2,3,-3,4};
				for(int d : directionOffsets){
					Direction lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
					Team teamOfMine = rc.senseMine(rc.getLocation().add(lookingAtCurrently));
					if (rc.canMove(lookingAtCurrently) && Clock.getRoundNum()>0 && (teamOfMine == null || teamOfMine == rc.getTeam())){
						rc.spawn(lookingAtCurrently);
						break;
					}
				}
			}
		}catch(Exception ex){ex.printStackTrace();}
	}
	
	public MapLocation getTeamCenterOfMass(){
		this.nearbyRobots = this.rc.senseNearbyGameObjects(Robot.class);
		if(this.nearbyRobots == null)
			return this.rc.getLocation();
		int x = 0, y = 0;
		int count = 0;
		MapLocation robotLoc;
		for(Robot r : this.nearbyRobots)
			if(r != null && r.getTeam() == this.rc.getTeam()){
				try {
					robotLoc = this.rc.senseRobotInfo(r).location;
					x += robotLoc.x;
					y += robotLoc.y;
					count++;
				} catch (GameActionException e) {e.printStackTrace();}
			}
		if(x == 0 && y == 0)
			return this.rc.getLocation();
		MapLocation res = new MapLocation((int)(1.0*x/count),(int)(1.0*y/count));
		return res;
	}
	
}
