package Sprint1.Moves;

import battlecode.common.*;

public class Heal {

    static final int MOVE_AND_HEAL_RANGE_SQUARED = (int) Math.pow(Math.sqrt(GameConstants.HEAL_RADIUS_SQUARED) + 1, 2);


    //TODO fix moving towards full hp teammates.
    public static void healWithPriorityTo_Flag_InRange_Lowest(RobotController rc) throws GameActionException {
        RobotInfo[] nearbyTeammates = rc.senseNearbyRobots(-1, rc.getTeam());

        MapLocation loc = rc.getLocation();

        RobotInfo lowestFlagHolderInHealRange = null;
        int lowestFlagHolderInHealRangeHP = GameConstants.DEFAULT_HEALTH;

        RobotInfo lowestFlagHolderInMoveAndHealRange = null;
        int lowestFlagHolderInMoveAndHealRangeHP = GameConstants.DEFAULT_HEALTH;

        RobotInfo lowestInHealRange = null;
        int lowestInHealRangeHP = GameConstants.DEFAULT_HEALTH;

        RobotInfo lowestInMoveAndHealRange = null;
        int lowestInMoveAndHealRangeHP = GameConstants.DEFAULT_HEALTH;

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

    public static void healLowestAllyInRange(RobotController rc) throws GameActionException{
        RobotInfo[] allyRobots = rc.senseNearbyRobots(GameConstants.HEAL_RADIUS_SQUARED, rc.getTeam());
        MapLocation bestAllyLoc = null;
        int lowestHealth = Integer.MAX_VALUE;
        for (int i = 0; i < allyRobots.length; i++){
            // TODO: consider healing flag carrying bots, but may be better to just drop flag
            int tempRobotHealth = allyRobots[i].getHealth();
            MapLocation tempRobotLoc = allyRobots[i].getLocation();
            if (tempRobotHealth < lowestHealth && rc.canHeal(tempRobotLoc)){
                lowestHealth = tempRobotHealth;
                bestAllyLoc = tempRobotLoc;
            }
        }
        if (bestAllyLoc != null && rc.canHeal(bestAllyLoc)){
            rc.heal(bestAllyLoc);
        }
    }

}
