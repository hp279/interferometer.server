package org.interferometer;

import org.ojalgo.function.UnaryFunction;

public abstract class AbstractFunction implements UnaryFunction<Double> {
    public enum State {
        Error(-1), NotDefined(0), Defined(1);

        private byte myByte;

        State(final int aByte) {
            myByte = (byte) aByte;
        }
    }

    protected State state;

    protected AbstractFunction() {
        state = State.NotDefined;
    }

    protected void setError() {
        state = State.Error;
    }

    public Double invoke(Double x) {
        return this.invoke(x.doubleValue());
    }

    public double diff(double x, double h) {
        return (invoke(x + h) - invoke(x - h)) / (2 * h);
    }

    public double diff2(double x, double h) {
        return (invoke(x + h) - 2 * invoke(x) + invoke(x - h)) / (h * h);
    }
}