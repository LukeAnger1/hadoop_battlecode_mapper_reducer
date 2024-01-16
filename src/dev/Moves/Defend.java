package dev.Moves;

import battlecode.common.*;
import dev.Communication;

import dev.Parameters;

public class Defend {
    public static void alertLocIfUnderAttack(RobotController rc) throws GameActionException {
        if (Communication.underAttack(rc)) return;
        MapLocation currentLoc = rc.getLocation();
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemies.length >= Parameters.UNDER_ATTACK_ENEMY_THRESHOLD && enemies.length < Parameters.GIVE_UP_UNDER_ATTACK_THRESHOLD) {
            Communication.updateUnderAttackLocation(rc, currentLoc);
        }
    }


}
