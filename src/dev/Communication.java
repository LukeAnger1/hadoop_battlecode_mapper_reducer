package dev;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import static dev.Parameters.*;

public strictfp class Communication {
    // Index: what is stored
    // 0: 4 bits nothing | 12 bits enemy spawn location 1
    // 1: 4 bits nothing | 12 bits enemy spawn location 2
    // 2: 4 bits nothing | 12 bits enemy spawn location 3
    // 3: 6 bits nothing | 6 bits duck count
    // 4: 13 bits nothing | 3 bits symmetry (where 3rd bit is horizontal, 2nd bit is vertical, 1st bit is rotational)
    static int horizontalMask = 0b100;
    static int verticalMask = 0b010;
    static int rotationalMask = 0b001;

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
        if (closestLocation != null) System.out.println("Closest enemy spawn location is " + closestLocation);
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
}
