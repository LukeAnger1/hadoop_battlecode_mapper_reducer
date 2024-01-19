package dev;

import battlecode.common.*;
import dev.Communication.Role;
import static dev.RobotPlayer.*;
import static dev.Moves.Movement.getClosestSpawnLocation;
import java.util.Queue;
import java.util.HashMap;
import java.util.LinkedList;

public class Pathing {

    // pathfinding state
	static Direction currentDirection = directions[RobotPlayer.rng.nextInt(directions.length)];
	static boolean onObstacle = false;
	static int maxDistSquared = 7200;
	static int checkPointSquared = maxDistSquared;
	static int navCount = 0;
    static MapLocation startPoint;
    static MapLocation goalLoc;
    static MapLocation hitPoint;
    static float slope;
    static float yIntercept;
    static boolean wallMode = false;
    static MapLocation lastLoc = null;
    static int stuckCounter = 0;
    static MapLocation undefined_loc = new MapLocation(-1, -1);
    static Queue<MapLocation> bfsQ =  new LinkedList<>();
    static Queue<MapLocation> hidden = new LinkedList<>();
    public static HashMap<MapLocation, MapLocation> parents = new HashMap<>();
    static int currentPathIdx = -1;
    // static MapLocation pSrc;
    static MapLocation pSink;

    // Navigation
    static void navigateTo(RobotController rc, MapLocation loc) throws GameActionException {
        rc.setIndicatorLine(rc.getLocation(), loc, 255, 0, 0);
		if (rc.isMovementReady()) {
     	   if (BFSSink != null && loc.equals(BFSSink) && bot.getRole() != Role.CAMPER) {
     	        MapLocation me = rc.getLocation();
     	        if (parents.containsKey(me)) {
     	            MapLocation goal = parents.get(me);
				    rc.setIndicatorLine(rc.getLocation(), goal, 0, 255, 0);
     	            tryMove(rc, me.directionTo(goal));
     	            while (goal != null && goal != BFSSink) {
     	                rc.setIndicatorDot(goal, 255, 0, 0);
     	                goal = parents.get(goal);
     	            }
     	        }
     	    } else {
     	        bug2(rc, loc);
     	   }
		}
    }

	static boolean isPassable(RobotController rc, Direction dir) throws GameActionException {
		MapLocation loc = rc.adjacentLocation(dir);
		if (!rc.onTheMap(loc))
			return false;
		MapInfo info = rc.senseMapInfo(loc);	
		return info.isPassable();
	}

