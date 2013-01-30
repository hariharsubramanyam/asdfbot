package smartNukeExperiment;


import java.util.ArrayList;
import java.util.Collections;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.Clock;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.Upgrade;

public class HDefaultState extends State{
	
	public MapLocation myLocation;
	public MapLocation enemyHQ;
	public Robot[] nearbyRobots;
	public boolean nukeMode;
	private ArrayList<EncampmentLoc> encampmentsToCapture;
	private int lastBroadcast;
	private int encampChannel = 9569;
	private int attackChannel = 9559;
	private int nukeChannel = 39947;
	public boolean nukeHalfDone;
	public boolean enemyNukeHalfDone;
	public boolean setToAttack;
	public MapLocation toGo;
	public MapLocation wayPoint1;
	public MapLocation wayPoint2;
	public MapLocation wayPoint3;
	public int solsWP1;
	public int solsWP2;
	public int solsWP3;
	public int solsEnemyHQ;
	public int timeNuke;
	public int timeEnemyNuke;
	public int nukeChangeDelay;
	
	public HDefaultState(StateMachine rootSM){
		this.stateID = SMConstants.HDEFAULTSTATE;
		this.rootSM = rootSM;
		this.rc = rootSM.getRC();
		this.myLocation = rc.getLocation();
		this.enemyHQ = rc.senseEnemyHQLocation();
		nukeMode = false;
		setEncampmentsToCapture();
		lastBroadcast = 9999999;
		this.nukeHalfDone = false;
		this.enemyNukeHalfDone = false;
		this.setToAttack = false;
		this.toGo = null;
		this.wayPoint1 = new MapLocation((int)(this.myLocation.x*.75+this.enemyHQ.x*.25),(int)(this.myLocation.y*.75+this.enemyHQ.y*.25));
		this.wayPoint2 = new MapLocation((int)(this.myLocation.x*.5+this.enemyHQ.x*.5),(int)(this.myLocation.y*.5+this.enemyHQ.y*.5));
		this.wayPoint3 = new MapLocation((int)(this.myLocation.x*.25+this.enemyHQ.x*.75),(int)(this.myLocation.y*.25+this.enemyHQ.y*.75));
		this.solsWP1 = 0;
		this.solsWP2 = 0;
		this.solsWP3 = 0;
		this.timeNuke = 2500;
		this.timeEnemyNuke = 2501;
		nukeChangeDelay = 10000000;
	}
	
	public void sendCenterOfMassMessage(MapLocation loc){
		int x = loc.x;
		int y = loc.y;
		String msg = "1";
		if(y < 10)
			msg += "00"+y;
		else if(y < 100)
			msg += "0"+y;
		else
			msg += y;
		
		msg += "0";
		
		if(x < 10)
			msg += "00" + x;
		else if(x < 100)
			msg += "0" + x;
		else
			msg += x;
		this.rc.setIndicatorString(0, msg);
		try {
			this.rc.broadcast(PlayerConstants.HQ_CENTER_OF_MASS_CHANNEL, Integer.parseInt(msg));
		} catch (GameActionException e) {e.printStackTrace();}
	}
	
	@Override
	public void doEntryAct() {}

	@Override
	public void doExitAct() {}

