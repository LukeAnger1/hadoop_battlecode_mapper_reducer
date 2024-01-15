package yarobot;

import battlecode.common.*;
import static yarobot.RobotPlayer.*;
import java.util.HashSet;

public class AttackerBot {
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


    public static void runAttacker(RobotController rc) throws GameActionException {
        if (!rc.isSpawned()){
            spawn(rc);
        }
        else{
            boolean haveEnemyFlag = rc.hasFlag() && rc.getRoundNum() >= GameConstants.SETUP_ROUNDS;

            if (haveEnemyFlag){
                navigateTo(rc, getClosestSpawnLocation(rc));
            }
            else{
                goToAndPickUpEnemyFlag(rc);
            }

            attackEnemies(rc);

            healAllies(rc);

            moveRandomly(rc);

            updateSymmetry(rc);
        }
    }

    public static void spawn(RobotController rc) throws GameActionException{
        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
        for (MapLocation loc : spawnLocs){
            if (rc.canSpawn(loc)) {
                spawnLocation = loc;
                oppositeOfSpawnLocation = new MapLocation(rc.getMapWidth() - loc.x, rc.getMapHeight() - loc.y);
                horizontalMirrorLocation = new MapLocation(rc.getMapWidth() - loc.x, loc.y);
                verticalMirrorLocation = new MapLocation(loc.x, rc.getMapHeight() - loc.y);
                rc.spawn(loc);
            }
        }
    }


    public static void navigateTo(RobotController rc, MapLocation loc) throws GameActionException{
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

    public static void goToAndPickUpEnemyFlag(RobotController rc) throws GameActionException{
        FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        // maintain closest flag in case we can't pick any up so that we can move towards it
        FlagInfo closestFlag = null;
        int closestDist = 7200;
        for (FlagInfo flag : flags){
            if (rc.canPickupFlag(flag.getLocation())){
                rc.pickupFlag(flag.getLocation());
                rc.setIndicatorString("Holding a flag!");
            }
            else{
                int dist = rc.getLocation().distanceSquaredTo(flag.getLocation());
                if (dist < closestDist){
                    closestDist = dist;
                    closestFlag = flag;
                }
            }
        }
        if (closestFlag != null){
            navigateTo(rc, closestFlag.getLocation());
        }
        else {
            // See if we know about an enemy spawn first
            MapLocation enemySpawnLocation = Communication.getClosestEnemySpawnLocation(rc);
            // if we're near this location and don't see a flag there, then let's not go there for now
            if (enemySpawnLocation != null && !enemySpawnLocation.equals(ignoreLocation) && !(rc.getLocation().distanceSquaredTo(enemySpawnLocation) <= 2)){
                navigateTo(rc, enemySpawnLocation);
            }
            else if (enemySpawnLocation != null && !enemySpawnLocation.equals(ignoreLocation)) {
                ignoreLocation = enemySpawnLocation;
            }
            // choose a symmetry that is true and navigate to it
            if (mirrors[2]){
                navigateTo(rc, oppositeOfSpawnLocation);
            }
            else if (mirrors[1]){
                navigateTo(rc, verticalMirrorLocation);
            }
            else if (mirrors[0]){
                navigateTo(rc, horizontalMirrorLocation);
            }
        }
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

    public static void attackEnemies(RobotController rc) throws GameActionException{
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        for (int i = 0; i < enemyRobots.length; i++){
            // attack enemy if we can
            if (rc.canAttack(enemyRobots[i].getLocation())){
                rc.attack(enemyRobots[i].getLocation());
                System.out.println("Attacked an enemy!");
            }
        }
    }

    public static void healAllies(RobotController rc) throws GameActionException{
        RobotInfo[] allyRobots = rc.senseNearbyRobots(-1, rc.getTeam());
        for (int i = 0; i < allyRobots.length; i++){
            // heal ally if we can
            if (rc.canHeal(allyRobots[i].getLocation())){
                rc.heal(allyRobots[i].getLocation());
                System.out.println("Healed an ally!");
            }
        }
    }

    public static void moveRandomly(RobotController rc) throws GameActionException{
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)){
            rc.move(dir);
        }
    }
}
