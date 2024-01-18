package Sprint1;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import static Sprint1.Communication.*;
import static Sprint1.Moves.Attack.attackLowestInRange;
import static Sprint1.Moves.Build.buildTrapIfEnoughPlayers;
import static Sprint1.Moves.Build.fillEverything;
import static Sprint1.Moves.Heal.healLowestAllyInRange;

import static Sprint1.Moves.Defend.alertLocIfUnderAttack;

import static Sprint1.RobotPlayer.rng;
import static Sprint1.Parameters.*;

public class CamperBot extends BaseBot {

    static MapLocation campingSpot = null;

    public CamperBot(int duckNumber) {
        super(duckNumber);
    }

    @Override
    public void firstTurn(RobotController rc) throws GameActionException {
        firstSpawn(rc, duckNumber);
        navigateTo(rc, rc.senseNearbyFlags(4)[0].getLocation());
        campingSpot = rc.getLocation();
        rc.setIndicatorString("Camper: Going to Flag");
    }

    @Override
    public void setupTurn(RobotController rc) throws GameActionException {
        buildTrapIfEnoughPlayers(rc, CAMPER_ENEMY_THRESHOLD_SETUP_STUN, CAMPER_ENEMY_THRESHOLD_SETUP_BOMB);
        fillEverything(rc);
    }

    @Override
    public void move(RobotController rc) throws GameActionException {
        if (!rc.isSpawned()) {
            firstSpawn(rc, duckNumber);
        } else {
            // alert of (approx) spawn loc under attack if enough enemies are nearby
            alertLocIfUnderAttack(rc);

            buildTrapIfEnoughPlayers(rc, CAMPER_ENEMY_THRESHOLD_MOVE_STUN, CAMPER_ENEMY_THRESHOLD_MOVE_BOMB);
            // try to attack
            attackLowestInRange(rc);
            // try to heal
            healLowestAllyInRange(rc);
            // move back to camping spot
            if (campingSpot != null) {
                navigateTo(rc, campingSpot);
            }
            updateSymmetry(rc);
            if (rng.nextInt() % 8 == 0) {
                fillEverything(rc);
            }

            Communication.markUnderAttackLocationAsFree(rc);
        }

        rc.setIndicatorString("CamperBot");

    }

    @Override
    public Role getRole() {
        return Role.CAMPER;
    }
}

