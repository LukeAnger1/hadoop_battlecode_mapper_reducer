package dev.Moves;

import battlecode.common.*;

import static dev.Moves.Movement.moveAwayFrom;

public class Attack {
    static final int MOVE_AND_ATTACK_RANGE_SQUARED = (int) Math.pow(Math.sqrt(GameConstants.ATTACK_RADIUS_SQUARED) + 1, 2);

    public static void attackWithPriorityTo_Flag_InRange_Lowest(RobotController rc) throws GameActionException {
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());

        MapLocation loc = rc.getLocation();

        RobotInfo lowestFlagHolderInAttackRange = null;
        int lowestFlagHolderInAttackRangeHP = GameConstants.DEFAULT_HEALTH + 1;

        RobotInfo lowestFlagHolderInMoveAndAttackRange = null;
        int lowestFlagHolderInMoveAndAttackRangeHP = GameConstants.DEFAULT_HEALTH + 1;

        RobotInfo lowestInAttackRange = null;
        int lowestInAttackRangeHP = GameConstants.DEFAULT_HEALTH + 1;

        RobotInfo lowestInMoveAndAttackRange = null;
        int lowestInMoveAndAttackRangeHP = GameConstants.DEFAULT_HEALTH + 1;

        // gather info
        for (RobotInfo enemy : nearbyEnemies) {
            MapLocation enemyLoc = enemy.getLocation();
            int distanceSquaredToEnemy = loc.distanceSquaredTo(enemyLoc);
            int enemyHP = enemy.getHealth();

            boolean hasFlag = enemy.hasFlag();
            boolean inAttackRange = distanceSquaredToEnemy <= GameConstants.ATTACK_RADIUS_SQUARED;
            boolean inMoveAndAttackRange = distanceSquaredToEnemy <= MOVE_AND_ATTACK_RANGE_SQUARED && rc.canMove(loc.directionTo(enemyLoc));

            if (hasFlag && inAttackRange) {
                if (enemyHP < lowestFlagHolderInAttackRangeHP) {
                    lowestFlagHolderInAttackRangeHP = enemyHP;
                    lowestFlagHolderInAttackRange = enemy;
                }
            }

            if (hasFlag && inMoveAndAttackRange) {
                if (enemyHP < lowestFlagHolderInMoveAndAttackRangeHP) {
                    lowestFlagHolderInMoveAndAttackRangeHP = enemyHP;
                    lowestFlagHolderInMoveAndAttackRange = enemy;
                }
            }

            if (inAttackRange) {
                if (enemyHP < lowestInAttackRangeHP) {
                    lowestInAttackRangeHP = enemyHP;
                    lowestInAttackRange = enemy;
                }
            }

            if (inMoveAndAttackRange) {
                if (enemyHP < lowestInMoveAndAttackRangeHP) {
                    lowestInMoveAndAttackRangeHP = enemyHP;
                    lowestInMoveAndAttackRange = enemy;
                }
            }


        }

        MapLocation enemyLoc;

        // if there is a flag holder we can hit in range we hit the lowest
        if (lowestFlagHolderInAttackRange != null) {
            enemyLoc = lowestFlagHolderInAttackRange.location;
            if (rc.canAttack(enemyLoc)) {
                rc.attack(enemyLoc);
            }
        }

        // if we can get in range and attack then move to it and attack
        else if (lowestFlagHolderInMoveAndAttackRange != null) {
            enemyLoc = lowestFlagHolderInMoveAndAttackRange.location;
            rc.move(loc.directionTo(enemyLoc));
            if (rc.canAttack(enemyLoc)) {
                rc.attack(enemyLoc);
            }

            // if we are in range of a duck to attack we hit it and kite
        } else if (lowestInAttackRange != null) {
            enemyLoc = lowestInAttackRange.location;
            if (rc.canAttack(enemyLoc)) {
                rc.attack(enemyLoc);
            }
            moveAwayFrom(rc, enemyLoc); // kiting
        }
        // if we are able to move forward and attack then do that
        else if (lowestInMoveAndAttackRange != null) {
            enemyLoc = lowestInMoveAndAttackRange.location;
            rc.move(loc.directionTo(enemyLoc));
            if (rc.canAttack(enemyLoc)) {
                rc.attack(enemyLoc);
            }
        }
    }

    public static void attackLowestInRange(RobotController rc) throws GameActionException {
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(GameConstants.ATTACK_RADIUS_SQUARED, rc.getTeam().opponent());
        MapLocation bestEnemyLoc = null;
        int lowestHealth = Integer.MAX_VALUE;
        for (RobotInfo enemyRobot : enemyRobots) {
            // TODO: may consider specializing in high level healing robots
            int tempRobotHealth = enemyRobot.getHealth();
            MapLocation tempRobotLoc = enemyRobot.getLocation();
            if (tempRobotHealth < lowestHealth && rc.canAttack(tempRobotLoc)) {
                lowestHealth = tempRobotHealth;
                bestEnemyLoc = tempRobotLoc;
            }
        }
        if (bestEnemyLoc != null && rc.canAttack(bestEnemyLoc)) {
            rc.attack(bestEnemyLoc);
        }
    }

    
}
