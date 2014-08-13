package org.interferometer.set;

public class Union extends FlatSet {
    FlatSet A, B;

    public Union(FlatSet A, FlatSet B) {
        this.A = A;
        this.B = B;
    }

    public boolean has(double x, double y) {
        return A.has(x, y) || B.has(x, y);
    }
}