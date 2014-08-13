package org.interferometer;

import org.ojalgo.function.UnaryFunction;

// delta f(x) = f(x) - f(x - s)
public class ShiftFunction extends AbstractFunction {
    UnaryFunction<Double> f;
    double s;

    public ShiftFunction(UnaryFunction<Double> f, double s) {
        this.f = f;
        this.s = s;
    }

    public double invoke(double x) {
        return f.invoke(x) - f.invoke(x - s);
    }
}