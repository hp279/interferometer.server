package org.interferometer;

import org.ojalgo.function.BinaryFunction;

// delta f(x) = f(x) - f(x - s)
public class Shift1Function2 extends AbstractFunction2 {
    BinaryFunction<Double> f;
    double s;

    public Shift1Function2(BinaryFunction<Double> f, double s) {
        this.f = f;
        this.s = s;
    }

    public double invoke(double x, double y) {
        return f.invoke(x, y) - f.invoke(x - s, y);
    }
}