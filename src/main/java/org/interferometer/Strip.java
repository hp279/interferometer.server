package org.interferometer;

import java.util.TreeSet;

public class Strip {

    TreeSet<IntPoint> pixels;

    public enum Type {
        Increasing, Decreasing, Undefined;
    }

    private Type type;

    public Strip(final Type type) {
        this.type = type;
        this.pixels = new TreeSet<IntPoint>();
    }

    public Strip() {
        this(Type.Undefined);
    }
}