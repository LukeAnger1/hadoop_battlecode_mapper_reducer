package Sprint1.Moves;

import battlecode.common.*;
import Sprint1.Communication;

import static Sprint1.Parameters.GIVE_UP_UNDER_ATTACK_THRESHOLD;
import static Sprint1.Parameters.UNDER_ATTACK_ENEMY_THRESHOLD;

public class Defend {
    public static void alertLocIfUnderAttack(RobotController rc) throws GameActionException {
        if (Communication.underAttack(rc)) return;
        MapLocation currentLoc = rc.getLocation();
        RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemies.length >= UNDER_ATTACK_ENEMY_THRESHOLD && enemies.length < GIVE_UP_UNDER_ATTACK_THRESHOLD) {
            Communication.updateUnderAttackLocation(rc, currentLoc);
        }
    }


}
