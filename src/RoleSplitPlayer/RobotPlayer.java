package RoleSplitPlayer;

import battlecode.common.*;

import java.util.Random;

import static RoleSplitPlayer.Communication.roleSelection;

public strictfp class RobotPlayer {

    static final Random rng = new Random(6148);

    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };


    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        BaseBot bot = new BaseBot();

        while (true) {
            try {
                // round 1 code role selection + first actions
                if (rc.getRoundNum() == 1) {
                    bot = roleSelection(rc);
                    bot.firstTurn(rc);
                } else if (rc.getRoundNum() < GameConstants.SETUP_ROUNDS) {
                    bot.setupTurn(rc);
                } else {
                    if (rc.canBuyGlobal(GlobalUpgrade.ATTACK)) {
                        rc.buyGlobal(GlobalUpgrade.ATTACK);
                    } else if (rc.canBuyGlobal(GlobalUpgrade.HEALING)) {
                        rc.buyGlobal(GlobalUpgrade.HEALING);
                    } else if (rc.canBuyGlobal(GlobalUpgrade.CAPTURING)){
                        rc.buyGlobal(GlobalUpgrade.CAPTURING);
                    }
                    bot.move(rc);
                    bot = newRoleIfSpecialized(rc, bot.duckNumber, bot);
                }
            } catch (GameActionException e) {
                System.out.println("GameActionException Caused By: " + rc.getID() + " role: " + bot.getRole() + " location: " + rc.getLocation());
                e.printStackTrace();

            } catch (Exception e) {
                System.out.println("Exception Caused By: " + rc.getID() + " role: " + bot.getRole() + " location: " + rc.getLocation());
                e.printStackTrace();

            } finally {
                Clock.yield();
            }
        }
    }

    public static BaseBot newRoleIfSpecialized(RobotController rc, int duckNumber, BaseBot bot){
        if (rc.getLevel(SkillType.HEAL) >= 4){
            return new HealerBot(duckNumber);
        } else if (rc.getLevel(SkillType.ATTACK) >= 4) {
            return new AttackerBot(duckNumber);
        } else if (rc.getLevel(SkillType.BUILD) >= 4) {
            return new BuilderBot(duckNumber);
        } else {
            return bot; // dont change
        }
    }
}
