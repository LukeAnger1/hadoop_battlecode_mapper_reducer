package dev;

import battlecode.common.*;
import dev.Communication.*;

import static dev.Moves.Attack.attackWithPriorityTo_Flag_InRange_Lowest;
import static dev.Moves.Build.fillLattice;
import static dev.Moves.Build.goToNearbyCrumbsAndFillWater;
import static dev.Moves.Heal.healWithPriorityTo_Flag_InRange_Lowest;
import static dev.Moves.Movement.*;
import static dev.Moves.Defend.*;
import static dev.Moves.Utils.getNumberOfNearbyTeammates;
import static dev.Moves.Utils.getNumberofNearbyEnemies;
import static dev.Parameters.RETREAT_HEALTH_THRESHOLD;
import static dev.RobotPlayer.rng;
import static dev.Moves.Utils.getNumberOfNearbyTeammates;
import static dev.Moves.Utils.getNumberofNearbyEnemies;
import static dev.Pathing.parents;
import static dev.RobotPlayer.BFSSink;

public class HealerBot extends BaseBot {

    public HealerBot(int duckNumber) {
        super(duckNumber);
    }

    @Override
    public void firstTurn(RobotController rc) throws GameActionException {
        super.firstTurn(rc);
    }

    @Override
    public void setupTurn(RobotController rc) throws GameActionException {
        super.setupTurn(rc);
    }

    @Override
    public void move(RobotController rc) throws GameActionException {
        if (!rc.isSpawned()) {
            spawn(rc, duckNumber);
        }

        if (rc.isSpawned()){
            updateSymmetry(rc);
            // alert of (approx) spawn loc under attack if enough enemies are nearby

            // go to any flags we see
            goToClosestNearbyFlagAndPickup(rc);
            boolean haveEnemyFlag = rc.hasFlag();
            if (haveEnemyFlag) {
				returnFlag(rc);
				return;
            }

            
            // dont mess up our own flag holders
            dontBlockFlagHolders(rc);
            // fight
//            if (rc.getHealth() < RETREAT_HEALTH_THRESHOLD){
//                retreat(rc);
//            }
            if (getNumberofNearbyEnemies(rc) > getNumberOfNearbyTeammates(rc)) {
                moveBehindOurAverageTrapPosition(rc);
            }
            healWithPriorityTo_Flag_InRange_Lowest(rc);
            attackWithPriorityTo_Flag_InRange_Lowest(rc);

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
    }

    @Override
    public Role getRole() {
        return Role.HEALER;
    }
}
