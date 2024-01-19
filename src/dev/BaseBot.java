package dev;

import battlecode.common.*;
import dev.Communication.Role;

import java.util.HashSet;

import static dev.Communication.getUnderAttackLocation;
import static dev.Communication.underAttack;
import static dev.Moves.Attack.attackWithPriorityTo_Flag_InRange_Lowest;
import static dev.Moves.Build.fillLattice;
import static dev.Moves.Build.goToNearbyCrumbsAndFillWater;
import static dev.Moves.Heal.healWithPriorityTo_Flag_InRange_Lowest;
import static dev.Moves.Utils.getHorizontalLineReflection;
import static dev.Moves.Utils.getRotationalReflection;
import static dev.Moves.Utils.getVerticalLineReflection;
import static dev.Moves.Utils.spawnLocationGroupFinder;
import static dev.Moves.Movement.moveRandomParticle;
import static dev.RobotPlayer.directions;
import static dev.RobotPlayer.rng;
import static dev.RobotPlayer.BFSSink;

public class BaseBot {


    static MapLocation spawnLocation;
    static MapLocation oppositeOfSpawnLocation;
    static MapLocation horizontalMirrorLocation;
    static MapLocation verticalMirrorLocation;
    // mirrors[0] = horizontal (line), mirrors[1] = vertical (line), mirrors[2] = rotational
    // To clarify, vertical being true means that the map is symmetric across a vertical line!!!!!!!!!!!!
    static boolean[] mirrors = {true, true, true};
    static MapLocation lastLoc;
    // hashset of visited enemy locations
    static HashSet<MapLocation> visitedEnemyLocations = new HashSet<MapLocation>();
    static MapLocation ignoreLocation = null;
    static MapLocation[] enemySpawnLocations = {null, null, null};

    static Direction currentDirection = directions[rng.nextInt(directions.length)];

    public int duckNumber = 0;

    static public MapLocation[] spawns = null;

    public void firstTurn(RobotController rc) throws GameActionException {
        firstSpawn(rc, duckNumber);

        // move off of spawn if on edge
        if (rc.isSpawned()) {
            for (Direction dir : directions) {
                MapLocation loc = rc.getLocation().add(dir);
                if (rc.canMove(dir) && !rc.senseMapInfo(loc).isSpawnZone()) {
                    rc.move(dir);
                }
            }

            // move off of flag tile
            FlagInfo[] flags = rc.senseNearbyFlags(1);
            for (FlagInfo flagInfo : flags) {
                if (flagInfo.getLocation() == rc.getLocation()) {
                    for (Direction dir : directions) {
                        if (rc.canMove(dir)) {
                            rc.move(dir);
                        }
                    }
                }
            }

            updateSymmetry(rc);
        }
    }

    public void setupTurn(RobotController rc) throws GameActionException {
        if (!rc.isSpawned()) {
            spawn(rc, duckNumber);
        }

        if (rc.isSpawned()) {
            goToNearbyCrumbsAndFillWater(rc);
            fillLattice(rc);
            updateSymmetry(rc);
			moveRandomParticle(rc);
        }
    }

    public void move(RobotController rc) throws GameActionException {
        if (!rc.isSpawned()) {
            spawn(rc, duckNumber);
        }
        if (rc.isSpawned()) {
            updateSymmetry(rc);
            attackWithPriorityTo_Flag_InRange_Lowest(rc);
            healWithPriorityTo_Flag_InRange_Lowest(rc);
        }
    }

    public static void navigateTo(RobotController rc, MapLocation loc) throws GameActionException {
//        // Use the comments above to implement bug navigation
//        MapLocation currentLoc = rc.getLocation();
//        rc.setIndicatorString("Navigating to " + loc.toString());
//        while (!currentLoc.equals(loc) && rc.isMovementReady()) {
//            Direction dir = currentLoc.directionTo(loc);
//            if (rc.canMove(dir)) {
//                rc.move(dir);
//            } else {
//                // follow wall using right hand on wall method
//                Direction newDir = dir.rotateLeft();
//                while (!rc.canMove(newDir)) {
//                    newDir = newDir.rotateLeft();
//                }
//
//                // with some probability, rotate left one more time
//                if (rng.nextInt() % 3 == 1) {
//                    newDir = newDir.rotateLeft();
//                }
//
//                if (rc.canMove(newDir)) rc.move(newDir);
//            }
//        }
//
        Pathing.navigateTo(rc, loc);
    }


