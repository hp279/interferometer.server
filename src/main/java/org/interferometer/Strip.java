package org.interferometer;

import java.util.TreeSet;

public class Strip {

    TreeSet<Pixel> pixels;

    public enum Type {
        Increasing, Decreasing, Undefined;
    }

    private Type type;

    public Strip(final Type type) {
        this.type = type;
        this.pixels = new TreeSet<Pixel>();
    }

    public Strip() {
        this(Type.Undefined);
    }
}