package dev.fast;

import dev.fast.FastArrayStringInt.*;

public class fastDict {
    private FastArrayStringInt data;
    private static int maxLength;

    public fastDict () {
        data = new FastArrayStringInt(19999);
        maxLength = FastArrayStringInt.maxLength / 2;
    }

    private int getIndexFast(Object key) {
        return key.hashCode() % this.maxLength;
    }

    // TODO: prevent intersection
    public void set (Object key, int value) {
        this.data.set(getIndexFast(key), value);
    }

    // TODO: prevent intersection
    public int get (Object key) {
        return this.data.get(getIndexFast(key));
    }
}
