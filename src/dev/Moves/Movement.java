package dev.Moves;

import battlecode.common.*;

import static dev.BaseBot.navigateTo;
import static dev.Moves.Utils.*;
import static dev.RobotPlayer.directions;
import static dev.RobotPlayer.BFSSink;
import static dev.Pathing.parents;
import dev.RobotPlayer;

public class Movement {
	static Direction currentDirection = directions[RobotPlayer.rng.nextInt(directions.length)];
    public static MapLocation getClosestSpawnLocation(RobotController rc) throws GameActionException {
        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
        MapLocation closestLoc = spawnLocs[0];
        int closestDist = 7200;
        for (MapLocation loc : spawnLocs) {
            if (rc.isSpawned()) {
                int dist = rc.getLocation().distanceSquaredTo(loc);
                if (dist < closestDist) {
                    closestDist = dist;
                    closestLoc = loc;
                }
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

	public static void moveRandomParticle(RobotController rc) throws GameActionException {
		if (rc.canMove(currentDirection)) {
			rc.move(currentDirection);
		} else {
			currentDirection = directions[RobotPlayer.rng.nextInt(directions.length)];
			if (rc.canMove(currentDirection)) {
				rc.move(currentDirection);
			} else {
				currentDirection = directions[RobotPlayer.rng.nextInt(directions.length)];
				if (rc.canMove(currentDirection)) {
					rc.move(currentDirection);
				} else {
					currentDirection = directions[RobotPlayer.rng.nextInt(directions.length)];
					if (rc.canMove(currentDirection)) {
						rc.move(currentDirection);
					}
				}
			}
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

            if (loc.distanceSquaredTo(flagHolderLoc) <= 9) {
                // might be blocking give it space
				moveAwayFrom(rc, flagHolderLoc);
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

    public static void retreat(RobotController rc) throws GameActionException {

        MapLocation loc = rc.getLocation();
//        MapLocation avgEnemyLoc = getAverageEnemyLocation(rc, rc.senseNearbyRobots(-1, rc.getTeam().opponent()));
//        MapLocation avgTeammateLoc = getAverageTeammateLocation(rc, rc.senseNearbyRobots(-1, rc.getTeam()));
        RobotInfo closestEnemy = getClosestEnemy(rc);
        //Direction enemyDirToTeam = avgEnemyLoc.directionTo(avgEnemyLoc);


        if (closestEnemy != null){
            moveAwayFrom(rc, closestEnemy.getLocation());
        }
//        if (avgEnemyLoc != null){
//            moveAwayFrom(rc, avgEnemyLoc);
//        } else if (avgTeammateLoc != null) {
//            rc.setIndicatorDot(avgTeammateLoc, 50,50,20);
//            navigateTo(rc, avgTeammateLoc);
//        }

    }

	public static void returnFlag(RobotController rc) throws GameActionException {
        if (BFSSink != null && BFSSink.isWithinDistanceSquared(getClosestSpawnLocation(rc), 4) && parents.containsKey(rc.getLocation()))
            navigateTo(rc, BFSSink);
		else navigateTo(rc, getClosestSpawnLocation(rc));
	}

}
