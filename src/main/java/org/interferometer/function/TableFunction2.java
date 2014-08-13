package org.interferometer.function;

import java.io.PrintStream;
import java.util.Scanner;

import org.interferometer.set.Rectangle;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;

// функция задаётся таблицей значений, в промежутках - линейная интерполяция
// размер таблицы - не меньше 2 на 2
public class TableFunction2 extends AbstractFunction2 {
    double minx, maxx, miny, maxy;
    double deltax, deltay;
    double z[][];

    private void init(double minx, double maxx, double miny, double maxy,
            double z[][]) {
        this.minx = minx;
        this.maxx = maxx;
        this.miny = miny;
        this.maxy = maxy;
        this.deltax = (maxx - minx) / (z.length - 1);
        this.deltay = (maxy - miny) / (z[0].length - 1);
        this.z = z;
    }

    protected void resize(double minx, double maxx, double miny, double maxy,
            int m, int n) {
        setArea(new Rectangle(minx, miny, Utils.prevValue(maxx),
                Utils.prevValue(maxy)));
        z = new double[m + 1][n + 1];
        init(minx, maxx, miny, maxy, z);
        setNotDefined();
    }

    protected void resize(TableFunction2 fun) // устанавливает те же параметры,
                                              // что и у fun
    {
        resize(fun.getMinX(), fun.getMaxX(), fun.getMinY(), fun.getMaxY(),
                fun.getSizeX(), fun.getSizeY());
    }

    public TableFunction2(double minx, double maxx, double miny, double maxy,
            double z[][]) {
        super(new Rectangle(minx, miny, Utils.prevValue(maxx),
                Utils.prevValue(maxy)));
        init(minx, maxx, miny, maxy, z);
    }

    public TableFunction2(double minx, double maxx, double miny, double maxy,
            int m, int n) {
        resize(minx, maxx, miny, maxy, m, n);
    }

    public TableFunction2() // потом можно инициализировать через resize
    {
        setError();
    }

    public final double getMinX() {
        return minx;
    }

    public final double getMaxX() {
        return maxx;
    }

    public final double getMinY() {
        return miny;
    }

    public final double getMaxY() {
        return maxy;
    }

    public final int getSizeX() {
        return z.length - 1;
    }

    public final int getSizeY() {
        return z[0].length - 1;
    }

    public final double getStepX() {
        return deltax;
    }

    public final double getStepY() {
        return deltay;
    }

    public double getArgument1(int i) {
        return i < getSizeX() ? (minx + deltax * i) : Utils
                .prevValue(getMaxX()); // чтобы всё попадало в интервалы;
    }

    public double getArgument2(int j) {
        return j < getSizeY() ? (miny + deltay * j) : Utils
                .prevValue(getMaxY()); // чтобы всё попадало в интервалы;
    }

    public double[] getArgument(int i, int j) {
        double result[] = new double[2];
        result[0] = getArgument1(i);
        result[1] = getArgument2(j);
        return result;
    }

    public double getDistance(int i1, int j1, int i2, int j2) {
        double x1 = getArgument1(i1), y1 = getArgument2(j1), x2 = getArgument1(i2), y2 = getArgument2(j2);
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    public double getValue(int i, int j) {
        return z[i][j];
    }

    protected void setValue(int i, int j, double f) {
        z[i][j] = f;
    }

    public void assign(BinaryFunction<Double> f) // записываем значение другой
                                                 // функции f
    {
        for (int i = 0; i < z.length; ++i)
            for (int j = 0; j < z[i].length; ++j) {
                if (hasArgument(getArgument1(i), getArgument2(j)))
                    setValue(i, j, f.invoke(this.getArgument1(i),
                            this.getArgument2(j)));
            }
        setDefined();
    }

    public void append(BinaryFunction<Double> f) // прибавляем значение другой
                                                 // функции f
    {
        for (int i = 0; i < z.length; ++i)
            for (int j = 0; j < z[i].length; ++j)
                if (hasArgument(getArgument1(i), getArgument2(j)))
                    setValue(
                            i,
                            j,
                            getValue(i, j)
                                    + f.invoke(this.getArgument1(i),
                                            this.getArgument2(j)));
    }

    public void transform(UnaryFunction<Double> f) // применяем ко всем
                                                   // значениям другую функцию f
    {
        for (int i = 0; i < z.length; ++i)
            for (int j = 0; j < z[i].length; ++j)
                if (hasArgument(getArgument1(i), getArgument2(j)))
                    setValue(i, j, f.invoke(this.getValue(i, j)));
    }

    public void read(Scanner s) {
        // s.useDelimiter(" ");
        for (int i = 0; i < getSizeX() + 1; ++i)
            for (int j = 0; j < getSizeY() + 1; ++j) {
                if (hasArgument(getArgument1(i), getArgument2(j)))
                    setValue(i, j, s.nextDouble());
            }
        setDefined();
    }

    public void write(PrintStream out) {
        for (int i = 0; i < getSizeX() + 1; ++i) {
            for (int j = 0; j < getSizeY() + 1; ++j)
                if (hasArgument(getArgument1(i), getArgument2(j)))
                    out.printf("%f ", getValue(i, j));
            out.print('\n');
        }
    }

    public double invoke(double x, double y) {
        double partx = (x - minx) / deltax, party = (y - miny) / deltay;
        int i = (int) Math.floor(partx), j = (int) Math.floor(party);
        // квадратичная интерполяция:
        return (z[i][j] * (i + 1 - partx) + z[i + 1][j] * (partx - i))
                * (j + 1 - party)
                + (z[i][j + 1] * (i + 1 - partx) + z[i + 1][j + 1]
                        * (partx - i)) * (party - j);
        // линейная интерполяция работает плохо
        // return ( z[i][j] * (i+1-partx + j+1-party) +
        // z[i+1][j] * (partx-i) +
        // z[i][j+1] * (party-j)) / 2;
    }

}