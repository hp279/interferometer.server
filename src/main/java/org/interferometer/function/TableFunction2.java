package org.interferometer.function;

import java.lang.Math;

import java.io.PrintStream;
import java.util.Scanner;

import org.interferometer.linear.TableAction;
import org.interferometer.set.FlatSet;
import org.interferometer.set.Intersection;
import org.interferometer.set.Rectangle;
import org.interferometer.util.IntPoint;
import org.ojalgo.access.Access2D;
import org.ojalgo.function.BinaryFunction;
import org.ojalgo.function.UnaryFunction;
import org.ojalgo.function.aggregator.AggregatorFunction;

/**
 * Функция задаётся таблицей значений, в промежутках - квадратичная
 * интерполяция. Размер таблицы - не меньше 2 на 2. Область определения может
 * иметь 4 вида: 1) null для пустой функции (m = n = 0) 2) произвольное
 * множество для пустой функции 3) Rectangle с установленным свойством "Table2"
 * для функции с определёнными размерами 4) Intersection(произвольное множество,
 * Rectangle со свойством "Table2") для функции с опред. размерами
 */
public class TableFunction2 extends AbstractFunction2 implements
        Access2D<Double> {
    double minx, maxx, miny, maxy;
    double deltax, deltay;
    int m, n; // количества элементов - количества интервалов между ними на 1
              // меньше!
    double z[][];

    private void init(double minx, double maxx, double miny, double maxy,
            double z[][]) {
        this.minx = minx;
        this.maxx = maxx;
        this.miny = miny;
        this.maxy = maxy;
        this.z = z;
        this.m = z.length;
        this.n = z[0].length;
        this.deltax = (maxx - minx) / (this.m - 1);
        this.deltay = (maxy - miny) / (this.n - 1);
    }

    public void setArea(FlatSet area) {
        if (isEmpty())
            this.area = area;
        else
            this.area = new Intersection(getBasicArea(), area);
    }

    // В данном случае m, n - количества интервалов между элементами
    protected void resize(double minx, double maxx, double miny, double maxy,
            int m, int n) {
        this.m = m + 1;
        this.n = n + 1;
        if (this.m > 0 && this.n > 0) {
            z = new double[this.m][this.n];
            init(minx, maxx, miny, maxy, z);
        }
        FlatSet basic_area = getBasicArea(), add_area = getAdditionArea();
        if (add_area != null)
            this.area = new Intersection(basic_area, add_area);
        else
            this.area = basic_area;
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
        super(getBasicArea(minx, maxx, miny, maxy));
        init(minx, maxx, miny, maxy, z);
    }

    public TableFunction2(double minx, double maxx, double miny, double maxy,
            int m, int n) {
        resize(minx, maxx, miny, maxy, m, n);
    }

    public TableFunction2() // потом можно инициализировать через resize
    {
        this.m = 0;
        this.n = 0;
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

    private static FlatSet getBasicArea(double minx, double maxx, double miny,
            double maxy) {
        FlatSet result = new Rectangle(minx, miny, Utils.prevValue(maxx),
                Utils.prevValue(maxy));
        result.addProperty("Table2");
        return result;
    }

    public FlatSet getBasicArea() {
        return getBasicArea(minx, maxx, miny, maxy);
    }

    public FlatSet getAdditionArea() {
        FlatSet result = getArea();
        if (result == null || result.hasProperty("Table2"))
            return null;
        if (result.getClass().getName().endsWith("Intersection")) {
            Intersection intarea = (Intersection) result;
            FlatSet A = intarea.getSet1(), B = intarea.getSet2();
            if (A.hasProperty("Table2"))
                result = B;
            if (B.hasProperty("Table2"))
                result = A;
        }
        return result;
    }

    public final boolean isEmpty() {
        return (this.m == 0);
    }

    public final int getSizeX() {
        return m - 1;
    }

    public final int getSizeY() {
        return n - 1;
    }

    @Override
    public int getColDim() {
        return getSizeX() + 1;
    }

    @Override
    public int getRowDim() {
        return getSizeY() + 1;
    }

    @Override
    public int size() {
        return getColDim() * getRowDim();
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

    public boolean hasArgumentInt(int i, int j) {
        return hasArgument(getArgument1(i), getArgument2(j));
    }
    
    public boolean hasArgumentInt(IntPoint pt) {
        return hasArgumentInt(pt.getX(), pt.getY());
    }

    public double getDistance(int i1, int j1, int i2, int j2) {
        double x1 = getArgument1(i1), y1 = getArgument2(j1), x2 = getArgument1(i2), y2 = getArgument2(j2);
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    public double getValue(int i, int j) {
        return z[i][j];
    }
    
    public double getValue(IntPoint pt) {
        return getValue(pt.getX(), pt.getY());
    }

    @Override
    public double doubleValue(int aRow, int aCol) {
        return getValue(aRow, aCol);
    }

    @Override
    public Double get(int aRow, int aCol) {
        return new Double(getValue(aRow, aCol));
    }

    protected void setValue(int i, int j, double f) {
        z[i][j] = f;
    }

    public void iterate(TableAction<TableFunction2> action) {
        for (int i = 0; i < z.length; ++i)
            for (int j = 0; j < z[i].length; ++j) {
                if (hasArgumentInt(i, j))
                    action.act(this, i, j);
            }
    }

    public void assign(final BinaryFunction<Double> f) // записываем значение
                                                       // другой функции f
    {
        iterate(new TableAction<TableFunction2>() {
            @Override
            public void act(TableFunction2 a, int row, int col) {
                setValue(row, col,
                        f.invoke(a.getArgument1(row), a.getArgument2(col)));
            }
        });
        setDefined();
    }

    public void append(final BinaryFunction<Double> f) // прибавляем значение
                                                       // другой функции f
    {
        iterate(new TableAction<TableFunction2>() {
            @Override
            public void act(TableFunction2 a, int row, int col) {
                setValue(
                        row,
                        col,
                        a.getValue(row, col)
                                + f.invoke(a.getArgument1(row),
                                        a.getArgument2(col)));
            }
        });
    }

    public void transform(final UnaryFunction<Double> f) // применяем ко всем
                                                         // значениям другую
                                                         // функцию f
    {
        iterate(new TableAction<TableFunction2>() {
            @Override
            public void act(TableFunction2 a, int row, int col) {
                setValue(row, col, f.invoke(a.getValue(row, col)));
            }
        });
    }

    public double aggregate(AggregatorFunction<Double> aggregator) {
        aggregator.reset();
        for (int i = 0; i < z.length; ++i)
            for (int j = 0; j < z[i].length; ++j)
                if (hasArgument(getArgument1(i), getArgument2(j)))
                    aggregator.invoke(this.getValue(i, j));
        return aggregator.doubleValue();
    }

    public void read(final Scanner s) {
        // s.useDelimiter(" ");
        iterate(new TableAction<TableFunction2>() {
            @Override
            public void act(TableFunction2 a, int row, int col) {
                setValue(row, col, s.nextDouble());
            }
        });
        setDefined();
    }

    public void write(PrintStream out) {
        for (int i = 0; i < getSizeX() + 1; ++i) {
            for (int j = 0; j < getSizeY() + 1; ++j)
                if (hasArgumentInt(i, j))
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