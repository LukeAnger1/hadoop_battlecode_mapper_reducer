package dev.Moves;

import battlecode.common.*;

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
}
