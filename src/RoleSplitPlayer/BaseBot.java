package RoleSplitPlayer;

import RoleSplitPlayer.Communication.Role;
import battlecode.common.*;

import java.util.ArrayList;
import java.util.HashSet;

import static RoleSplitPlayer.RobotPlayer.directions;
import static RoleSplitPlayer.RobotPlayer.rng;

public class BaseBot {

    static final int MOVE_AND_ATTACK_RANGE_SQUARED = (int) Math.pow(Math.sqrt(GameConstants.ATTACK_RADIUS_SQUARED) + 1, 2);
    static final int MOVE_AND_HEAL_RANGE_SQUARED = (int) Math.pow(Math.sqrt(GameConstants.HEAL_RADIUS_SQUARED) + 1, 2);

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

    static Direction currentDirection = directions[rng.nextInt() % 8];

    public int duckNumber = 0;

    static public ArrayList<MapLocation> spawns = null;

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
        } else {
            goToNearbyCrumbsAndFillWater(rc);
            fillEverything(rc);
            walkInDirection(rc);
            updateSymmetry(rc);
        }
    }

    public void fillEverything(RobotController rc) throws GameActionException {
        MapLocation loc = rc.getLocation();
        for (Direction dir : Direction.allDirections()) {
            MapLocation fillLoc = loc.add(dir);
            if (rc.canFill(fillLoc)) {
                rc.fill(fillLoc);
            }
        }

    }

    private void goToNearbyCrumbsAndFillWater(RobotController rc) throws GameActionException {
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


    private void walkInDirection(RobotController rc) throws GameActionException {

        while (rc.isMovementReady()) {
            Direction dir = currentDirection;
            if (rc.canMove(dir)) {
                rc.move(dir);
            } else {
                // follow wall using right hand on wall method
                Direction newDir = dir.rotateLeft();
                while (!rc.canMove(newDir)) {
                    newDir = newDir.rotateLeft();
                }

                // with some probability, rotate left one more time
                if (rng.nextInt() % 3 == 1) {
                    newDir = newDir.rotateLeft();
                }

                if (rc.canMove(newDir)) rc.move(newDir);
            }
        }
    }

    public void move(RobotController rc) throws GameActionException {
        if (!rc.isSpawned()) {
            spawn(rc, duckNumber);
        } else {
            updateSymmetry(rc);
            attackWithPriorityTo_Flag_InRange_Lowest(rc);
            healWithPriorityTo_Flag_InRange_Lowest(rc);
        }
    }

    public static void navigateTo(RobotController rc, MapLocation loc) throws GameActionException {
        // Use the comments above to implement bug navigation
        MapLocation currentLoc = rc.getLocation();
        rc.setIndicatorString("Navigating to " + loc.toString());
        while (!currentLoc.equals(loc) && rc.isMovementReady()) {
            Direction dir = currentLoc.directionTo(loc);
            if (rc.canMove(dir)) {
                rc.move(dir);
            } else {
                // follow wall using right hand on wall method
                Direction newDir = dir.rotateLeft();
                while (!rc.canMove(newDir)) {
                    newDir = newDir.rotateLeft();
                }

                // with some probability, rotate left one more time
                if (rng.nextInt() % 3 == 1) {
                    newDir = newDir.rotateLeft();
                }

                if (rc.canMove(newDir)) rc.move(newDir);
            }
        }

    }


    public static void updateSymmetry(RobotController rc) throws GameActionException {
        // if we are at a location that can sense a mirror location, and there isn't actually an enemy spawn location there, mark it as false
        mirrors = Communication.getSymmetry(rc);
        Team enemyTeam = rc.getTeam().opponent();
        // we have to do enemyTeam.ordinal() + 1 because the spawn zone team is 1-indexed :/
        if (rc.canSenseLocation(horizontalMirrorLocation)) {
            if (rc.senseMapInfo(horizontalMirrorLocation).getSpawnZoneTeam() == (enemyTeam.ordinal() + 1)) {
                Communication.updateEnemySpawnLocation(rc, horizontalMirrorLocation);
            } else {
                Communication.updateSymmetryToFalse(rc, Communication.Symmetry.HORIZONTAL);
            }
        }
        if (rc.canSenseLocation(verticalMirrorLocation)) {
            if (rc.senseMapInfo(verticalMirrorLocation).getSpawnZoneTeam() == (enemyTeam.ordinal() + 1)) {
                Communication.updateEnemySpawnLocation(rc, verticalMirrorLocation);
            } else {
                Communication.updateSymmetryToFalse(rc, Communication.Symmetry.VERTICAL);
            }
        }
        if (rc.canSenseLocation(oppositeOfSpawnLocation)) {
            if (rc.senseMapInfo(oppositeOfSpawnLocation).getSpawnZoneTeam() == (enemyTeam.ordinal() + 1)) {
                Communication.updateEnemySpawnLocation(rc, oppositeOfSpawnLocation);
            } else {
                Communication.updateSymmetryToFalse(rc, Communication.Symmetry.ROTATIONAL);
            }
        }
    }

    public static void spawn(RobotController rc, int duckNumber) throws GameActionException {
        firstSpawn(rc, duckNumber);
        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
        for (MapLocation loc : spawnLocs) {
            if (rc.canSpawn(loc)) {
                spawnLocation = loc;
                oppositeOfSpawnLocation = new MapLocation(rc.getMapWidth() - loc.x - 1, rc.getMapHeight() - loc.y - 1);
                horizontalMirrorLocation = new MapLocation(loc.x, rc.getMapHeight() - loc.y - 1);
                verticalMirrorLocation = new MapLocation(rc.getMapWidth() - loc.x - 1, loc.y);
                rc.spawn(loc);
            }
        }
    }

    public static void firstSpawn(RobotController rc, int duckNumber) throws GameActionException {
        MapLocation[] spawnLocs = rc.getAllySpawnLocations();


        // 3 locations representing a location from each spawn
        spawns = spawnLocationGroupFinder(rc);

        // gets the next spawn from the last
        MapLocation spawnWeWant = spawns.get(duckNumber % 3);


        for (MapLocation loc : spawnLocs) {
            if (loc.isWithinDistanceSquared(spawnWeWant, 8) && rc.canSpawn(loc)) {
                spawnLocation = loc;
                oppositeOfSpawnLocation = new MapLocation(rc.getMapWidth() - loc.x - 1, rc.getMapHeight() - loc.y - 1);
                horizontalMirrorLocation = new MapLocation(loc.x, rc.getMapHeight() - loc.y - 1);
                verticalMirrorLocation = new MapLocation(rc.getMapWidth() - loc.x - 1, loc.y);
                rc.spawn(loc);
            }
        }
    }

    public static void spawn(RobotController rc, MapLocation loc) throws GameActionException {
        spawnLocation = loc;
        oppositeOfSpawnLocation = new MapLocation(rc.getMapWidth() - loc.x, rc.getMapHeight() - loc.y);
        horizontalMirrorLocation = new MapLocation(rc.getMapWidth() - loc.x, loc.y);
        verticalMirrorLocation = new MapLocation(loc.x, rc.getMapHeight() - loc.y);
        rc.spawn(loc);
    }

    public static ArrayList<MapLocation> spawnLocationGroupFinder(RobotController rc) {
        ArrayList<MapLocation> spawns = new ArrayList<>();

        for (MapLocation loc : rc.getAllySpawnLocations()) {

            // first is always not near any others
            if (spawns.size() == 0) {
                spawns.add(loc);
                continue;
            }

            boolean differentSpawn = true;

            for (MapLocation loc2 : spawns) {

                if (loc.isWithinDistanceSquared(loc2, 8)) {
                    differentSpawn = false;
                }
            }

            if (differentSpawn) {
                spawns.add(loc);
                if (spawns.size() == 3) {
                    break;
                }
            }


        }

        return spawns;
    }

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

    public static void moveRandomly(RobotController rc) throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }

    public static void attackWithPriorityTo_Flag_InRange_Lowest(RobotController rc) throws GameActionException {
        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());

        MapLocation loc = rc.getLocation();

        RobotInfo lowestFlagHolderInAttackRange = null;
        int lowestFlagHolderInAttackRangeHP = GameConstants.DEFAULT_HEALTH + 1;

        RobotInfo lowestFlagHolderInMoveAndAttackRange = null;
        int lowestFlagHolderInMoveAndAttackRangeHP = GameConstants.DEFAULT_HEALTH + 1;

        RobotInfo lowestInAttackRange = null;
        int lowestInAttackRangeHP = GameConstants.DEFAULT_HEALTH + 1;

        RobotInfo lowestInMoveAndAttackRange = null;
        int lowestInMoveAndAttackRangeHP = GameConstants.DEFAULT_HEALTH + 1;

        // gather info
        for (RobotInfo enemy : nearbyEnemies) {
            MapLocation enemyLoc = enemy.getLocation();
            int distanceSquaredToEnemy = loc.distanceSquaredTo(enemyLoc);
            int enemyHP = enemy.getHealth();

            boolean hasFlag = enemy.hasFlag();
            boolean inAttackRange = distanceSquaredToEnemy <= GameConstants.ATTACK_RADIUS_SQUARED;
            boolean inMoveAndAttackRange = distanceSquaredToEnemy <= MOVE_AND_ATTACK_RANGE_SQUARED && rc.canMove(loc.directionTo(enemyLoc));

            if (hasFlag && inAttackRange) {
                if (enemyHP < lowestFlagHolderInAttackRangeHP) {
                    lowestFlagHolderInAttackRangeHP = enemyHP;
                    lowestFlagHolderInAttackRange = enemy;
                }
            }

            if (hasFlag && inMoveAndAttackRange) {
                if (enemyHP < lowestFlagHolderInMoveAndAttackRangeHP) {
                    lowestFlagHolderInMoveAndAttackRangeHP = enemyHP;
                    lowestFlagHolderInMoveAndAttackRange = enemy;
                }
            }

            if (inAttackRange) {
                if (enemyHP < lowestInAttackRangeHP) {
                    lowestInAttackRangeHP = enemyHP;
                    lowestInAttackRange = enemy;
                }
            }

            if (inMoveAndAttackRange) {
                if (enemyHP < lowestInMoveAndAttackRangeHP) {
                    lowestInMoveAndAttackRangeHP = enemyHP;
                    lowestInMoveAndAttackRange = enemy;
                }
            }


        }

        MapLocation enemyLoc;

        // if there is a flag holder we can hit in range we hit the lowest
        if (lowestFlagHolderInAttackRange != null) {
            enemyLoc = lowestFlagHolderInAttackRange.location;
            if (rc.canAttack(enemyLoc)) {
                rc.attack(enemyLoc);
            }
        }

        // if we can get in range and attack then move to it and attack
        else if (lowestFlagHolderInMoveAndAttackRange != null) {
            enemyLoc = lowestFlagHolderInMoveAndAttackRange.location;
            rc.move(loc.directionTo(enemyLoc));
            if (rc.canAttack(enemyLoc)) {
                rc.attack(enemyLoc);
            }

        // if we are in range of a duck to attack we hit it and kite
        } else if (lowestInAttackRange != null) {
            enemyLoc = lowestInAttackRange.location;
            if (rc.canAttack(enemyLoc)) {
                rc.attack(enemyLoc);
            }
            moveAwayFrom(rc, enemyLoc); // kiting
        }
        // if we are able to move forward and attack then do that
        else if (lowestInMoveAndAttackRange != null) {
            enemyLoc = lowestInMoveAndAttackRange.location;
            rc.move(loc.directionTo(enemyLoc));
            if (rc.canAttack(enemyLoc)) {
                rc.attack(enemyLoc);
            }
        }




    }

    private static void moveAwayFrom(RobotController rc, MapLocation enemyLoc) throws GameActionException {
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

    public static void healWithPriorityTo_Flag_InRange_Lowest(RobotController rc) throws GameActionException {
        RobotInfo[] nearbyTeammates = rc.senseNearbyRobots(-1, rc.getTeam());

        MapLocation loc = rc.getLocation();

        RobotInfo lowestFlagHolderInHealRange = null;
        int lowestFlagHolderInHealRangeHP = GameConstants.DEFAULT_HEALTH + 1;

        RobotInfo lowestFlagHolderInMoveAndHealRange = null;
        int lowestFlagHolderInMoveAndHealRangeHP = GameConstants.DEFAULT_HEALTH + 1;

        RobotInfo lowestInHealRange = null;
        int lowestInHealRangeHP = GameConstants.DEFAULT_HEALTH + 1;

        RobotInfo lowestInMoveAndHealRange = null;
        int lowestInMoveAndHealRangeHP = GameConstants.DEFAULT_HEALTH + 1;

        // gather info
        for (RobotInfo teammate : nearbyTeammates) {
            MapLocation teammateLoc = teammate.getLocation();
            int distanceSquaredToTeammate = loc.distanceSquaredTo(teammateLoc);
            int teammateHP = teammate.getHealth();
            boolean hasFlag = teammate.hasFlag();
            boolean inHealRange = distanceSquaredToTeammate <= GameConstants.HEAL_RADIUS_SQUARED;
            boolean inMoveAndHealRange = distanceSquaredToTeammate <= MOVE_AND_HEAL_RANGE_SQUARED && rc.canMove(loc.directionTo(teammateLoc));
            if (hasFlag && inHealRange) {
                if (teammateHP < lowestFlagHolderInHealRangeHP) {
                    lowestFlagHolderInHealRangeHP = teammateHP;
                    lowestFlagHolderInHealRange = teammate;
                }
            }

            if (hasFlag && inMoveAndHealRange) {
                if (teammateHP < lowestFlagHolderInMoveAndHealRangeHP) {
                    lowestFlagHolderInMoveAndHealRangeHP = teammateHP;
                    lowestFlagHolderInMoveAndHealRange = teammate;
                }
            }

            if (inHealRange) {
                if (teammateHP < lowestInHealRangeHP) {
                    lowestInHealRangeHP = teammateHP;
                    lowestInHealRange = teammate;
                }
            }

            if (inMoveAndHealRange) {
                if (teammateHP < lowestInMoveAndHealRangeHP) {
                    lowestInMoveAndHealRangeHP = teammateHP;
                    lowestInMoveAndHealRange = teammate;
                }
            }
        }

        MapLocation teammateLoc = null;

        if (lowestFlagHolderInHealRange != null) {
            teammateLoc = lowestFlagHolderInHealRange.location;
        } else if (lowestFlagHolderInMoveAndHealRange != null) {
            teammateLoc = lowestFlagHolderInMoveAndHealRange.location;
            rc.move(loc.directionTo(teammateLoc));
        } else if (lowestInHealRange != null) {
            teammateLoc = lowestInHealRange.location;
        } else if (lowestInMoveAndHealRange != null) {
            teammateLoc = lowestInMoveAndHealRange.location;
            rc.move(loc.directionTo(teammateLoc));
        }

        if (teammateLoc != null && rc.canHeal(teammateLoc)) {
            rc.heal(teammateLoc);
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

    public static void buildTrapIfEnoughPlayers(RobotController rc, int enemyThresholdStun, int enemyThresholdBomb) throws GameActionException {

        RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());

        TrapType trapType = nearbyEnemies.length > enemyThresholdBomb ? TrapType.EXPLOSIVE : nearbyEnemies.length > enemyThresholdStun ? TrapType.STUN : TrapType.NONE;

        if (trapType == TrapType.NONE) return;

        for (Direction direction1 : Direction.allDirections()) {
            if (rc.canBuild(trapType, rc.getLocation().add(direction1))) {
                rc.build(trapType, rc.getLocation().add(direction1));
                break;
            }
        }
    }
}

