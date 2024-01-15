package RoleSplitPlayer;

import battlecode.common.*;
import RoleSplitPlayer.Communication.*;

import static RoleSplitPlayer.RobotPlayer.rng;

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
        } else {
            updateSymmetry(rc);
            boolean haveEnemyFlag = rc.hasFlag();
            MapLocation loc = rc.getLocation();

            if (haveEnemyFlag) {
                navigateTo(rc, getClosestSpawnLocation(rc));
            } else {
                dontBlockFlagHolders(rc);
                healWithPriorityTo_Flag_InRange_Lowest(rc);
                attackWithPriorityTo_Flag_InRange_Lowest(rc);
                goToAndPickUpEnemyFlag(rc);
                if (rng.nextInt() % 8 == 0) {
                    fillEverything(rc);
                }
            }
            moveRandomly(rc);
            rc.setIndicatorString("HealerBot");
        }
    }

    @Override
    public Role getRole() {
        return Role.HEALER;
    }
}
