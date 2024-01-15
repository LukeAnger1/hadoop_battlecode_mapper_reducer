package L;

import battlecode.common.*;
import static L.RobotPlayer.*;
import java.util.HashSet;

public class HealBot {
    public static void healAllies(RobotController rc) throws GameActionException{
        MapLocation allyLoc = getBestAllyHeal(rc);
        if (allyLoc != null) {
            rc.heal(allyLoc);
            // System.out.println("Attacked an enemy!");
        }
    }

    public static MapLocation getBestAllyHeal(RobotController rc) throws GameActionException{
        RobotInfo[] allyRobots = rc.senseNearbyRobots(GameConstants.HEAL_RADIUS_SQUARED, RobotPlayer.ourTeam);
        MapLocation bestAllyLoc = null;
        int lowestHealth = Integer.MAX_VALUE;
        for (int i = 0; i < allyRobots.length; i++){
            // TODO: consider healing flag carrying bots, but may be better to just drop flag
            int tempRobotHealth = allyRobots[i].getHealth();
            MapLocation tempRobotLoc = allyRobots[i].getLocation();
            if (tempRobotHealth < lowestHealth && rc.canHeal(tempRobotLoc)){
                lowestHealth = tempRobotHealth;
                bestAllyLoc = tempRobotLoc;
            }
        }
        return bestAllyLoc;
    }
}
