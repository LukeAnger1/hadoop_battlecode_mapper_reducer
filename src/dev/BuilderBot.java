package dev;

import battlecode.common.*;
import dev.Communication.Role;

import static dev.Moves.Attack.attackWithPriorityTo_Flag_InRange_Lowest;
import static dev.Moves.Build.*;
import static dev.Moves.Heal.healWithPriorityTo_Flag_InRange_Lowest;
import static dev.Moves.Movement.*;
import static dev.Moves.Movement.moveRandomly;
import static dev.Moves.Movement.getClosestSpawnLocation;
import static dev.Moves.Movement.moveAwayFrom;
import static dev.Moves.Utils.*;
import static dev.RobotPlayer.rng;
import dev.Parameters;

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
        // farm to level X
        // start placing traps close to round 200
        farmBuildingLevel(rc, 3);
        fillEverything(rc);
        if (rc.getRoundNum() > 180) {
            goToAndPickUpEnemyFlag(rc);
        } else {
            moveRandomly(rc);
        }
        if (rc.getRoundNum() > 195){
            buildTrapIfEnoughPlayers(rc, Parameters.BUILDER_ENEMY_THRESHOLD_SETUP_STUN, Parameters.BUILDER_ENEMY_THRESHOLD_SETUP_BOMB);
        }
    }



    @Override
    public void move(RobotController rc) throws GameActionException {
        if (!rc.isSpawned()) {
            spawn(rc, duckNumber);
        }

        if(rc.isSpawned()) {
            if (rc.getRoundNum() < 205){
                MapLocation avgEnemyLoc = getAverageEnemyLocation(rc, rc.senseNearbyRobots(-1));
                if (avgEnemyLoc != null) {
                    moveAwayFrom(rc, avgEnemyLoc);
                }
            }

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
            
            rc.setIndicatorString("BuilderBot");
            buildTrapIfEnoughPlayers(rc, Parameters.BUILDER_ENEMY_THRESHOLD_MOVE_STUN, Parameters.BUILDER_ENEMY_THRESHOLD_MOVE_BOMB);
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
            Communication.markUnderAttackLocationAsFree(rc);
        }
    }

    @Override
    public Role getRole() {
        return Role.BUILDER;
    }
}
