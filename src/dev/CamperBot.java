package dev;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import static dev.Communication.*;
import static dev.Moves.Attack.attackLowestInRange;
import static dev.Moves.Build.buildTrapIfEnoughPlayers;
import static dev.Moves.Build.fillLattice;
import static dev.Moves.Build.farmBuildingLevel;
import static dev.Moves.Heal.healLowestAllyInRange;

import static dev.Moves.Defend.alertLocIfUnderAttack;
import static dev.Moves.Movement.moveRandomly;
import static dev.Moves.Utils.eliminateSymmetryFast;
import static dev.RobotPlayer.rng;
import static dev.Parameters.*;
import static dev.Moves.Movement.moveRandomly;

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
        eliminateSymmetryFast(rc);
        rc.setIndicatorString("Camper: Going to Flag");
    }

    @Override
    public void setupTurn(RobotController rc) throws GameActionException {
        buildTrapIfEnoughPlayers(rc, CAMPER_ENEMY_THRESHOLD_SETUP_STUN, CAMPER_ENEMY_THRESHOLD_SETUP_BOMB);
        fillLattice(rc);
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
                fillLattice(rc);
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

