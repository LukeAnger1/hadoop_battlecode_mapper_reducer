package dev;

import battlecode.common.*;

import java.util.Random;

import static dev.Communication.roleSelection;
import static dev.Communication.Role;
import static dev.Moves.Movement.getClosestSpawnLocation;
import static dev.Pathing.bfs;

public strictfp class RobotPlayer {

    public static Random rng = null;
    public static BaseBot bot;

    public static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    public static MapLocation BFSSink;
    static final byte M_HIDDEN = 0b0000;
	static final byte M_EMPTY = 0b0001;
	static final byte M_WALL = 0b0010;
    public static byte[][] map = new byte[60][60];
    public static int height = 0;
    public static int width = 0;
    public static int explored_counter = 0;

    public static void printMap() {
		String out = "\n";
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				out = out + map[i][j];
			}
			out = out + "\n";
		}
		System.out.println(out);
	}

    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        rng = new Random(rc.getID());
        bot = new BaseBot();

        while (true) {
            try {
                if (rc.isSpawned()) {
                    MapInfo[] tiles = rc.senseNearbyMapInfos();
                    for (int i = 0; i < tiles.length; i++) {
                        MapInfo tile = tiles[i];
                        MapLocation loc = tile.getMapLocation();
                        if (map[loc.x][loc.y] == M_HIDDEN)
                            explored_counter++;
                        if (tile.isWall()) {
                            map[loc.x][loc.y] = M_WALL;
                        } else {
                            map[loc.x][loc.y] = M_EMPTY;
                        }
                    }
                }
                // round 1 code role selection + first actions
                if (rc.getRoundNum() == 1) {
                    height = rc.getMapHeight(); // Y dim
                    width = rc.getMapWidth(); // X dim
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
                while (((float)explored_counter)/(width*height) > 0.25 && BFSSink != null && Clock.getBytecodeNum() < GameConstants.BYTECODE_LIMIT * 0.8) {
                    rc.setIndicatorDot(BFSSink, 255, 0, 0);
                    bfs(rc, BFSSink);
                }
                Clock.yield();
            }
        }
    }

    public static BaseBot newRoleIfSpecialized(RobotController rc, int duckNumber, BaseBot bot){

        if (bot.getRole() == Role.CAMPER) return bot;

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
