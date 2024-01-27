package dev.fast;

public class fastSet {
    // TODO IMPORTANT this needs the memory to be initialized to zero and it isnt yet!!!!!!
    private FastArrayStringInt data;
    private static final int bitsInSet = 1310654464;

    public fastSet () {
        data = new FastArrayStringInt(19999);
    }

    private int getIndexFast(Object key) {
        return key.hashCode() % bitsInSet;
    }

    // TODO: prevent intersection
    public void set (Object key, boolean value) {
        this.data.setBit(getIndexFast(key), value);
    }

    // TODO: prevent intersection
    public boolean get (Object key) {
        return this.data.getBit(getIndexFast(key));
    }
}
