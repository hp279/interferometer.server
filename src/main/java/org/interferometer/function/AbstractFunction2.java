package org.interferometer.function;

import org.interferometer.linear.Matrix22;
import org.interferometer.set.FlatSet;
import org.ojalgo.function.BinaryFunction;

public abstract class AbstractFunction2 extends AbstractFunction implements
        BinaryFunction<Double> {
    FlatSet area;

    public AbstractFunction2(FlatSet area) {
        this.area = area;
    }

    public AbstractFunction2() {
        this.area = null;
    }

    public FlatSet getArea() {
        return area;
    }

    public void setArea(FlatSet area) {
        this.area = area;
    }

    // аргумент принадлежит области определения
    public boolean hasArgument(double x, double y) {
        return (area == null) || area.has(x, y);
    }

    // ромб с центром в точке (x,y) размера 2h принадлежит области определения
    public boolean hasSquare(double x, double y, double h) {
        return hasArgument(x, y - h) && hasArgument(x, y + h)
                && hasArgument(x - h, y) && hasArgument(x + h, y);
    }

    public Double invoke(Double x, Double y) {
        return this.invoke(x.doubleValue(), y.doubleValue());
    }

    // градиент - вектор из 2 аргументов
    // должно быть hasSquare(x, y, h)
    public double[] diff(double x, double y, double h) {
        double[] result = new double[2];
        result[0] = (invoke(x + h, y) - invoke(x - h, y)) / (2 * h);
        result[1] = (invoke(x, y + h) - invoke(x, y - h)) / (2 * h);
        return result;
    }

    // матрица 2-х производных - размер 2 на 2
    // должно быть hasSquare(x, y, 2*h)
    public Matrix22 diff2(double x, double y, double h) {
        double hh4 = 4 * h * h;
        double a11 = (invoke(x + 2 * h, y) - 2 * invoke(x, y) + invoke(x - 2
                * h, y))
                / hh4, a12 = (invoke(x + h, y + h) - invoke(x + h, y - h)
                - invoke(x - h, y + h) + invoke(x - h, y - h))
                / hh4, a22 = (invoke(x, y + 2 * h) - 2 * invoke(x, y) + invoke(
                x, y - 2 * h)) / hh4;
        // result.add( 0, 0, a11 );
        // result.add( 0, 1, a12 );
        // result.add( 1, 0, a12 );
        // result.add( 1, 1, a22 );
        return new Matrix22(a11, a12, a12, a22);
    }
}