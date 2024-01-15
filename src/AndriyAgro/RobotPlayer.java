package AndriyAgro;

import battlecode.common.*;
import battlecode.world.Flag;
import scala.Int;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Random;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;

    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);

    /**
     * Array containing all the possible movement directions.
     */
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc The RobotController object. You use it to perform actions from this robot, and to get
     *           information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // Hello world! Standard output is very useful for debugging.
        // Everything you say here will be directly viewable in your terminal when you run a match!

        // You can also use indicators to save debug notes in replays.
        rc.setIndicatorString("Hello world!");
        Deque<MapLocation> visited = new ArrayDeque<>();

        while (true) {
            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to do.

            turnCount += 1;  // We have now been alive for one more turn!

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try {
                // Make sure you spawn your robot in before you attempt to take any actions!
                // Robots not spawned in do not have vision of any tiles and cannot perform any actions.
                if (!rc.isSpawned()) {
                    MapLocation[] spawnLocs = rc.getAllySpawnLocations();
                    // Pick a random spawn location to attempt spawning in.
                    for (MapLocation spawnCandidate : spawnLocs) {
                        if (rc.canSpawn(spawnCandidate)) {
                            rc.spawn(spawnCandidate);
                        }
                    }
                } else {
                    if (rc.canBuyGlobal(GlobalUpgrade.ACTION)) {
                        rc.buyGlobal(GlobalUpgrade.ACTION);
                    } else if (rc.canBuyGlobal(GlobalUpgrade.HEALING)) {
                        rc.buyGlobal(GlobalUpgrade.HEALING);
                    }
                    if (rc.canPickupFlag(rc.getLocation())) {
                        rc.pickupFlag(rc.getLocation());
                        rc.setIndicatorString("Holding a flag!");
                    }

                    if (rc.hasFlag() && rc.getRoundNum() >= GameConstants.SETUP_ROUNDS) {
                        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
                        MapLocation firstLoc = spawnLocs[0];
                        Direction dir = rc.getLocation().directionTo(closestAllySpawnLocation(rc, rc.getLocation()));
                        if (rc.canMove(dir)) {
                            rc.move(dir);
                        }

                    }
                    FlagInfo[] enemyFlagInfos = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
                    if (enemyFlagInfos.length > 0){
                        FlagInfo flag = enemyFlagInfos[0];
                        if (flag.isPickedUp()){
                            if (rc.getLocation().distanceSquaredTo(flag.getLocation()) <= 9){
                                if(rc.canMove(rc.getLocation().directionTo(flag.getLocation()).opposite())){
                                    rc.move(rc.getLocation().directionTo(flag.getLocation()).opposite());
                                }
                            }
                        }
                    }
                    MapLocation[] nearbyCrumbs = rc.senseNearbyCrumbs(-1);
                    for (MapLocation nearbyCrumb : nearbyCrumbs) {
                        Direction dir = getDirection(nearbyCrumb.x - rc.getLocation().x, nearbyCrumb.y - rc.getLocation().y);
                        if (rc.canMove(dir)) {
                            rc.move(dir);
                        }
                    }

                    if (rc.getID() % 3 == 0 && rc.getRoundNum() >= GameConstants.SETUP_ROUNDS && !rc.hasFlag()) {

                        Direction direction = getDirection(rc.readSharedArray(0) - rc.getLocation().x, rc.readSharedArray(1) - rc.getLocation().y);
                        if (rc.canMove(direction)) {
                            rc.move(direction);
                        }

                        if (rc.getID() % 6 == 0) {
                            updateEnemyRobots(rc);
                            rc.setIndicatorString("SPARTA COMMANDER");
                        }
                        if (rc.getID() % 15 == 0) {
                            if (rc.senseNearbyRobots(-1, rc.getTeam().opponent()).length > 3)
                            for (Direction direction1 : directions) {
                                if (rc.canBuild(TrapType.STUN, rc.getLocation().add(direction1))) {
                                    rc.build(TrapType.STUN, rc.getLocation().add(direction1));
                                    rc.setIndicatorString("SPARTA BOMBER");
                                    break;
                                }
                            }
                        }

                        for (Direction direction1 : directions) {
                            if (rc.canAttack(rc.getLocation().add(direction1))) {
                                rc.attack(rc.getLocation().add(direction1));
                                rc.setIndicatorString("SPARTA ATTACK");
                                break;
                            }
                        }

                        for (Direction direction1 : directions) {
                            if (rc.canHeal(rc.getLocation().add(direction1))) {
                                rc.heal(rc.getLocation().add(direction1));
                                rc.setIndicatorString("SPARTA HEAL");
                                break;
                            }
                        }

                    }


                    if (rc.getID() % 3 != 0 && rc.getRoundNum() >= 200 && !rc.hasFlag()) {

                        Direction direction = getDirection(rc.readSharedArray(0) - rc.getLocation().x, rc.readSharedArray(1) - rc.getLocation().y);
                        if (rc.canMove(direction)) {
                            rc.move(direction);
                        }

                        for (Direction direction1 : directions) {
                            if (rc.canHeal(rc.getLocation().add(direction1))) {
                                rc.heal(rc.getLocation().add(direction1));
                                rc.setIndicatorString("GREEK HEAL");
                                break;
                            }
                        }

                        for (Direction direction1 : directions) {
                            if (rc.canAttack(rc.getLocation().add(direction1))) {
                                rc.attack(rc.getLocation().add(direction1));
                                rc.setIndicatorString("GREEK ATTACK");
                                break;
                            }
                        }
                    }

                    for (Direction direction1 : directions) {
                        if (rc.canFill((rc.getLocation().add(direction1)))) {
                            rc.fill(rc.getLocation().add(direction1));
                            rc.setIndicatorString("DIGGIN");
                            break;
                        }
                    }


                    // If we are holding an enemy flag, singularly focus on moving towards
                    // an ally spawn zone to capture it! We use the check roundNum >= SETUP_ROUNDS
                    // to make sure setup phase has ended.

                    // Move and attack randomly if no objective.
                    for(Direction direction: directions){
                        if (rc.canMove(direction) && !visited.contains(rc.getLocation().add(direction))){
                            rc.move(direction);
                            visited.add(rc.getLocation().add(direction));
                        }
                        if (visited.size() > 20){
                            visited.pollFirst();
                        }
                    }
                }

            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                System.out.println("GameActionException");
                e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                System.out.println("Exception");
                e.printStackTrace();

            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }

    private static MapLocation closestAllySpawnLocation(RobotController rc, MapLocation location) {
        MapLocation closestLocation = new MapLocation(0, 0);
        int lowestDistance = Int.MaxValue();
        MapLocation currentLocation = rc.getLocation();
        for (MapLocation allySpawnLocation : rc.getAllySpawnLocations()) {
            int dist = Math.max(Math.abs(currentLocation.x - allySpawnLocation.x), Math.abs(currentLocation.y - allySpawnLocation.y));
            if (lowestDistance > dist) {
                lowestDistance = dist;
                closestLocation = allySpawnLocation;
            }
        }
        return closestLocation;
    }

    public static void updateEnemyRobots(RobotController rc) throws GameActionException {
        // Sensing methods can be passed in a radius of -1 to automatically 
        // use the largest possible value.
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemyRobots.length != 0) {
            // Save an array of locations with enemy robots in them for future use.
            MapLocation[] enemyLocations = new MapLocation[enemyRobots.length];
            for (int i = 0; i < enemyRobots.length; i++) {
                enemyLocations[i] = enemyRobots[i].getLocation();
            }
            // Let the rest of our team know how many enemy robots we see!
            if (rc.canWriteSharedArray(0, enemyLocations[0].x) && rc.canWriteSharedArray(0, enemyLocations[0].y)) {
                rc.writeSharedArray(0, enemyLocations[0].x);
                rc.writeSharedArray(1, enemyLocations[0].y);
            }
        } else {
            if (rc.senseBroadcastFlagLocations().length > 0) {
                int base = 0;
                if (rc.canWriteSharedArray(0, rc.senseBroadcastFlagLocations()[base].x) && rc.canWriteSharedArray(0, rc.senseBroadcastFlagLocations()[base].y)) {
                    rc.writeSharedArray(0, rc.senseBroadcastFlagLocations()[base].x);
                    rc.writeSharedArray(1, rc.senseBroadcastFlagLocations()[base].y);
                }
            }
        }
    }

    private static Direction getDirection(int x, int y) {
        if (x > 0) {
            if (y > 0) {
                return Direction.NORTHEAST;
            } else if (y < 0) {
                return Direction.SOUTHEAST;
            } else {
                return Direction.EAST;
            }
        } else if (x < 0) {
            if (y > 0) {
                return Direction.NORTHWEST;
            } else if (y < 0) {
                return Direction.SOUTHWEST;
            } else {
                return Direction.WEST;
            }
        } else {
            if (y > 0) {
                return Direction.NORTH;
            } else if (y < 0) {
                return Direction.SOUTH;
            } else {
                return Direction.CENTER;
            }
        }
    }


}
