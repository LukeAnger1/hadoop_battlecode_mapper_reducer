package L;

import battlecode.common.*;
import static L.RobotPlayer.*;
import java.util.HashSet;

public class FlagBot {
    public static FlagInfo getClosestFlagAndPickUpIfCan(RobotController rc) throws GameActionException{
        // TODO: use this function senseBroadcastFlagLocations()
        // TODO: I don't like how it picks up but like the short cut so will fix later
        FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        // maintain closest flag in case we can't pick any up so that we can move towards it
        FlagInfo closestFlag = null;
        int closestDist = Integer.MAX_VALUE;
        for (FlagInfo flag : flags){
            if (rc.canPickupFlag(flag.getLocation())){
                rc.pickupFlag(flag.getLocation());
                rc.setIndicatorString("Holding a flag!");
                return null;
            }
            else{
                // We can use distance squared to but I would rather use taxi distance
                int dist = Nav.taxiDistance(rc.getLocation(), flag.getLocation());
                if (dist < closestDist){
                    closestDist = dist;
                    closestFlag = flag;
                }
            }
        }
        return null;
    }

    public static void goToAndPickUpEnemyFlag(RobotController rc) throws GameActionException{
        // FlagInfo[] flags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
        // maintain closest flag in case we can't pick any up so that we can move towards it
        FlagInfo closestFlag = getClosestFlagAndPickUpIfCan(rc);

        if (closestFlag != null){
            Nav.navigateTo(rc, closestFlag.getLocation());
        }
        else {
            // See if we know about an enemy spawn first
            MapLocation enemySpawnLocation = Communication.getClosestEnemySpawnLocation(rc);
            // if we're near this location and don't see a flag there, then let's not go there for now
            if (enemySpawnLocation != null && !enemySpawnLocation.equals(Nav.ignoreLocation) && !(rc.getLocation().distanceSquaredTo(enemySpawnLocation) <= 2)){
                Nav.navigateTo(rc, enemySpawnLocation);
            }
            else if (enemySpawnLocation != null && !enemySpawnLocation.equals(Nav.ignoreLocation)) {
                Nav.ignoreLocation = enemySpawnLocation;
            }
            // choose a symmetry that is true and navigate to it
            if (Nav.mirrors[2]){
                Nav.navigateTo(rc, Nav.oppositeOfSpawnLocation);
            }
            else if (Nav.mirrors[1]){
                Nav.navigateTo(rc, Nav.verticalMirrorLocation);
            }
            else if (Nav.mirrors[0]){
                Nav.navigateTo(rc, Nav.horizontalMirrorLocation);
            }
        }
    }
}
