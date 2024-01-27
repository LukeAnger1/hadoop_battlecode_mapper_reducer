package L;

import battlecode.common.*;
import static L.RobotPlayer.*;
import java.util.HashSet;

public class Nav {
    static MapLocation spawnLocation;
    static MapLocation oppositeOfSpawnLocation;
    static MapLocation horizontalMirrorLocation;
    static MapLocation verticalMirrorLocation;
    // mirrors[0] = horizontal, mirrors[1] = vertical, mirrors[2] = rotational
    static boolean[] mirrors = {true, true, true};
    static MapLocation lastLoc;
    // hashset of visited enemy locations
    static HashSet<MapLocation> visitedEnemyLocations = new HashSet<MapLocation>();
    static MapLocation ignoreLocation = null;

    public static void navigateTo(RobotController rc, MapLocation loc) throws GameActionException{
        bugNav(rc, loc);
    }

    public static void bugNav(RobotController rc, MapLocation loc) throws GameActionException{
        // Use the comments above to implement bug navigation
        MapLocation currentLoc = rc.getLocation();
        rc.setIndicatorString("Navigating to " + loc.toString());
        while (!currentLoc.equals(loc) && rc.isMovementReady()){
            Direction dir = currentLoc.directionTo(loc);
            if (rc.canMove(dir)){
                rc.move(dir);
            }
            else{
                // follow wall using right hand on wall method
                Direction newDir = dir.rotateLeft();
                while (!rc.canMove(newDir)){
                    newDir = newDir.rotateLeft();
                }

                // with some probability, rotate left one more time
                if (rng.nextInt() % 3 == 1){
                    newDir = newDir.rotateLeft();
                }

                if (rc.canMove(newDir)) rc.move(newDir);
            }
        }
    }

    public static void moveRandomly(RobotController rc) throws GameActionException{
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)){
            rc.move(dir);
        }
    }

    public static MapLocation getSpawnLocation(RobotController rc) {
        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
        for (MapLocation loc : spawnLocs){
            if (rc.canSpawn(loc)) {
                spawnLocation = loc;
                oppositeOfSpawnLocation = new MapLocation(rc.getMapWidth() - loc.x, rc.getMapHeight() - loc.y);
                horizontalMirrorLocation = new MapLocation(rc.getMapWidth() - loc.x, loc.y);
                verticalMirrorLocation = new MapLocation(loc.x, rc.getMapHeight() - loc.y);
                return loc;
            }
        }
        return null;
    }

    public static MapLocation getClosestSpawnLocation(RobotController rc) throws GameActionException{
        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
        MapLocation closestLoc = spawnLocs[0];
        int closestDist = 7200;
        for (MapLocation loc : spawnLocs){
            int dist = rc.getLocation().distanceSquaredTo(loc);
            if (dist < closestDist){
                closestDist = dist;
                closestLoc = loc;
            }
        }
        rc.setIndicatorString("Closest spawn location is " + closestLoc.toString());
        return closestLoc;
    }
    public static void updateSymmetry(RobotController rc) throws GameActionException{
        // if we are at a location that can sense a mirror location, and there isn't actually an enemy spawn location there, mark it as false
        if (rc.canSenseLocation(horizontalMirrorLocation)){
            if (!rc.senseMapInfo(horizontalMirrorLocation).isSpawnZone()){
                mirrors[0] = false;
            }
            else {
                Communication.updateEnemySpawnLocation(rc, horizontalMirrorLocation);
            }
        }
        if (rc.canSenseLocation(verticalMirrorLocation)){
            if (!rc.senseMapInfo(verticalMirrorLocation).isSpawnZone()){
                mirrors[1] = false;
            }
            else {
                Communication.updateEnemySpawnLocation(rc, verticalMirrorLocation);
            }
        }
        if (rc.canSenseLocation(oppositeOfSpawnLocation)){
            if (!rc.senseMapInfo(oppositeOfSpawnLocation).isSpawnZone()){
                mirrors[2] = false;
            }
            else {
                Communication.updateEnemySpawnLocation(rc, oppositeOfSpawnLocation);
            }
        }
    }
    public static int taxiDistance(MapLocation start, MapLocation end) {
        // This function is good because this is how the robot actually has to move
        return Math.max(Math.abs(start.x - end.y), Math.abs(end.x - end.y));
    }
}