    public static void updateSymmetry(RobotController rc) throws GameActionException {
        // if we are at a location that can sense a mirror location, and there isn't actually an enemy spawn location there, mark it as false
        mirrors = Communication.getSymmetry(rc);
        Team enemyTeam = rc.getTeam().opponent();
        // we have to do enemyTeam.ordinal() + 1 because the spawn zone team is 1-indexed :/
        if (rc.canSenseLocation(horizontalMirrorLocation)) {
            if (rc.senseMapInfo(horizontalMirrorLocation).getSpawnZoneTeamObject() == enemyTeam) {
                Communication.updateEnemySpawnLocation(rc, horizontalMirrorLocation);
            } else {
                Communication.updateSymmetryToFalse(rc, Communication.Symmetry.HORIZONTAL);
            }
        }
        if (rc.canSenseLocation(verticalMirrorLocation)) {
            if (rc.senseMapInfo(verticalMirrorLocation).getSpawnZoneTeamObject() == enemyTeam) {
                Communication.updateEnemySpawnLocation(rc, verticalMirrorLocation);
            } else {
                Communication.updateSymmetryToFalse(rc, Communication.Symmetry.VERTICAL);
            }
        }
        if (rc.canSenseLocation(oppositeOfSpawnLocation)) {
            if (rc.senseMapInfo(oppositeOfSpawnLocation).getSpawnZoneTeamObject() == enemyTeam) {
                Communication.updateEnemySpawnLocation(rc, oppositeOfSpawnLocation);
            } else {
                Communication.updateSymmetryToFalse(rc, Communication.Symmetry.ROTATIONAL);
            }
        }
    }

    public static void firstSpawn(RobotController rc, int duckNumber) throws GameActionException {
        MapLocation[] spawnLocs = rc.getAllySpawnLocations();


        // 3 locations representing a location from each spawn
        spawns = spawnLocationGroupFinder(rc);

        // gets the next spawn from the last
        MapLocation spawnWeWant = spawns[duckNumber % 3];
        

        for (MapLocation loc : spawnLocs) {
            if (loc.isWithinDistanceSquared(spawnWeWant, 8) && rc.canSpawn(loc)) {
                spawnLocation = loc;
                oppositeOfSpawnLocation = new MapLocation(rc.getMapWidth() - loc.x - 1, rc.getMapHeight() - loc.y - 1);
                horizontalMirrorLocation = new MapLocation(loc.x, rc.getMapHeight() - loc.y - 1);
                verticalMirrorLocation = new MapLocation(rc.getMapWidth() - loc.x - 1, loc.y);
                rc.spawn(loc);
                if (BFSSink == null) {
                    // System.out.println(loc);
                    // System.out.println(spawnWeWant);
                    BFSSink = spawnWeWant.add(directions[1]);
                    // System.out.println(spawnLocation);
                }
            }
        }
    }

    public static void spawn(RobotController rc, int duckNumber) throws GameActionException {
        if (underAttack(rc)) { // if under attack spawn near the action
            underAttackSpawn(rc);
        } else { // if not under attack spawn at your designated spawn
            firstSpawn(rc, duckNumber);
        }

        // brute force if we havent spawned yet
        if (!rc.isSpawned()) {
            MapLocation[] spawnLocs = rc.getAllySpawnLocations();
            for (MapLocation loc : spawnLocs) {
                if (rc.canSpawn(loc)) {
                    spawnLocation = loc;
                    oppositeOfSpawnLocation = getRotationalReflection(rc, loc);
                    horizontalMirrorLocation = getHorizontalLineReflection(rc, loc);
                    verticalMirrorLocation = getVerticalLineReflection(rc, loc);
                    rc.spawn(loc);
                }
            }
        }
    }

    private static void underAttackSpawn(RobotController rc) throws GameActionException {
        MapLocation underAttackLocation = getUnderAttackLocation(rc);
        MapLocation[] spawnLocations = spawnLocationGroupFinder(rc);
        MapLocation[] allSpawnLocations = rc.getAllySpawnLocations();
        // find if one of the 3 is near the action
        for (MapLocation spawnCandidate: spawnLocations){
            if (spawnCandidate.distanceSquaredTo(underAttackLocation) < 25){

                for (MapLocation loc : allSpawnLocations) {
                    if (loc.isWithinDistanceSquared(spawnCandidate, 8) && rc.canSpawn(loc)) {
                        spawnLocation = loc;
                        oppositeOfSpawnLocation = new MapLocation(rc.getMapWidth() - loc.x - 1, rc.getMapHeight() - loc.y - 1);
                        horizontalMirrorLocation = new MapLocation(loc.x, rc.getMapHeight() - loc.y - 1);
                        verticalMirrorLocation = new MapLocation(rc.getMapWidth() - loc.x - 1, loc.y);
                        rc.spawn(loc);
                    }
                }
            }
        }
    }

