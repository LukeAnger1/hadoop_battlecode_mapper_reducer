package L;

import battlecode.common.*;
import static L.RobotPlayer.*;
import static L.HealBot.*;
import static L.Nav.*;
import static L.FlagBot.*;

import java.util.HashSet;

public class AttackerBot {
    
    public static void runAttacker(RobotController rc) throws GameActionException {
        boolean haveEnemyFlag = rc.hasFlag() && rc.getRoundNum() >= GameConstants.SETUP_ROUNDS;
        
        if (haveEnemyFlag){
            Nav.navigateTo(rc, getClosestSpawnLocation(rc));
        }
        else{
            FlagBot.goToAndPickUpEnemyFlag(rc);
        }

        if (rc.isActionReady()) {
            attackEnemies(rc);
        }

        if (rc.isActionReady()) {
            HealBot.healAllies(rc);
        }

        Nav.moveRandomly(rc);

        updateSymmetry(rc);
    }

    public static void attackEnemies(RobotController rc) throws GameActionException{
        MapLocation enemyLoc = getBestEnemyAttack(rc);
        if (enemyLoc != null) {
            rc.attack(enemyLoc);
            // System.out.println("Attacked an enemy!");
        }
    }

    public static MapLocation getBestEnemyAttack(RobotController rc) throws GameActionException{
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(GameConstants.ATTACK_RADIUS_SQUARED, rc.getTeam().opponent());
        MapLocation bestEnemyLoc = null;
        int lowestHealth = Integer.MAX_VALUE;
        for (int i = 0; i < enemyRobots.length; i++){
            // TODO: may consider specializing in high level healing robots
            int tempRobotHealth = enemyRobots[i].getHealth();
            MapLocation tempRobotLoc = enemyRobots[i].getLocation();
            if (tempRobotHealth < lowestHealth && rc.canAttack(tempRobotLoc)){
                lowestHealth = tempRobotHealth;
                bestEnemyLoc = tempRobotLoc;
            }
        }
        return bestEnemyLoc;
    }
}
