package team092;


import java.util.ArrayList;
import java.util.Collections;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.Clock;
import battlecode.common.Team;
import battlecode.common.Upgrade;

public class HDefaultState extends State{
	
	public MapLocation myLocation;
	public MapLocation enemyHQ;
	public Robot[] nearbyRobots;
	public boolean nukeMode;
	private ArrayList<EncampmentLoc> encampmentsToCapture;
	private int lastBroadcast;
	private int encampChannel = 9553;
	private int nukeChannel = 39842;
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
		this.wayPoint1 = new MapLocation((int)(this.myLocation.x*.75+this.enemyHQ.x*.25),(int)(this.myLocation.y*.75+this.enemyHQ.y*.25));
		this.wayPoint2 = new MapLocation((int)(this.myLocation.x*.5+this.enemyHQ.x*.5),(int)(this.myLocation.y*.5+this.enemyHQ.y*.5));
		this.wayPoint3 = new MapLocation((int)(this.myLocation.x*.25+this.enemyHQ.x*.75),(int)(this.myLocation.y*.25+this.enemyHQ.y*.75));

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
			if(!this.nukeHalfDone && rc.checkResearchProgress(Upgrade.NUKE) >= 200)
				this.nukeHalfDone = true;
			if(!this.enemyNukeHalfDone && rc.senseEnemyNukeHalfDone() == true)
				this.enemyNukeHalfDone = true;
			if(this.enemyNukeHalfDone && !this.nukeHalfDone){
				this.setToAttack = true;
				rc.broadcast(this.nukeChannel, 186254);
			}
			if(setToAttack){
				if (!rc.hasUpgrade(Upgrade.DEFUSION))
					rc.researchUpgrade(Upgrade.DEFUSION);
				else if(rc.getTeamPower()<100.0 && !rc.hasUpgrade(Upgrade.PICKAXE))
					rc.researchUpgrade(Upgrade.PICKAXE);
				else
					if (rc.getTeamPower() < 100){
						if (rc.readBroadcast(58621) != 498)
							rc.broadcast(58621, 498);
						rc.researchUpgrade(Upgrade.NUKE);
					}
					else{
						if (Clock.getRoundNum() % 150 != 0){
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
				int nearbyAllies = rc.senseNearbyGameObjects(Robot.class, rc.getLocation(), 192, rc.getTeam()).length;
				if(!rc.hasUpgrade(Upgrade.PICKAXE) && nearbyAllies == PlayerConstants.NUM_ROBOTS_IN_DEFEND_GROUP){
					rc.researchUpgrade(Upgrade.PICKAXE);
					nukeMode = false;
				}
				else if(Clock.getRoundNum() > 2000 && Math.min(2500 - Clock.getRoundNum(), (int)(rc.getEnergon())) + 100 > 400 - rc.checkResearchProgress(Upgrade.NUKE))
					PlayerConstants.NUM_ROBOTS_IN_ATTACK_GROUP = 10;
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
		}catch(Exception ex){ex.printStackTrace();}
	}
	
	public void spawnSoldier(){
		Direction dir = this.myLocation.directionTo(enemyHQ);
		int[] directionOffsets = {0,1,-1,2,-2,3,-3,4};
		for(int d : directionOffsets){
			Direction lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
			if (rc.canMove(lookingAtCurrently) && rc.senseMine(this.myLocation.add(lookingAtCurrently)) == null &&Clock.getRoundNum()>0){
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
