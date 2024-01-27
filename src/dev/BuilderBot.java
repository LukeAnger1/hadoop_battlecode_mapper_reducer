package dev;

import battlecode.common.*;
import dev.Communication.Role;

import static dev.Moves.Attack.attackWithPriorityTo_Flag_InRange_Lowest;
import static dev.Moves.Build.*;
import static dev.Moves.Heal.healWithPriorityTo_Flag_InRange_Lowest;
import static dev.Moves.Movement.*;
import static dev.Moves.Movement.moveRandomParticle;
import static dev.Moves.Movement.getClosestSpawnLocation;
import static dev.Moves.Movement.moveAwayFrom;
import static dev.Moves.Utils.*;
import static dev.RobotPlayer.rng;
import static dev.Parameters.*;
import static dev.Pathing.parents;
import static dev.RobotPlayer.BFSSink;

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
        fillLattice(rc);
        if (rc.getRoundNum() > 180) {
            goToAndPickUpEnemyFlag(rc);
        } else {
            moveRandomParticle(rc);
        }
        if (rc.getRoundNum() > 195){
            buildTrapIfEnoughPlayers(rc, BUILDER_ENEMY_THRESHOLD_SETUP_STUN, BUILDER_ENEMY_THRESHOLD_SETUP_BOMB);
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
            rc.setIndicatorString("BuilderBot");
            buildTrapIfEnoughPlayers(rc, BUILDER_ENEMY_THRESHOLD_MOVE_STUN, BUILDER_ENEMY_THRESHOLD_MOVE_BOMB);
            if (getNumberofNearbyEnemies(rc) > getNumberOfNearbyTeammates(rc)) {
                moveBehindOurAverageTrapPosition(rc);
            }
            attackWithPriorityTo_Flag_InRange_Lowest(rc);
            healWithPriorityTo_Flag_InRange_Lowest(rc);

            // crumbs
            goToNearbyCrumbsAndFillWater(rc);
            // go to flag
            navigateToPossibleEnemyFlagLocations(rc);
            if (rng.nextInt() % 8 == 0) {
                fillLattice(rc);
            }
            Communication.markUnderAttackLocationAsFree(rc);
        	farmBuildingLevel(rc, 1);
        }
    }

    @Override
    public Role getRole() {
        return Role.BUILDER;
    }
}