    // seems sus NGL
    public static void navigateToPossibleSpawnLocation(RobotController rc) throws GameActionException {
        enemySpawnLocations = Communication.getEnemySpawnLocations(rc);
        // bubble sort spawn locations i.e.
        // https://stackoverflow.com/questions/13040240/fastest-way-to-sort-3-values-in-java
        /*
        if (a > b)
            swap(a,b)
        if (b > c)
            swap(b,c)
        if (a > b)
            swap(a,b)
         */
        if (enemySpawnLocations[0] != null && enemySpawnLocations[1] != null && enemySpawnLocations[2] != null) {
            MapLocation tmp;
            if (rc.getLocation().distanceSquaredTo(enemySpawnLocations[0]) > rc.getLocation().distanceSquaredTo(enemySpawnLocations[1])) {
                tmp = enemySpawnLocations[0];
                enemySpawnLocations[0] = enemySpawnLocations[1];
                enemySpawnLocations[1] = tmp;
            }
            if (rc.getLocation().distanceSquaredTo(enemySpawnLocations[1]) > rc.getLocation().distanceSquaredTo(enemySpawnLocations[2])) {
                tmp = enemySpawnLocations[1];
                enemySpawnLocations[1] = enemySpawnLocations[2];
                enemySpawnLocations[2] = tmp;
            }
            if (rc.getLocation().distanceSquaredTo(enemySpawnLocations[0]) > rc.getLocation().distanceSquaredTo(enemySpawnLocations[1])) {
                tmp = enemySpawnLocations[0];
                enemySpawnLocations[0] = enemySpawnLocations[1];
                enemySpawnLocations[1] = tmp;
            }
        }
        for (MapLocation loc : enemySpawnLocations) {
            if (loc != null && !visitedEnemyLocations.contains(loc)) {
                navigateTo(rc, loc);
                if (rc.getLocation().distanceSquaredTo(loc) <= 2) visitedEnemyLocations.add(loc);
                return;
            }
        }
        if (mirrors[2] && !visitedEnemyLocations.contains(oppositeOfSpawnLocation)) {
            navigateTo(rc, oppositeOfSpawnLocation);
            if (rc.getLocation().distanceSquaredTo(oppositeOfSpawnLocation) <= 2)
                visitedEnemyLocations.add(oppositeOfSpawnLocation);
        } else if (mirrors[1] && !visitedEnemyLocations.contains(verticalMirrorLocation)) {
            navigateTo(rc, verticalMirrorLocation);
            if (rc.getLocation().distanceSquaredTo(verticalMirrorLocation) <= 2)
                visitedEnemyLocations.add(verticalMirrorLocation);
        } else if (mirrors[0] && !visitedEnemyLocations.contains(horizontalMirrorLocation)) {
            navigateTo(rc, horizontalMirrorLocation);
            if (rc.getLocation().distanceSquaredTo(horizontalMirrorLocation) <= 2)
                visitedEnemyLocations.add(horizontalMirrorLocation);
        } else {
            MapLocation[] flagPossibleLocations = rc.senseBroadcastFlagLocations();
            if (flagPossibleLocations.length > 0) {
                navigateTo(rc, flagPossibleLocations[0]);
            }
        }
    }

    public static void navigateToPossibleEnemyFlagLocations(RobotController rc) throws GameActionException {
        navigateToPossibleSpawnLocation(rc);
        // MapLocation[] flags = rc.senseBroadcastFlagLocations();
        // switch (flags.length) {
        //     case 0:
        //         navigateToPossibleSpawnLocation(rc);
        //         break;
        //     case 1:
        //         navigateTo(rc, flags[0]);
        //         break;
        //     case 2:
        //         MapLocation tmp;
        //         if (rc.getLocation().distanceSquaredTo(flags[0]) > rc.getLocation().distanceSquaredTo(flags[1])) {
        //             tmp = flags[0];
        //             flags[0] = flags[1];
        //             flags[1] = tmp;
        //         }
        //         navigateTo(rc, flags[0]);
        //         break;
        //     case 3:
        //         if (rc.getLocation().distanceSquaredTo(flags[0]) > rc.getLocation().distanceSquaredTo(flags[1])) {
        //             tmp = flags[0];
        //             flags[0] = flags[1];
        //             flags[1] = tmp;
        //         }
        //         if (rc.getLocation().distanceSquaredTo(flags[1]) > rc.getLocation().distanceSquaredTo(flags[2])) {
        //             tmp = flags[1];
        //             flags[1] = flags[2];
        //             flags[2] = tmp;
        //         }
        //         if (rc.getLocation().distanceSquaredTo(flags[0]) > rc.getLocation().distanceSquaredTo(flags[1])) {
        //             tmp = flags[0];
        //             flags[0] = flags[1];
        //             flags[1] = tmp;
        //         }
        //         navigateTo(rc, flags[0]);
        //         break;
        // }
    }

