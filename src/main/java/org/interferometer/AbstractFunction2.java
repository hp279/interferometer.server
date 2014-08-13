package org.interferometer;

import org.ojalgo.function.BinaryFunction;

public abstract class AbstractFunction2 implements BinaryFunction<Double> {
    public Double invoke(Double x, Double y) {
        return this.invoke(x.doubleValue(), y.doubleValue());
    }

    // градиент - вектор из 2 аргументов
    public double[] diff(double x, double y, double h) {
        double[] result = new double[2];
        result[0] = (invoke(x + h, y) - invoke(x - h, y)) / (2 * h);
        result[1] = (invoke(x, y + h) - invoke(x, y - h)) / (2 * h);
        return result;
    }

    // матрица 2-х производных - размер 2 на 2
    public double[][] diff2(double x, double y, double h) {
        double[][] result = new double[2][2];
        double hh4 = 4 * h * h;
        result[0][0] = (invoke(x + 2 * h, y) - 2 * invoke(x, y) + invoke(x - 2
                * h, y))
                / hh4;
        result[0][1] = result[1][0] = (invoke(x + h, y + h)
                - invoke(x + h, y - h) - invoke(x - h, y + h) + invoke(x - h, y
                - h))
                / hh4;
        result[1][1] = (invoke(x, y + h) - 2 * invoke(x, y) + invoke(x, y - h))
                / hh4;
        return result;
    }
}