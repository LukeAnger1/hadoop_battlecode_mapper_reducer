package yarobot;

import battlecode.common.*;
import static yarobot.RobotPlayer.*;

public strictfp class Communication {
    // 0: enemy spawn location 1
    // 1: enemy spawn location 2
    // 2: enemy spawn location 3
    public static void updateEnemySpawnLocation(RobotController rc, MapLocation location) throws GameActionException{
        int locationInt = convertMapLocationToInt(rc, location);
        // locations are updated in order, so if we find a location that is the same as the one we are trying to update, we can stop
        for (int i = 0; i < 3; i++){
            int currentLocationInt = rc.readSharedArray(i);
            MapLocation currentLocation = convertIntToMapLocation(rc, currentLocationInt);

            if (currentLocation.distanceSquaredTo(location) <= 2){
                break;
            }

            if (currentLocationInt == 0 ){
                if (rc.canWriteSharedArray(i, currentLocationInt)) {
                    rc.writeSharedArray(i, locationInt);
                }
                break;
            }
        }
    }

    public static MapLocation getClosestEnemySpawnLocation(RobotController rc) throws GameActionException{
        MapLocation closestLocation = null;
        int closestDist = 7200;
        for (int i = 0; i < 3; i++){
            int location = rc.readSharedArray(i);
            if (location != 0){
                MapLocation loc = convertIntToMapLocation(rc, location);
                int dist = rc.getLocation().distanceSquaredTo(loc);
                if (dist < closestDist){
                    closestDist = dist;
                    closestLocation = loc;
                }
            }
        }
        if (closestLocation != null) System.out.println("Closest enemy spawn location is " + closestLocation.toString());
        return closestLocation;
    }

    public static MapLocation convertIntToMapLocation(RobotController rc, int location){
        int mapWidth = rc.getMapWidth();
        int x = (location - 1) / mapWidth;
        int y = (location - 1) % mapWidth;
        return new MapLocation(x, y);
    }

    public static int convertMapLocationToInt(RobotController rc, MapLocation location){
        int x = location.x;
        int y = location.y;
        int mapWidth = rc.getMapWidth();
        return x * mapWidth + y + 1;
    }
}
