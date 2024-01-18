package dev.Moves;

import battlecode.common.*;
import static dev.Communication.onlyOneSymmetry;
import dev.Communication.Symmetry;

import static dev.Parameters.MAX_DIST;

public class Utils {

    public static MapLocation getAverageEnemyLocation(RobotController rc, RobotInfo[] nearbyEnemies) {
        if (nearbyEnemies.length == 0){
            return null;
        }
        int xSum = 0;
        int ySum = 0;

        for (RobotInfo nearbyEnemy: nearbyEnemies){
            xSum += nearbyEnemy.location.x;
            ySum += nearbyEnemy.location.y;
        }

        int xAvg = xSum/ nearbyEnemies.length;
        int yAvg = ySum/ nearbyEnemies.length;

        // get average
        MapLocation avgLoc = new MapLocation(xAvg, yAvg);
        rc.setIndicatorDot(avgLoc, 0,255,0);
        return avgLoc;
    }

    public static MapLocation getAverageTrapLocation(RobotController rc, MapInfo[] mapInfos) throws GameActionException {
        int xSum = 0;
        int ySum = 0;
        int trapCount = 0;
        for (MapInfo mapInfo: mapInfos){
            if(mapInfo.getTrapType() != TrapType.NONE) {
                trapCount++;
                xSum += mapInfo.getMapLocation().x;
                ySum += mapInfo.getMapLocation().y;
            }
        }

        if (trapCount == 0){
            return null;
        }
        int xAvg = xSum/ trapCount;
        int yAvg = ySum/ trapCount;

        // get average
        MapLocation avgLoc = new MapLocation(xAvg, yAvg);
        rc.setIndicatorDot(avgLoc, 0,255,0);
        return avgLoc;
    }

    public static MapLocation[] spawnLocationGroupFinder(RobotController rc) {
        MapLocation[] spawns = new MapLocation[3];
        int locationsFound = 0;


        for (MapLocation loc : rc.getAllySpawnLocations()) {

            // first is always not near any others
            if (locationsFound == 0) {
                spawns[0] = loc;
                locationsFound++;
                continue;
            } else if (locationsFound == 1) {
                if (!loc.isWithinDistanceSquared(spawns[0], 8)) {
                    spawns[1] = loc;
                    locationsFound++;
                    continue;
                }
            } else {
                if (!loc.isWithinDistanceSquared(spawns[0], 8) && !loc.isWithinDistanceSquared(spawns[1], 8)) {
                    spawns[2] = loc;
                    locationsFound++;
                    break;
                }
            }
        }

        return spawns;
    }

    public static int getNumberOfNearbyTeammates(RobotController rc) throws GameActionException {
        return rc.senseNearbyRobots(-1, rc.getTeam()).length;
    }

    public static int getNumberofNearbyEnemies(RobotController rc) throws GameActionException {
        return rc.senseNearbyRobots(-1, rc.getTeam().opponent()).length;
    }

    public static MapLocation getAverageTeammateLocation(RobotController rc, RobotInfo[] nearbyTeammates) {
        if (nearbyTeammates.length == 0){
            return null;
        }
        int xSum = 0;
        int ySum = 0;

        for (RobotInfo nearbyEnemy: nearbyTeammates){
            xSum += nearbyEnemy.location.x;
            ySum += nearbyEnemy.location.y;
        }

        int xAvg = xSum/ nearbyTeammates.length;
        int yAvg = ySum/ nearbyTeammates.length;

        // get average
        MapLocation avgLoc = new MapLocation(xAvg, yAvg);
        rc.setIndicatorDot(avgLoc, 0,255,0);
        return avgLoc;
    }

    // Reflection functions
    public static MapLocation getHorizontalLineReflection(RobotController rc, MapLocation loc) throws GameActionException {
        return new MapLocation(loc.x, rc.getMapHeight() - loc.y - 1);
    }

    public static MapLocation getVerticalLineReflection(RobotController rc, MapLocation loc) throws GameActionException {
        return new MapLocation(rc.getMapWidth() - loc.x - 1, loc.y);
    }

    public static MapLocation getRotationalReflection(RobotController rc, MapLocation loc) throws GameActionException {
        return new MapLocation(rc.getMapWidth() - loc.x - 1, rc.getMapHeight() - loc.y - 1);
    }

    /*
     * Returns the location that is symmetrical to the given location
     * @param rc the robot controller
     * @param loc the location to get the symmetrical location of
     * @return the symmetrical location, rotational if symmetry is unknown
     */
    public static MapLocation getSymmetricalLocation(RobotController rc, MapLocation loc) throws GameActionException {
        Symmetry symmetry = onlyOneSymmetry(rc);
        switch (symmetry) {
            case HORIZONTAL:
                return getHorizontalLineReflection(rc, loc);
            case VERTICAL:
                return getVerticalLineReflection(rc, loc);
            case ROTATIONAL:
                return getRotationalReflection(rc, loc);
            case NOTKNOWN:
                return getRotationalReflection(rc, loc);
        }
        return getRotationalReflection(rc, loc);
    }

    public static RobotInfo getClosestEnemy(RobotController rc) throws GameActionException {
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        RobotInfo closestEnemy = null;
        int minDist = MAX_DIST;
        MapLocation loc = rc.getLocation();
        for (RobotInfo enemy: nearbyEnemies){
            int dist = loc.distanceSquaredTo(enemy.location);
            if (dist < minDist){
                minDist = dist;
                closestEnemy = enemy;
            }
        }
        return closestEnemy;
    }
}
