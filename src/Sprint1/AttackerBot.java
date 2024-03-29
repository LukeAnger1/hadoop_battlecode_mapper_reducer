package Sprint1;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import Sprint1.Communication.Role;

import static Sprint1.Moves.Attack.attackWithPriorityTo_Flag_InRange_Lowest;
import static Sprint1.Moves.Build.fillEverything;
import static Sprint1.Moves.Build.goToNearbyCrumbsAndFillWater;
import static Sprint1.Moves.Heal.healWithPriorityTo_Flag_InRange_Lowest;
import static Sprint1.Moves.Movement.*;
import static Sprint1.Moves.Utils.getNumberOfNearbyTeammates;
import static Sprint1.Moves.Utils.getNumberofNearbyEnemies;
import static Sprint1.RobotPlayer.rng;

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
        if (!rc.isSpawned()) {
            spawn(rc, duckNumber);
        }

        if (rc.isSpawned()) {
            fillEverything(rc);
            updateSymmetry(rc);
            if (rc.getRoundNum() > 180) {
                goToAndPickUpEnemyFlag(rc);
            }
            moveRandomly(rc);
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
            if (haveEnemyFlag){
                navigateTo(rc, getClosestSpawnLocation(rc));
            }

            
            // dont mess up our own flag holders
            dontBlockFlagHolders(rc);
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
                fillEverything(rc);
            }


            moveRandomly(rc);
            Communication.markUnderAttackLocationAsFree(rc);
        }

        rc.setIndicatorString("AttackerBot");
    }

    @Override
    public Role getRole() {
        return Role.ATTACKER;
    }
}