	@Override
	public void doAction() {
		try{
			if(rc.readBroadcast(encampChannel) != lastBroadcast){
				lastBroadcast = createNextBroadcast();
				rc.broadcast(encampChannel, lastBroadcast);
			}
			if(!this.nukeHalfDone && rc.checkResearchProgress(Upgrade.NUKE) > 200 && !this.nukeHalfDone){
				this.nukeHalfDone = true;
				this.timeNuke = Clock.getRoundNum();
				rc.setIndicatorString(1, this.timeNuke + " is my nuke timestamp");
			}
			if(!this.enemyNukeHalfDone && rc.senseEnemyNukeHalfDone() == true && !this.enemyNukeHalfDone){
				this.enemyNukeHalfDone = true;
				this.timeEnemyNuke = Clock.getRoundNum();
				rc.setIndicatorString(2, this.timeEnemyNuke + " is Enemy's nuke timestamp");
			}
			if(this.timeEnemyNuke <= this.timeNuke && !this.setToAttack){
				this.setToAttack = true;
				rc.broadcast(this.nukeChannel, 186254);
			}
			if(setToAttack){
				if (Clock.getRoundNum() >= 1000)
					PlayerConstants.NUM_ROBOTS_IN_ATTACK_GROUP = 25;
				if (Clock.getRoundNum() >= 1500)
					PlayerConstants.NUM_ROBOTS_IN_ATTACK_GROUP = 20;
				if (Clock.getRoundNum() >= 2000)
					PlayerConstants.NUM_ROBOTS_IN_ATTACK_GROUP = 15;
				if(this.toGo == null){
					this.toGo = this.wayPoint1;
				}
				else if(this.toGo.equals(this.wayPoint1)){
					Robot[] robsWP1 = rc.senseNearbyGameObjects(Robot.class, this.wayPoint1, 196, rc.getTeam());
					this.solsWP1 = this.numSoldier(robsWP1);
					if(this.solsWP1 >= (int)(0.75*PlayerConstants.NUM_ROBOTS_IN_ATTACK_GROUP))
							this.toGo = this.wayPoint2;
					else
						this.toGo = this.wayPoint1;
				}
				else if(this.toGo.equals(this.wayPoint2)){
					Robot[] robsWP1 = rc.senseNearbyGameObjects(Robot.class, this.wayPoint1, 100, rc.getTeam());
					this.solsWP1 = this.numSoldier(robsWP1);
					Robot[] robsWP2 = rc.senseNearbyGameObjects(Robot.class, this.wayPoint2, 100, rc.getTeam());
					this.solsWP2 = this.numSoldier(robsWP2);
					if(this.solsWP2 >= (int)(PlayerConstants.NUM_ROBOTS_IN_ATTACK_GROUP))
							this.toGo = this.wayPoint3;
/*					else if(this.solsWP2 < (int)(0.75*PlayerConstants.NUM_ROBOTS_IN_ATTACK_GROUP) && this.solsWP1 < (int)(0.25*PlayerConstants.NUM_ROBOTS_IN_ATTACK_GROUP))
						this.toGo = this.wayPoint1;*/
					else
						this.toGo = this.wayPoint2;
				}
				else if(this.toGo.equals(this.wayPoint3)){
					Robot[] robsWP2 = rc.senseNearbyGameObjects(Robot.class, this.wayPoint2, 196, rc.getTeam());
					this.solsWP2 = this.numSoldier(robsWP2);
					Robot[] robsWP3 = rc.senseNearbyGameObjects(Robot.class, this.wayPoint3, 196, rc.getTeam());
					this.solsWP3 = this.numSoldier(robsWP3);
					if(this.solsWP3 >= (int)(0.5*PlayerConstants.NUM_ROBOTS_IN_ATTACK_GROUP))
						this.toGo = this.enemyHQ;
					else if(this.solsWP3 < (int)(0.25*PlayerConstants.NUM_ROBOTS_IN_ATTACK_GROUP)/* && this.solsWP2 < (int)(0.25*PlayerConstants.NUM_ROBOTS_IN_ATTACK_GROUP)*/)
						this.toGo = this.wayPoint2;
					else
						this.toGo = this.wayPoint3;
				}
				else if(this.toGo.equals(this.enemyHQ)){
					Robot[] robsEnemyHQ = rc.senseNearbyGameObjects(Robot.class, this.enemyHQ, 196, rc.getTeam());
					this.solsEnemyHQ = this.numSoldier(robsEnemyHQ);
					Robot[] robsWP3 = rc.senseNearbyGameObjects(Robot.class, this.wayPoint3, 196, rc.getTeam());
					this.solsWP3 = this.numSoldier(robsWP3);
					if(this.solsEnemyHQ < 5/*(int)(0.5*PlayerConstants.NUM_ROBOTS_IN_ATTACK_GROUP) && this.solsWP2 < (int)(0.25*PlayerConstants.NUM_ROBOTS_IN_ATTACK_GROUP)*/)
						this.toGo = this.wayPoint3;
					else
						this.toGo = this.enemyHQ;	
				}
				int toGoLoc = PlayerConstants.mapLocationToInt(this.toGo);
				rc.broadcast(this.attackChannel, toGoLoc);
				if (!rc.hasUpgrade(Upgrade.DEFUSION))
					rc.researchUpgrade(Upgrade.DEFUSION);
				else if(rc.getTeamPower()<100.0 && !rc.hasUpgrade(Upgrade.PICKAXE))
					rc.researchUpgrade(Upgrade.PICKAXE);
				else
					if (rc.getTeamPower() < 100){
						PlayerConstants.NUM_ROBOTS_IN_ATTACK_GROUP = 10;
/*						if (rc.readBroadcast(58621) != 498)
							rc.broadcast(58621, 498);*/
						rc.researchUpgrade(Upgrade.NUKE);
					}
					else{
						if (Clock.getRoundNum() % 100 != 0){
							if (rc.readBroadcast(58621) == 498)
								rc.broadcast(58621, 0);
						}
						else
							if (rc.readBroadcast(58621) != 498)
								rc.broadcast(58621, 498);
						spawnSoldier();
					}
			}
			else{
				int nearbyAllies = rc.senseNearbyGameObjects(Robot.class, rc.getLocation(), 625, rc.getTeam()).length;
				if(!rc.hasUpgrade(Upgrade.PICKAXE) && nearbyAllies == PlayerConstants.NUM_ROBOTS_IN_DEFEND_GROUP){
					rc.researchUpgrade(Upgrade.PICKAXE);
					nukeMode = false;
				}
				else if(nearbyAllies > PlayerConstants.NUM_ROBOTS_IN_ATTACK_GROUP)
					nukeMode = true;
				else
					nukeMode = false;
				if(nukeMode)
					if (rc.hasUpgrade(Upgrade.PICKAXE))
						this.rc.researchUpgrade(Upgrade.NUKE);
					else
						this.rc.researchUpgrade(Upgrade.PICKAXE);
				else if(rc.getTeamPower() < 100)
					this.rc.researchUpgrade(Upgrade.NUKE);
				else
					spawnSoldier();
			}
			
			if(Clock.getRoundNum()==nukeChangeDelay){
				PlayerConstants.NUM_ROBOTS_IN_ATTACK_GROUP = 30;
			}
		}catch(Exception ex){ex.printStackTrace();}
	}
	
