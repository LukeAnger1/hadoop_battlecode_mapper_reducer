package dev;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import dev.Communication.Role;

import static dev.RobotPlayer.BFSSink;
import static dev.Pathing.parents;
import static dev.Moves.Attack.attackWithPriorityTo_Flag_InRange_Lowest;
import static dev.Moves.Build.fillLattice;
import static dev.Moves.Build.goToNearbyCrumbsAndFillWater;
import static dev.Moves.Heal.healWithPriorityTo_Flag_InRange_Lowest;
import static dev.Moves.Movement.*;
import static dev.Moves.Utils.getNumberOfNearbyTeammates;
import static dev.Moves.Utils.getNumberofNearbyEnemies;
import static dev.Parameters.RETREAT_HEALTH_THRESHOLD;
import static dev.RobotPlayer.rng;

public class AttackerBot extends BaseBot {

    public AttackerBot(int duckNumber) {
        super(duckNumber);
    }

    @Override
    public void firstTurn(RobotController rc) throws GameActionException {
        super.firstTurn(rc);
    }

    @Override
    public void setupTurn(RobotController rc) throws GameActionException {
		super.setupTurn(rc);
        if (rc.isSpawned() && rc.getRoundNum() > 180) {
            goToAndPickUpEnemyFlag(rc);
        }
    }

    @Override
    public void move(RobotController rc) throws GameActionException {
        if (!rc.isSpawned()) {
            spawn(rc, duckNumber);
        }

        if (rc.isSpawned()){
            updateSymmetry(rc);

            // pick up any we are in range for and head back
            pickUpFlags(rc);
            boolean haveEnemyFlag = rc.hasFlag();
            if (haveEnemyFlag) {
				returnFlag(rc);
            }
            
            // dont mess up our own flag holders
            dontBlockFlagHolders(rc);
//            if (rc.getHealth() < RETREAT_HEALTH_THRESHOLD){
//                retreat(rc);
//            }
            // fight
            if (getNumberofNearbyEnemies(rc) > getNumberOfNearbyTeammates(rc)) {
                moveBehindOurAverageTrapPosition(rc);
            }
            attackWithPriorityTo_Flag_InRange_Lowest(rc);
            healWithPriorityTo_Flag_InRange_Lowest(rc);

            // go to any flags we see
            goToClosestNearbyFlagAndPickup(rc);
            // crumbs
            goToNearbyCrumbsAndFillWater(rc);
            // go to flag
            navigateToPossibleEnemyFlagLocations(rc);
            if (rng.nextInt() % 8 == 0) {
                fillLattice(rc);
            }


            moveRandomParticle(rc);
            Communication.markUnderAttackLocationAsFree(rc);
        }

        rc.setIndicatorString("AttackerBot");
    }

    @Override
    public Role getRole() {
        return Role.ATTACKER;
    }
}