    static boolean tryMove(RobotController rc, Direction dir) throws GameActionException {

        if (isPassable(rc, dir) && rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else {
            Direction right = dir.rotateRight();
            if (isPassable(rc, right) && rc.canMove(right)) {
                rc.move(right);
                return true;
            }
            Direction left = dir.rotateLeft();
            if (isPassable(rc, left) && rc.canMove(left)) {
                rc.move(left);
                return true;
            }
        }
        return false;	
    }

    static void turnRight() throws GameActionException {
        currentDirection = currentDirection.rotateRight();
    }	

    static void turnLeft() throws GameActionException {
        currentDirection = currentDirection.rotateLeft();
    }

    static int directionToIndex(Direction dir) throws GameActionException {
        for (int i = 0; i < directions.length; i++) {
            if (directions[i] == dir) {
                return i;
            }
        }
        // should never happen
        return -1;
    }

    static Direction oppositeDirection (Direction dir) {
        switch (dir) {
            case NORTH:
                return Direction.SOUTH;
            case NORTHEAST:
                return Direction.SOUTHWEST;
            case SOUTHEAST:
                return Direction.NORTHWEST;
            case SOUTH:
                return Direction.NORTH;
            case EAST:
                return Direction.WEST;
            case WEST:
                return Direction.EAST;
            case SOUTHWEST:
                return Direction.NORTHEAST;
            default:
                return Direction.SOUTHEAST;
        }
    }

    static boolean senseRight(RobotController rc) throws GameActionException {
        Direction senseDir = currentDirection.rotateRight().rotateRight(); 
        return isPassable(rc, senseDir);
    }

    static boolean senseFrontRight(RobotController rc) throws GameActionException {
        Direction senseDir = currentDirection.rotateRight(); 
        return isPassable(rc, senseDir);
    }

    static boolean senseFront(RobotController rc) throws GameActionException {
        return isPassable(rc, currentDirection);
    }

    static void bugRandom(RobotController rc, MapLocation loc) throws GameActionException {
        // head towards goal
        Direction goalDir = rc.getLocation().directionTo(loc);
        if (rc.canMove(goalDir)) {
            rc.move(goalDir);
            return;
        } else { // indicates obstacle
            // move random
            Direction dir = directions[rng.nextInt(8)];
            if (rc.canMove(dir)) {
                rc.move(dir);
            }
        }
    }

    static boolean touchingObstacle(RobotController rc) throws GameActionException {
        Direction rightHandDir = currentDirection.rotateRight().rotateRight();
        return !isPassable(rc, rightHandDir) || !isPassable(rc, rightHandDir.rotateRight()) || !isPassable(rc, rightHandDir.rotateLeft());
    }

    static void bug2(RobotController rc, MapLocation loc) throws GameActionException {
        //write this path finding algorithm based on the following pseudocode
        // 		On a high level, the Bug2 algorithm has two main modes:

        // Go to Goal Mode: Move from the current location towards the goal (x,y) coordinate.
        // Wall Following Mode: Move along a wall.
        // Here is pseudocode for the algorithm:

        // 1.      Calculate a start-goal line. The start-goal line is an imaginary line that connects the starting position to the goal position.

        // 2.      While Not at the Goal

        // Move towards the goal along the start-goal line.
        // If a wall is encountered:
        // Remember the location where the wall was first encountered. This is the "hit point."
        // Follow the wall until you encounter the start-goal line. This point is known as the "leave point."
        //  If the leave point is closer to the goal than the hit point, leave the wall, and move towards the goal again.
        // Otherwise, continue following the wall.
        // 3.      When the goal is reached, stop.
        int dist = rc.getLocation().distanceSquaredTo(loc);
        if (dist < 1) {
            return;
        }

        // calculate a start-goal line
        if (goalLoc == null || !goalLoc.equals(loc)) {
            goalLoc = loc;
            currentDirection = rc.getLocation().directionTo(goalLoc);
            startPoint = rc.getLocation();
            slope = (goalLoc.y - startPoint.y) / (goalLoc.x - startPoint.x + 0.001f);
            yIntercept = startPoint.y - slope * startPoint.x;
            hitPoint = null;
            wallMode = false;
            navCount = 0;
        }
        navCount++;
        Direction goalDir = rc.getLocation().directionTo(goalLoc);

        // head towards goal
        if (!wallMode && !tryMove(rc, goalDir) && rc.getMovementCooldownTurns() == 0) {
            // if we're in this block, we couldn't move in the direction of the goal
            if (!isPassable(rc, goalDir)) {
                wallMode = true;
                currentDirection = currentDirection.rotateLeft().rotateLeft();
                hitPoint = rc.getLocation();
            } else {
                Direction a = currentDirection.rotateLeft();
                if (rc.canMove(a)) {
                    rc.move(a);
                    currentDirection = a;
                } else {
                    a = a.rotateLeft();
                    if (rc.canMove(a)) {
                        rc.move(a);
                        currentDirection = a;
                    } else {
                        a = a.rotateLeft();
                        if (rc.canMove(a)) {
                            rc.move(a);
                            currentDirection = a;
                        } else {
                            a = a.rotateLeft();
                            if (rc.canMove(a)) {
                                rc.move(a);
                                currentDirection = a;
                            } else {
                                a = a.rotateLeft();
                                if (rc.canMove(a)) {
                                    rc.move(a);
                                    currentDirection = a;
                                } else {
                                    a = a.rotateLeft();
                                    if (rc.canMove(a)) {
                                        rc.move(a);
                                        currentDirection = a;
                                    } else {
                                        a = a.rotateLeft();
                                        if (rc.canMove(a)) {
                                            rc.move(a);
                                            currentDirection = a;
                                        } else {
                                            a = a.rotateLeft();
                                            if (rc.canMove(a)) {
                                                rc.move(a);
                                                currentDirection = a;
                                            }
                                        }
                                    } 
                                }
                            }
                        }
                    }
                }
                if (rng.nextInt() % 3 == 0){
                    currentDirection = currentDirection.rotateLeft();
                }
            }
        }
        
        if (wallMode) {
            // check if we are on the line
            if (onMLine(rc.getLocation()) && rc.getLocation().distanceSquaredTo(goalLoc) < hitPoint.distanceSquaredTo(goalLoc)) {
                // if we are on the line, we are done wall following
                wallMode = false;
                return;
            }
            // follow obstacle using right hand rule
            boolean frontRight = senseFrontRight(rc);
            boolean front = senseFront(rc);
            boolean right = senseRight(rc);
            if (right) {
                Direction moveDirection = currentDirection.rotateRight().rotateRight();
                if (tryMove(rc, moveDirection)) {
                    currentDirection = moveDirection;
                } else if (rc.getMovementCooldownTurns() == 0) {
                    bugRandom(rc, goalLoc);
                }
            } else if (frontRight) {
                Direction moveDirection = currentDirection.rotateRight();
                if (tryMove(rc, moveDirection)) {
                    currentDirection = moveDirection;
                } else if (rc.getMovementCooldownTurns() == 0){
                    bugRandom(rc, goalLoc);
                }
            } else if (front) {
                tryMove(rc, currentDirection);
            } else {
                turnLeft();
            }
        }

        if (!touchingObstacle(rc) || navCount > 200)
            goalLoc = null;

        if (stuckCounter > 10) {
            for (int i = directions.length; --i >= 0;)
                if (rc.canMove(directions[i])) {
                    rc.move(directions[i]);
                    currentDirection = directions[i];
                }
        }

        if (lastLoc == null)
            lastLoc = rc.getLocation();

        if (!rc.getLocation().equals(lastLoc)) {
            lastLoc = null;
            stuckCounter = 0;
        } else {
            stuckCounter++;
        }
    }

	static float abs(float x) {
		return x < 0 ? -x: x;
	}

    static boolean onMLine(MapLocation loc) {
        float epsilon = 3.5f;
        return abs(loc.y - (slope * loc.x + yIntercept)) < epsilon;
    }

    static boolean bfs(RobotController rc, MapLocation sink) throws GameActionException {
        // System.out.println("computed: " + parents.size());
        if (pSink == null || !sink.equals(pSink)) {
            // System.out.println("clearing BFS");
            // System.out.println("sink: " + sink);
            pSink = sink;
            parents.clear();
            bfsQ.clear();
        } else if (bfsQ.size() == 0) {
            if (!parents.containsKey(sink)) { // initialize sink
                rc.setIndicatorDot(sink, 255, 0, 0);
                bfsQ.add(sink);
                parents.put(sink, undefined_loc);
            } else if (!rc.isSpawned()) {    
                parents.clear();
                bfsQ.clear();
            }
            else if (hidden.size() > 10) {
                MapLocation tile = hidden.remove();
                if (map[tile.x][tile.y] != M_HIDDEN) {
                    bfsQ.add(tile);
                } else {
                    hidden.add(tile);
                }
            }
        } else if (bfsQ.size() > 0){
            MapLocation next = bfsQ.remove();
            if (map[next.x][next.y] == M_WALL) {
                parents.remove(next);
                return false;
            }
            rc.setIndicatorDot(next, 0, 255, 0);
            for (int i = directions.length; --i >= 0;) {
                MapLocation adj = next.add(directions[i]);
                if (!rc.onTheMap(adj))
                    continue;
                byte tile = map[adj.x][adj.y];
                switch (tile) {
                    case M_HIDDEN:
                        parents.put(adj, next);
                        hidden.add(adj);
                        continue;
                    case M_WALL:
                        continue;
                    default:
                }
                if (!parents.containsKey(adj)) {
                    bfsQ.add(adj);
                    parents.put(adj, next);
                }
            }
        }
        return false;
    }
}
