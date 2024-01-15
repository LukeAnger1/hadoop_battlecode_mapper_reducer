package RoleSplitPlayer;

import battlecode.common.*;
import RoleSplitPlayer.Communication.Role;

import static RoleSplitPlayer.RobotPlayer.rng;

public class BuilderBot extends BaseBot {

    public BuilderBot(int duckNumber) {
        super(duckNumber);
    }

    @Override
    public void firstTurn(RobotController rc) throws GameActionException {
        super.firstTurn(rc);
    }

    @Override
    public void setupTurn(RobotController rc) throws GameActionException {
        fillEverything(rc);
    }

    @Override
    public void move(RobotController rc) throws GameActionException {
        if (!rc.isSpawned()) {
            spawn(rc, duckNumber);
        }

        if(rc.isSpawned()) {

            if (rc.hasFlag()){
                navigateTo(rc, getClosestSpawnLocation(rc));
            }
            updateSymmetry(rc);

            rc.setIndicatorString("BuilderBot");
            buildTrapIfEnoughPlayers(rc, 2, 4);
            attackWithPriorityTo_Flag_InRange_Lowest(rc);
            healWithPriorityTo_Flag_InRange_Lowest(rc);
            goToAndPickUpEnemyFlag(rc);

            if (rng.nextInt() % 8 == 0) {
                fillEverything(rc);
            }
        }
    }

    @Override
    public Role getRole() {
        return Role.BUILDER;
    }

}
