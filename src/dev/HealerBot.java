package dev;

import battlecode.common.*;
import dev.Communication.*;

import static dev.Moves.Attack.attackWithPriorityTo_Flag_InRange_Lowest;
import static dev.Moves.Build.fillEverything;
import static dev.Moves.Build.goToNearbyCrumbsAndFillWater;
import static dev.Moves.Heal.healWithPriorityTo_Flag_InRange_Lowest;
import static dev.Moves.Movement.*;
import static dev.Moves.Utils.getNumberOfNearbyTeammates;
import static dev.Moves.Utils.getNumberofNearbyEnemies;
import static dev.RobotPlayer.rng;

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

            // pick up any we are in range for and head back
            pickUpFlags(rc);
            boolean haveEnemyFlag = rc.hasFlag();
            if (haveEnemyFlag){
                navigateTo(rc, getClosestSpawnLocation(rc));
            }

            // dont mess up our own flag holders
            dontBlockFlagHolders(rc);
            // fight
            if (getNumberofNearbyEnemies(rc) > getNumberOfNearbyTeammates(rc)) {
                moveBehindOurAverageTrapPosition(rc);
            }
            healWithPriorityTo_Flag_InRange_Lowest(rc);
            attackWithPriorityTo_Flag_InRange_Lowest(rc);

            // go to any flags we see
            goToClosestNearbyFlagAndPickup(rc);
            // crumbs
            goToNearbyCrumbsAndFillWater(rc);
            // go to flag
            navigateToPossibleEnemyFlagLocations(rc);
            if (rng.nextInt() % 8 == 0) {
                fillEverything(rc);
            }


            moveRandomly(rc);
        }
    }

    @Override
    public Role getRole() {
        return Role.HEALER;
    }
}
