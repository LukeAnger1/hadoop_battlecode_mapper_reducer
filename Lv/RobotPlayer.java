package Lv;

import battlecode.common.*;

import java.util.Random;

public strictfp class RobotPlayer {
    static int turnCount = 0;
    static final Random rng = new Random(6147);
    static final Direction[] directions = Direction.values(); // Simplified direction array

    public static void run(RobotController rc) throws GameActionException {
        System.out.println("I'm alive");
        rc.setIndicatorString("Hello world!");

        while (true) {
            turnCount++;
            try {
                if (!rc.isSpawned()) {
                    attemptSpawn(rc);
                } else {
                    performActions(rc);
                }
            } catch (GameActionException e) {
                System.out.println("GameActionException");
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("Exception");
                e.printStackTrace();
            } finally {
                Clock.yield();
            }
        }
    }

    private static void attemptSpawn(RobotController rc) throws GameActionException {
        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
        MapLocation randomLoc = spawnLocs[rng.nextInt(spawnLocs.length)];
        if (rc.canSpawn(randomLoc)) rc.spawn(randomLoc);
    }

    private static void performActions(RobotController rc) throws GameActionException {
        handleFlagActions(rc);
        if (rc.hasFlag() && rc.getRoundNum() >= GameConstants.SETUP_ROUNDS) {
            returnToSpawn(rc);
        } else {
            randomMoveAndAttack(rc);
            occasionallyPlaceTrap(rc);
            updateEnemyRobots(rc);
        }
    }

    private static void handleFlagActions(RobotController rc) throws GameActionException {
        if (rc.canPickupFlag(rc.getLocation())) {
            rc.pickupFlag(rc.getLocation());
            rc.setIndicatorString("Holding a flag!");
        }
    }

    private static void returnToSpawn(RobotController rc) throws GameActionException {
        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
        MapLocation nearestSpawn = findNearestLocation(rc.getLocation(), spawnLocs);
        Direction dir = rc.getLocation().directionTo(nearestSpawn);
        if (rc.canMove(dir)) rc.move(dir);
    }

    private static void randomMoveAndAttack(RobotController rc) throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        if (rc.canMove(dir)) {
            rc.move(dir);
        } else if (rc.canAttack(nextLoc)) {
            rc.attack(nextLoc);
            System.out.println("Attacking an enemy!");
        }
    }

    private static void occasionallyPlaceTrap(RobotController rc) throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation prevLoc = rc.getLocation().subtract(dir);
        if (rc.canBuild(TrapType.EXPLOSIVE, prevLoc) && rng.nextDouble() < 0.05) { // 5% chance
            rc.build(TrapType.EXPLOSIVE, prevLoc);
        }
    }

    private static void updateEnemyRobots(RobotController rc) throws GameActionException {
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemyRobots.length > 0) {
            rc.setIndicatorString("Detected enemy robots!");
            rc.writeSharedArray(0, enemyRobots.length);
        }
    }

    private static MapLocation findNearestLocation(MapLocation current, MapLocation[] locations) {
        MapLocation closest = null;
        int minDistance = Integer.MAX_VALUE;
        for (MapLocation loc : locations) {
            int distance = current.distanceSquaredTo(loc);
            if (distance < minDistance) {
                minDistance = distance;
                closest = loc;
            }
        }
        return closest;
    }
}
