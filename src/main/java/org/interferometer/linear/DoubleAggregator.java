package org.interferometer.linear;

import org.ojalgo.function.aggregator.AggregatorFunction;
import org.ojalgo.scalar.PrimitiveScalar;
import org.ojalgo.scalar.Scalar;

public abstract class DoubleAggregator implements AggregatorFunction<Double> {
    protected double myValue;

    public DoubleAggregator() {
        this.reset();
    }

    public double doubleValue() {
        return myValue;
    }

    public Double getNumber() {
        return Double.valueOf(this.doubleValue());
    }

    public int intValue() {
        return (int) this.doubleValue();
    }

    public void invoke(final Double anArg) {
        this.invoke(anArg.doubleValue());
    }

    public void merge(final Double anArg) {
        this.invoke(anArg.doubleValue());
    }

    abstract public double merge(double x, double y);

    public Double merge(final Double x, final Double y) {
        return Double.valueOf(merge(x.doubleValue(), y.doubleValue()));
    }

    public Scalar<Double> toScalar() {
        return new PrimitiveScalar(this.doubleValue());
    }

    public static DoubleAggregator MIN = new DoubleAggregator() {
        public AggregatorFunction<Double> reset() {
            myValue = Double.POSITIVE_INFINITY;
            return this;
        }

        public void invoke(double arg) {
            myValue = Math.min(myValue, arg);
        }

        public double merge(double x, double y) {
            return Math.min(x, y);
        }
    };

    public static DoubleAggregator MAX = new DoubleAggregator() {
        public AggregatorFunction<Double> reset() {
            myValue = Double.NEGATIVE_INFINITY;
            return this;
        }

        public void invoke(double arg) {
            myValue = Math.max(myValue, arg);
        }

        public double merge(double x, double y) {
            return Math.max(x, y);
        }
    };

    public static DoubleAggregator SUM = new DoubleAggregator() {
        public AggregatorFunction<Double> reset() {
            myValue = 0;
            return this;
        }

        public void invoke(double arg) {
            myValue += arg;
        }

        public double merge(double x, double y) {
            return x + y;
        }
    };

    public static DoubleAggregator SUM_SQUARES = new DoubleAggregator() {
        public AggregatorFunction<Double> reset() {
            myValue = 0;
            return this;
        }

        public void invoke(double arg) {
            myValue += arg * arg;
        }

        public double merge(double x, double y) {
            return x + y;
        }
    };
}
