package Sprint1.fast;

import battlecode.common.*;

public class FastIntMap {
    // We are using chunks but we can also seperate by x and y
    // Can change the chunk size stuff for optimization, ex of setup for hadoop cluster below
    // private static final int CHUNK_SIZE = <?CHUNK_SIZE?>;
    private static final int CHUNK_SIZE = 64;
    // TODO: instead of a list of booleans we can save as list of ints for example then extract the value
    private boolean[][] data;
    private int width;
    private int height;

    public FastIntMap(int maxSize) {
        this.data = new boolean[maxSize / 64 + 1][];
    }

    // TODO: test optimization
    public FastIntMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.data = new boolean[(width * height) / 64 + 1][];
    }

    public void set(int index, boolean data) {
        int chunkIndex = index / CHUNK_SIZE;
        boolean[] array = this.data[chunkIndex];
        if (array == null) {
            array = new boolean[CHUNK_SIZE];
            this.data[chunkIndex] = array;
        }
        array[index % CHUNK_SIZE] = data;
    }

    // TODO: test optimization
    public int mapToInt(MapLocation loc) {
        return loc.y * this.width + loc.x;
    }

    // TODO: test optimization
    public void set(MapLocation loc, boolean data) {
        assert (this.width > 0) : "The width is set to an illegal value. This code be due to using the maxSize constructor";
        assert (this.height > 0) : "The height is set to an illegal value. This code be due to using the maxSize constructor";
        // TODO: This sets the chunks as lines but we can consider setting them in a grid
        set(mapToInt(loc), data);
    }

    public boolean get(int index) {
        int chunkIndex = index / CHUNK_SIZE;
        boolean[] array = this.data[chunkIndex];
        if (array == null) {
            array = new boolean[CHUNK_SIZE];
            this.data[chunkIndex] = array;
        }
        return array[index % CHUNK_SIZE];
    }

    // TODO: test optimization
    public boolean get(MapLocation loc) {
        assert (this.width > 0) : "The width is set to an illegal value. This code be due to using the maxSize constructor";
        assert (this.height > 0) : "The height is set to an illegal value. This code be due to using the maxSize constructor";
        return get(mapToInt(loc));
    }

    public static void main (String args []) {
        // This is for testing
        FastIntMap map = new FastIntMap(30, 60);
        map.set(new MapLocation(1, 3), true);
        System.out.println(map.get(new MapLocation(2, 2)));
        System.out.println(map.get(new MapLocation(1, 3)));
    }
}