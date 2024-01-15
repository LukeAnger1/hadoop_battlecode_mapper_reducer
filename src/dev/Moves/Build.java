package dev.Moves;

import battlecode.common.*;

import static dev.Moves.Utils.getAverageEnemyLocation;

public class Build {

    public static void fillEverything(RobotController rc) throws GameActionException {
        MapLocation loc = rc.getLocation();
        for (Direction dir : Direction.allDirections()) {
            MapLocation fillLoc = loc.add(dir);
            if (rc.canFill(fillLoc)) {
                rc.fill(fillLoc);
            }
        }

    }

    public static void goToNearbyCrumbsAndFillWater(RobotController rc) throws GameActionException {
        MapLocation[] nearbyCrumbs = rc.senseNearbyCrumbs(-1);
        int closestCrumbDist = 7200;
        MapLocation closestCrumb = null;
        MapLocation loc = rc.getLocation();

        for (MapLocation crumb : nearbyCrumbs) {
            // get direction to crumb and check if we encounter water otw
            Direction dirToCrumb = loc.directionTo(crumb);

            MapLocation squareTowardsCrumbs = loc.add(dirToCrumb);
            MapInfo info = rc.senseMapInfo(squareTowardsCrumbs);
            boolean canFillWater = info.isWater() && rc.canFill(squareTowardsCrumbs);

            if ((canFillWater || rc.canMove(dirToCrumb)) && loc.distanceSquaredTo(crumb) < closestCrumbDist) {
                closestCrumbDist = loc.distanceSquaredTo(crumb);
                closestCrumb = crumb;
            }
        }

        // if there is a crumb try to move to it
        // then try to fill
        if (closestCrumb != null) {
            Direction dir = loc.directionTo(closestCrumb);
            if (rc.canMove(dir)) {
                rc.move(dir);
            } else {
                MapLocation squareToFill = loc.add(dir);
                if (rc.canFill(squareToFill)) {
                    rc.fill(squareToFill);
                }
            }
        }
    }

    public static void buildTrapIfEnoughPlayers(RobotController rc, int enemyThresholdStun, int enemyThresholdBomb) throws GameActionException {

        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (nearbyEnemies.length == 0){
            return;
        }

        MapLocation avgLoc = getAverageEnemyLocation(rc, nearbyEnemies);
        MapLocation loc = rc.getLocation();

        // TODO: consider what we can afford / econ
        TrapType trapType = nearbyEnemies.length > enemyThresholdBomb ? TrapType.EXPLOSIVE : nearbyEnemies.length > enemyThresholdStun ? TrapType.STUN : TrapType.NONE;
        if (trapType == TrapType.NONE) return;

        MapInfo[] possibleBuildSpots = rc.senseNearbyMapInfos(GameConstants.INTERACT_RADIUS_SQUARED);
        MapLocation closestToAvg = null;
        int closestToAvgDist = 7200;

        for (MapInfo buildSpot : possibleBuildSpots){
            MapLocation buildLoc = buildSpot.getMapLocation();
            int dist = buildLoc.distanceSquaredTo(avgLoc);
            if (dist < closestToAvgDist && rc.canBuild(trapType, buildLoc)){
                closestToAvg = buildLoc;
                closestToAvgDist = dist;
            }
        }

        if (closestToAvg != null){
            if (rc.canBuild(trapType, closestToAvg)){
                rc.build(trapType, closestToAvg);
            }
        }
    }

    public static void farmBuildingLevel(RobotController rc, int levelCap) throws GameActionException {
        if (rc.getLevel(SkillType.BUILD) >= levelCap){
            return;
        }
        MapInfo[] nearbyMapInfo = rc.senseNearbyMapInfos(2);
        for (MapInfo mapinfo : nearbyMapInfo){
            MapLocation mapLocation = mapinfo.getMapLocation();
            if (rc.canDig(mapLocation)){
                rc.dig(mapLocation);
                break;
            }
        }
    }
}
