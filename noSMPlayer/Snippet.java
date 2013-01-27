package noSMPlayer;

public class Snippet {
	public static void main(String[] args) {
		if(myEncamp.length > 0){
			MapLocation enemyHQ = rc.senseEnemyHQLocation();
			int closestDist = 10000000;
			MapLocation closestEncampment = null;
			for(MapLocation ml : myEncamp){
				int dist = ml.distanceSquaredTo(rc.getLocation());
				if(dist < closestDist){
					closestDist = dist;
					closestEncampment = ml;
				}
			}
	}
}