	public void spawnSoldier(){
		Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
		int[] directionOffsets = {0,1,-1,2,-2,3,-3,4};
		for(int d : directionOffsets){
			Direction lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
			if (rc.canMove(lookingAtCurrently) && rc.senseMine(rc.getLocation().add(lookingAtCurrently)) == null &&Clock.getRoundNum()>0){
				try {
					rc.spawn(lookingAtCurrently);
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}
	}
	
	public int numSoldier(Robot[] robs){
		int ns = 0;
		RobotInfo robInf;
		for(Robot r : robs){
			try{
				robInf = rc.senseRobotInfo(r);
				if(robInf.type == RobotType.SOLDIER)
					ns++;
			}catch(Exception ex){ex.printStackTrace();}
		}
		return ns;
	}
	
	public void setEncampmentsToCapture(){
		encampmentsToCapture = new ArrayList<EncampmentLoc>();
		try{
			MapLocation enemyHQ = rc.senseEnemyHQLocation();
			double hqDis = myLocation.distanceSquaredTo(enemyHQ);
			MapLocation[] encamps = rc.senseEncampmentSquares(myLocation, (Math.round((float)hqDis/4)), Team.NEUTRAL);
			for(MapLocation e : encamps){
				if (e.distanceSquaredTo(myLocation)>4){
					encampmentsToCapture.add(new EncampmentLoc(e, e.distanceSquaredTo(myLocation)));
				}
			}
			Collections.sort(encampmentsToCapture);
			while(encampmentsToCapture.size()>7){
				encampmentsToCapture.remove(7);
			}
			/*ArrayList<EncampmentLoc> mid = new ArrayList<EncampmentLoc>();
			for(int i=encampmentsToCapture.size()-1; i>=0; i--){
				mid.add(encampmentsToCapture.get(i));
			}*/
			//encampmentsToCapture = mid;
			int artCount = 0;
			int encCount = 0;
			int maxArts = Math.min(6, Math.round((float)encampmentsToCapture.size()/2));
			for (EncampmentLoc e: encampmentsToCapture){
				int locVsEnemyHQ = e.location.distanceSquaredTo(enemyHQ);
				if(e.distanceFromHQ<170 && artCount<maxArts && locVsEnemyHQ<hqDis+8){
					artCount++;
					encCount++;
					e.setType(2);
				}
				else{
					if(encCount%2 == 0){
						e.setType(3);
					}
					else{
						e.setType(4);
					}
					encCount++;
				}
			}
		
		} catch (GameActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int createNextBroadcast(){
		if(this.encampmentsToCapture.isEmpty()){
			return 0;
		}
		else{
			int message = 0;
			EncampmentLoc next = encampmentsToCapture.remove(0);
			message = (next.location.x + next.location.y*1000);
			message += next.type*1000000;
			return message;
		}
	}
	
}
