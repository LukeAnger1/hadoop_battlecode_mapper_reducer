package RoleSplitPlayer;

import battlecode.common.*;
import RoleSplitPlayer.Communication.Role;

import static RoleSplitPlayer.RobotPlayer.rng;

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
        } else {
            fillEverything(rc);
            updateSymmetry(rc);
            goToAndPickUpEnemyFlag(rc);
        }
    }

    @Override
    public void move(RobotController rc) throws GameActionException {
        if (!rc.isSpawned()) {
            spawn(rc, duckNumber);
        } else {

            updateSymmetry(rc);

            boolean haveEnemyFlag = rc.hasFlag();

            if (haveEnemyFlag){
                navigateTo(rc, getClosestSpawnLocation(rc));
            }
            else {
                dontBlockFlagHolders(rc);
                attackWithPriorityTo_Flag_InRange_Lowest(rc);
                healWithPriorityTo_Flag_InRange_Lowest(rc);
                goToAndPickUpEnemyFlag(rc);
                if (rng.nextInt() % 8 == 0) {
                    fillEverything(rc);
                }
            }

            moveRandomly(rc);
        }

        rc.setIndicatorString("AttackerBot");
    }

    @Override
    public Role getRole() {
        return Role.ATTACKER;
    }
}
