package RoleSplitPlayer;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import static RoleSplitPlayer.Communication.*;
import static RoleSplitPlayer.RobotPlayer.rng;

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
        super.setupTurn(rc);
        buildTrapIfEnoughPlayers(rc, 0, 1);
        fillEverything(rc);
    }

    @Override
    public void move(RobotController rc) throws GameActionException {
        if (!rc.isSpawned()) {
            firstSpawn(rc, duckNumber);
        } else {
            buildTrapIfEnoughPlayers(rc, 1, 3);
            // try to attack
            attackWithPriorityTo_Flag_InRange_Lowest(rc);
            // try to heal
            healWithPriorityTo_Flag_InRange_Lowest(rc);
            // move back to camping spot
            if (campingSpot != null) {
                navigateTo(rc, campingSpot);
            }
            updateSymmetry(rc);
            if (rng.nextInt() % 8 == 0) {
                fillEverything(rc);
            }
        }

        rc.setIndicatorString("CamperBot");

    }

    @Override
    public Role getRole() {
        return Role.CAMPER;
    }
}

