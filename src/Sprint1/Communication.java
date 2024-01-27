package Sprint1;

import battlecode.common.*;
import static Sprint1.Parameters.*;

public strictfp class Communication {
    // Index: what is stored
    // 0: 4 bits nothing | 12 bits enemy spawn location 1
    // 1: 4 bits nothing | 12 bits enemy spawn location 2
    // 2: 4 bits nothing | 12 bits enemy spawn location 3
    static int horizontalMask = 0b100;
    static int verticalMask = 0b010;
    static int rotationalMask = 0b001;
    // 3: 6 bits nothing | 6 bits duck count
    // 4: 13 bits nothing | 3 bits symmetry (where 3rd bit is horizontal, 2nd bit is vertical, 1st bit is rotational)
    // 5: 3 bits nothing | 12 bits location | 1 bit whether under attack or not
    static int underAttackMask = 0b1;
    static int underAttackLocationMask = 0b1111111111110;
    static int underAttackLocationShift = 1;

    enum Symmetry {
        HORIZONTAL, VERTICAL, ROTATIONAL
    }

    enum Role {
        CAMPER, BUILDER, ATTACKER, HEALER, BASE
    }

    static int[] roleSplit = {CAMPER_ROLE_RATE, BUILDER_ROLE_RATE, ATTACKER_ROLE_RATE, HEALER_ROLE_RATE};

    /* 
    HELPER FUNCTIONS
     */
    public static MapLocation convertIntToMapLocation(RobotController rc, int location) {
        int mapWidth = rc.getMapWidth();
        int x = (location - 1) / mapWidth;
        int y = (location - 1) % mapWidth;
        return new MapLocation(x, y);
    }

    public static int convertMapLocationToInt(RobotController rc, MapLocation location) {
        int x = location.x;
        int y = location.y;
        int mapWidth = rc.getMapWidth();
        return x * mapWidth + y + 1;
    }

    /*
     * COMMUNICATION FUNCTIONS
     */

    public static BaseBot roleSelection(RobotController rc) throws GameActionException {

        int duckCount = rc.readSharedArray(0);
        Role role = Role.ATTACKER;

        // assign role based on split
        int total = 0;
        for (int i = 0; i < roleSplit.length; i++) {
            total += roleSplit[i];
            if (duckCount < total) {
                role = Role.values()[i];
                break;
            }
        }

        // last bot clears out the shared array spot
        if (duckCount == 49){
            rc.writeSharedArray(0, 0);
        }
        else {
            rc.writeSharedArray(0, duckCount + 1);
        }

        switch (role) {
            case CAMPER:
                return new CamperBot(duckCount + 1);
            case HEALER:
                return new HealerBot(duckCount + 1);
            case BUILDER:
                return new BuilderBot(duckCount + 1);
            case ATTACKER:
            default:
                return new AttackerBot(duckCount + 1);
        }
    }

    public static void updateEnemySpawnLocation(RobotController rc, MapLocation location) throws GameActionException {

        int locationInt = convertMapLocationToInt(rc, location);
        // locations are updated in order, so if we find a location that is the same as the one we are trying to update, we can stop
        for (int i = 0; i < 3; i++) {
            int currentLocationInt = rc.readSharedArray(i);
            MapLocation currentLocation = convertIntToMapLocation(rc, currentLocationInt);

            if (currentLocation.distanceSquaredTo(location) <= 2) {
                break;
            }

            if (currentLocationInt == 0) {
                if (rc.canWriteSharedArray(i, currentLocationInt)) {
                    rc.writeSharedArray(i, locationInt);
                }
                break;
            }
        }
    }

    public static MapLocation getClosestEnemySpawnLocation(RobotController rc) throws GameActionException {
        MapLocation closestLocation = null;
        int closestDist = 7200;
        for (int i = 0; i < 3; i++) {
            int location = rc.readSharedArray(i);
            if (location != 0) {
                MapLocation loc = convertIntToMapLocation(rc, location);
                int dist = rc.getLocation().distanceSquaredTo(loc);
                if (dist < closestDist) {
                    closestDist = dist;
                    closestLocation = loc;
                }
            }
        }
        return closestLocation;
    }

    public static MapLocation[] getEnemySpawnLocations(RobotController rc) throws GameActionException {
        MapLocation[] enemySpawnLocations = new MapLocation[3];
        for (int i = 0; i < 3; i++) {
            int location = rc.readSharedArray(i);
            if (location != 0) {
                enemySpawnLocations[i] = convertIntToMapLocation(rc, location);
            }
        }
        return enemySpawnLocations;
    }

    public static void updateSymmetryToFalse(RobotController rc, Symmetry sym) throws GameActionException {
        int symmetry = rc.readSharedArray(4);
        // set the bit to 1 to indicate that symmetry is false
        switch (sym) {
            case HORIZONTAL:
                symmetry |= horizontalMask;
                break;
            case VERTICAL:
                symmetry |= verticalMask;
                break;
            case ROTATIONAL:
                symmetry |= rotationalMask;
                break;
        }
        if (rc.canWriteSharedArray(4, symmetry)) rc.writeSharedArray(4, symmetry);
    }

    public static boolean[] getSymmetry(RobotController rc) throws GameActionException {
        boolean[] symmetry = new boolean[3];
        int symmetryInt = rc.readSharedArray(4);
        symmetry[0] = (symmetryInt & horizontalMask) == 0;
        symmetry[1] = (symmetryInt & verticalMask) == 0;
        symmetry[2] = (symmetryInt & rotationalMask) == 0;
        return symmetry;
    }

    public static boolean underAttack(RobotController rc) throws GameActionException {
        return (rc.readSharedArray(5) & underAttackMask) == 1;
    }

    public static MapLocation getUnderAttackLocation(RobotController rc) throws GameActionException {
        int spawnLocInt = (rc.readSharedArray(5) & underAttackLocationMask) >> underAttackLocationShift;
        return convertIntToMapLocation(rc, spawnLocInt);
    }

    public static void markUnderAttackLocationAsFree(RobotController rc) throws GameActionException {
        MapLocation underAttackLoc = getUnderAttackLocation(rc);
        if (rc.getLocation().isWithinDistanceSquared(underAttackLoc, 4)) {
            RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
            if (enemies.length <= UNDER_ATTACK_ENEMY_THRESHOLD || enemies.length > GIVE_UP_UNDER_ATTACK_THRESHOLD) setNotUnderAttack(rc);
        }
    }

    public static void setNotUnderAttack(RobotController rc) throws GameActionException {
        if (rc.canWriteSharedArray(5, 0)) rc.writeSharedArray(5, 0);
    }

    public static void updateUnderAttackLocation(RobotController rc, MapLocation loc) throws GameActionException {
        int underAttackLocation = (convertMapLocationToInt(rc, loc) << underAttackLocationShift) | 0b1;
        if (rc.canWriteSharedArray(5, underAttackLocation)) {
            rc.writeSharedArray(5, underAttackLocation);
        }
    }
}
