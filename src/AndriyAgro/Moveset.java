package AndriyAgro;

import battlecode.common.*;
import scala.Int;

import java.awt.*;

public class Moveset {

    public static void chaseAndAttackEnemyFlagBearer(RobotController rc) throws GameActionException {

        // get all nearby enemies
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(GameConstants.ATTACK_RADIUS_SQUARED, rc.getTeam().opponent());

        // find the lowest flag holder enemy
        RobotInfo lowestFlagBearer = null;
        int lowestFlagBearerHP = GameConstants.DEFAULT_HEALTH + 1;

        for(RobotInfo nearbyEnemy: nearbyEnemies){
            if (nearbyEnemy.hasFlag() && nearbyEnemy.health < lowestFlagBearerHP){
                lowestFlagBearerHP = nearbyEnemy.health;
                lowestFlagBearer = nearbyEnemy;
            }
        }

        // chase flag
        if (lowestFlagBearer != null){
            Direction directionToFlagBearer = rc.getLocation().directionTo(lowestFlagBearer.location);
            if (rc.canMove(directionToFlagBearer)){
                rc.move(directionToFlagBearer);
            }

            if (rc.canAttack(lowestFlagBearer.location)){
                rc.attack(lowestFlagBearer.location);
            }
        }

    }

    public static void chaseAndHealLowestFlagBearer(RobotController rc) throws GameActionException {
        RobotInfo[] nearbyTeammates = rc.senseNearbyRobots(GameConstants.HEAL_RADIUS_SQUARED, rc.getTeam());

        // find the lowest flag holder teammate
        RobotInfo lowestFlagBearer = null;
        int lowestFlagBearerHP = GameConstants.DEFAULT_HEALTH + 1;

        for(RobotInfo nearbyTeammate: nearbyTeammates){
            if (nearbyTeammate.hasFlag() && nearbyTeammate.health < lowestFlagBearerHP){
                lowestFlagBearerHP = nearbyTeammate.health;
                lowestFlagBearer = nearbyTeammate;
            }
        }

        // chase flag
        if (lowestFlagBearer != null){
            Direction directionToFlagBearer = rc.getLocation().directionTo(lowestFlagBearer.location);
            if (rc.canMove(directionToFlagBearer)){
                rc.move(directionToFlagBearer);
            }

            if (rc.canHeal(lowestFlagBearer.location)){
                rc.heal(lowestFlagBearer.location);
            }
        }
    }

    public static void attackLowestDuck(RobotController rc) throws GameActionException {
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(GameConstants.ATTACK_RADIUS_SQUARED, rc.getTeam().opponent());

        // find the lowest flag holder enemy
        RobotInfo lowestEnemy = null;
        int lowestEnemyHP = GameConstants.DEFAULT_HEALTH + 1;

        for(RobotInfo nearbyEnemy: nearbyEnemies){
            if (nearbyEnemy.health < lowestEnemyHP){
                lowestEnemyHP = nearbyEnemy.health;
                lowestEnemy = nearbyEnemy;
            }
        }

        if (lowestEnemy != null){
            if (rc.canAttack(lowestEnemy.location)){
                rc.attack(lowestEnemy.location);
            }
        }

    }
    public static void healLowestDuck(RobotController rc) throws GameActionException {
        RobotInfo[] nearbyTeammates = rc.senseNearbyRobots(GameConstants.HEAL_RADIUS_SQUARED, rc.getTeam());

        // find the lowest flag holder enemy
        RobotInfo lowestTeammate = null;
        int lowestTeammateHP = GameConstants.DEFAULT_HEALTH + 1;

        for(RobotInfo nearbyTeammate: nearbyTeammates){
            if (nearbyTeammate.health < lowestTeammateHP){
                lowestTeammateHP = nearbyTeammate.health;
                lowestTeammate = nearbyTeammate;
            }
        }

        // chase flag
        if (lowestTeammate != null){
            if (rc.canHeal(lowestTeammate.location)){
                rc.heal(lowestTeammate.location);
            }
        }
    }


}