    // public static void pickUpFlags(RobotController rc) throws GameActionException {
    //     FlagInfo[] flags = rc.senseNearbyFlags(GameConstants.INTERACT_RADIUS_SQUARED, rc.getTeam().opponent());
    //     for (FlagInfo flag : flags) {
    //         if (rc.canPickupFlag(flag.getLocation())) {
    //             rc.pickupFlag(flag.getLocation());
    //             rc.setIndicatorString("Holding a flag!");
    //         }
    //     }
    // }

    public static void goToClosestNearbyFlagAndPickup(RobotController rc) throws GameActionException {
        FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        // maintain closest flag in case we can't pick any up so that we can move towards it
        FlagInfo closestFlag = null;
        int closestDist = 7200;
        for (FlagInfo flag : flags) {
            if (rc.canPickupFlag(flag.getLocation())) {
                rc.pickupFlag(flag.getLocation());
                rc.setIndicatorString("Holding a flag!");
            } else {
                int dist = rc.getLocation().distanceSquaredTo(flag.getLocation());
                if (dist < closestDist) {
                    closestDist = dist;
                    closestFlag = flag;
                }
            }
        }
        if (closestFlag != null) {
            MapLocation loc = closestFlag.getLocation();
            if (!rc.canSenseRobotAtLocation(loc)) {
                if (rc.canPickupFlag(loc)) {
                    rc.pickupFlag(loc);
                }
                navigateTo(rc, loc);
                if (rc.canPickupFlag(loc)) {
                    rc.pickupFlag(loc);
                }
            }
        }
    }

    public static void goToAndPickUpEnemyFlag(RobotController rc) throws GameActionException {
        FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        // maintain closest flag in case we can't pick any up so that we can move towards it
        FlagInfo closestFlag = null;
        int closestDist = 7200;
        for (FlagInfo flag : flags) {
            if (rc.canPickupFlag(flag.getLocation())) {
                rc.pickupFlag(flag.getLocation());
                rc.setIndicatorString("Holding a flag!");
            } else {
                int dist = rc.getLocation().distanceSquaredTo(flag.getLocation());
                if (dist < closestDist) {
                    closestDist = dist;
                    closestFlag = flag;
                }
            }
        }
        if (closestFlag != null) {
            navigateTo(rc, closestFlag.getLocation());
        } else {
            enemySpawnLocations = Communication.getEnemySpawnLocations(rc);
            for (MapLocation loc : enemySpawnLocations) {
                if (loc != null && !visitedEnemyLocations.contains(loc)) {
                    navigateTo(rc, loc);
                    if (rc.getLocation().distanceSquaredTo(loc) <= 2) visitedEnemyLocations.add(loc);
                    break;
                }
            }
            if (mirrors[2] && !visitedEnemyLocations.contains(oppositeOfSpawnLocation)) {
                navigateTo(rc, oppositeOfSpawnLocation);
                if (rc.getLocation().distanceSquaredTo(oppositeOfSpawnLocation) <= 2)
                    visitedEnemyLocations.add(oppositeOfSpawnLocation);
            } else if (mirrors[1] && !visitedEnemyLocations.contains(verticalMirrorLocation)) {
                navigateTo(rc, verticalMirrorLocation);
                if (rc.getLocation().distanceSquaredTo(verticalMirrorLocation) <= 2)
                    visitedEnemyLocations.add(verticalMirrorLocation);
            } else if (mirrors[0] && !visitedEnemyLocations.contains(horizontalMirrorLocation)) {
                navigateTo(rc, horizontalMirrorLocation);
                if (rc.getLocation().distanceSquaredTo(horizontalMirrorLocation) <= 2)
                    visitedEnemyLocations.add(horizontalMirrorLocation);
            } else {
                MapLocation[] flagPossibleLocations = rc.senseBroadcastFlagLocations();
                if (flagPossibleLocations.length > 0) {
                    navigateTo(rc, flagPossibleLocations[0]);
                }
            }
        }
    }


    public Role getRole() {
        return Role.BASE;
    }

    public BaseBot(int duckNumber) {
        this.duckNumber = duckNumber;
    }

    public BaseBot() {

    }


}

