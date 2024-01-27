package Sprint1;

import battlecode.common.*;
import Sprint1.Communication.*;

import static Sprint1.Moves.Attack.attackWithPriorityTo_Flag_InRange_Lowest;
import static Sprint1.Moves.Build.fillEverything;
import static Sprint1.Moves.Build.goToNearbyCrumbsAndFillWater;
import static Sprint1.Moves.Heal.healWithPriorityTo_Flag_InRange_Lowest;
import static Sprint1.Moves.Movement.*;

import static Sprint1.Moves.Utils.getNumberOfNearbyTeammates;
import static Sprint1.Moves.Utils.getNumberofNearbyEnemies;

import static Sprint1.RobotPlayer.rng;

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
            Communication.markUnderAttackLocationAsFree(rc);
        }
    }

    @Override
    public Role getRole() {
        return Role.HEALER;
    }
}
