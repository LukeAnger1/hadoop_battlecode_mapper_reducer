package Sprint1.Moves;

import battlecode.common.*;

import static Sprint1.BaseBot.navigateTo;
import static Sprint1.Moves.Utils.getAverageEnemyLocation;
import static Sprint1.Moves.Utils.getAverageTrapLocation;
import static Sprint1.RobotPlayer.directions;
import Sprint1.RobotPlayer;

public class Movement {
    public static MapLocation getClosestSpawnLocation(RobotController rc) throws GameActionException {
        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
        MapLocation closestLoc = spawnLocs[0];
        int closestDist = 7200;
        for (MapLocation loc : spawnLocs) {
            int dist = rc.getLocation().distanceSquaredTo(loc);
            if (dist < closestDist) {
                closestDist = dist;
                closestLoc = loc;
            }
        }
        rc.setIndicatorString("Closest spawn location is " + closestLoc.toString());
        return closestLoc;
    }

    public static void moveRandomly(RobotController rc) throws GameActionException {
        Direction dir = directions[RobotPlayer.rng.nextInt(directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }

    public static void moveAwayFrom(RobotController rc, MapLocation enemyLoc) throws GameActionException {
        MapLocation loc = rc.getLocation();
        Direction dirToEnemy = loc.directionTo(enemyLoc);
        Direction awayFromEnemy = dirToEnemy.opposite();
        Direction awayFromEnemyLeft = awayFromEnemy.rotateLeft();
        Direction awayFromEnemyRight = awayFromEnemyLeft.rotateRight();


        // we try directly away, then one direction to each side away
        if (rc.canMove(awayFromEnemy)){
            rc.move(awayFromEnemy);
        } else if (rc.canMove(awayFromEnemyLeft)) {
            rc.move(awayFromEnemyLeft);
        } else if (rc.canMove(awayFromEnemyRight)) {
            rc.move(awayFromEnemyRight);
        }
    }

    public static void dontBlockFlagHolders(RobotController rc) throws GameActionException {
        FlagInfo[] enemyFlagInfos = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        MapLocation loc = rc.getLocation();

        for (FlagInfo flag : enemyFlagInfos) {

            if (!flag.isPickedUp()) {
                continue;
            }

            Direction dir = loc.directionTo(getClosestSpawnLocation(rc));
            MapLocation flagHolderLoc = flag.getLocation();
            Direction flagHolderDir = flagHolderLoc.directionTo(getClosestSpawnLocation(rc));

            if (loc.distanceSquaredTo(flagHolderLoc) <= 4) {
                // might be blocking give it space
                Direction away = loc.directionTo(flagHolderLoc).opposite();
                if (rc.canMove(away)) {
                    rc.move(away);
                }

            } else if (loc.distanceSquaredTo(getClosestSpawnLocation(rc)) < 8) {
                // if we are close to spawn give it some space to move in
                dir = dir.rotateLeft().rotateLeft();
                if (rc.canMove(dir)) {
                    rc.move(dir);
                }
            } else {
                if (rc.canMove(dir)) {
                    rc.move(dir);
                }
            }

        }
    }

    public static void moveBehindOurAverageTrapPosition(RobotController rc) throws GameActionException {
        MapInfo[] nearbyMapInfos = rc.senseNearbyMapInfos(-1);
        RobotInfo[] nearbyEnemyInfos = rc.senseNearbyRobots(-1, rc.getTeam().opponent());

        MapLocation avgTrapLoc = getAverageTrapLocation(rc, nearbyMapInfos);
        MapLocation avgEnemyLoc = getAverageEnemyLocation(rc, nearbyEnemyInfos);

        if (avgTrapLoc == null || avgEnemyLoc == null) return;

        Direction enemyDirectionToTrap = avgEnemyLoc.directionTo(avgTrapLoc);
        navigateTo(rc, avgTrapLoc.add(enemyDirectionToTrap).add(enemyDirectionToTrap));
        rc.setIndicatorDot(avgEnemyLoc, 0,255,0);
        rc.setIndicatorDot(avgEnemyLoc, 0,0,255);
    }

}
