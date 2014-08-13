package org.interferometer;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.TreeSet;

import org.interferometer.function.AbstractFunction2;
import org.interferometer.function.TableFunction2;
import org.interferometer.linear.Matrix22;

public class StripsInfo {
    int m, n;
    int k[][];
    TableFunction2 function;
    LinkedList<Strip> strips;
    TreeSet<Border> borders;

    public static class StripsOptions {
        double h1, h2;
        double e0, e1, e2;
        boolean eval_diff2; // проверять ли матрицу 2-х производных
        boolean eval_diff1; // проверять ли градиент

        public StripsOptions(double h1, double h2, double e0, double e1,
                double e2, boolean eval_diff1, boolean eval_diff2) {
            this.h1 = h1;
            this.h2 = h2;
            this.e0 = e0;
            this.e1 = e1;
            this.e2 = e2;
            this.eval_diff1 = eval_diff1;
            this.eval_diff2 = eval_diff2;
        }

        public StripsOptions(double h1, double h2, double e0, double e1,
                double e2) {
            this(h1, h2, e0, e1, e2, true, false);
        }

        public boolean evalFunction(double val, double what) {
            return val >= what - e0 && val <= what + e0;
        }

        public boolean evalDiff(TableFunction2 function, double x, double y,
                double[] what) {
            // System.out.printf("\n(x, y, h1) = (%.15f, %.15f, %.15f)", x, y,
            // h1);
            if (!eval_diff1)
                return true;
            if (function.hasSquare(x, y, h1)) {
                // System.out.printf(" good (x, y, h1) in [%f; %f]*[%f; %f]",
                // function.getMinX(), function.getMaxX(), function.getMinY(),
                // function.getMaxY());
                double diff1[] = function.diff(x, y, h1);
                return diff1[0] >= what[0] - e1 && diff1[0] <= what[0] + e1
                        && diff1[1] >= what[1] - e1 && diff1[1] <= what[1] + e1;
            }
            return false;
        }

        public boolean evalDiff2(AbstractFunction2 function, double x, double y) {
            if (!eval_diff2)
                return true;
            if (function.hasSquare(x, y, h2 * 2)) {
                Matrix22 diff2 = function.diff2(x, y, h2);
                return diff2.isDetEqual(0, e2) && diff2.isTraceEqual(1, e2);
            }
            return false;
        }

    }

    StripsOptions options;

    public enum Status {
        NotRestored, BordersRestored, StripsRestored, Error;
    }

    private Status status;

    private enum PixelType {
        Nothing, // не идентифицирована
        MayBeMax, // возможная точка максимума
        PossibleMax, // точка максимума, причисленная к какой-то линии
        PossibleMaxAndBegin, // точка максимума, являющаяся началом или концом
                             // какой-то линии
        IThinkMax, // окончательно идентифицирована как точка максимума и часть
                   // линии
        MayBeMin, // возможная точка минимума
        PossibleMin, // точка минимума, причисленная к какой-то линии
        PossibleMinAndBegin, // точка минимума, являющаяся началом или концом
                             // какой-то линии
        IThinkMin // окончательно идентифицирована как точка минимума и часть
                  // линии
    }

    PixelType[][] evaluations;

    private void clearEvals() {
        this.k = new int[m][n];
        for (int i = 0; i < m; ++i)
            for (int j = 0; j < n; ++j)
                k[i][j] = 0;
        this.evaluations = new PixelType[m][n];
        for (int i = 0; i < m; ++i)
            for (int j = 0; j < n; ++j)
                evaluations[i][j] = PixelType.Nothing;
        this.strips = new LinkedList<Strip>();
        this.borders = new TreeSet<Border>();
        this.status = Status.NotRestored;
    }

    public StripsInfo(TableFunction2 function, StripsOptions options) {
        this.function = function;
        this.m = function.getSizeX() + 1;
        this.n = function.getSizeY() + 1;
        this.options = options;
        clearEvals();
    }

    public Status getStatus() {
        return status;
    }

    public int getK(int i, int j) {
        return k[i][j];
    }

    public void write(PrintStream out) {
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j)
                if (function.hasArgument(function.getArgument1(i),
                        function.getArgument2(j)))
                    switch (evaluations[i][j]) {
                    case Nothing:
                        out.printf("0 ");
                        break;
                    case MayBeMax:
                        out.printf("+1 ");
                        break;
                    case PossibleMax:
                        out.printf("+2 ");
                        break;
                    case PossibleMaxAndBegin:
                        out.printf("+2.1 ");
                        break;
                    case IThinkMax:
                        out.printf("3 ");
                        break;
                    case MayBeMin:
                        out.printf("-1 ");
                        break;
                    case PossibleMin:
                        out.printf("-2 ");
                        break;
                    case PossibleMinAndBegin:
                        out.printf("-2.1 ");
                        break;
                    case IThinkMin:
                        out.printf("-3 ");
                        break;
                    }
            out.print('\n');
        }
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j)
                if (function.hasArgument(function.getArgument1(i),
                        function.getArgument2(j)))
                    out.printf("%d ", k[i][j]);
            out.print('\n');
        }
    }

    // предварительная оценка, где могут быть максимумы
    private void evaluateMaxBorders() {
        double zero[] = new double[2];
        zero[0] = zero[1] = 0;
        for (int i = 0; i < m; ++i)
            for (int j = 0; j < n; ++j) {
                double x = function.getArgument1(i), y = function
                        .getArgument2(j);
                if (function.hasArgument(x, y)) {
                    double val = function.getValue(i, j);
                    if (options.evalFunction(val, 1)) {
                        if (options.evalDiff(function, x, y, zero)) {
                            if (options.evalDiff2(function, x, y))
                                this.evaluations[i][j] = PixelType.MayBeMax;
                        }
                    }
                }
            }
    }

    // определяем максимумы функций там, где это возможно:
    private void createMaxBorders() {
        evaluateMaxBorders();
        // TODO: а теперь создаём связные линии
    }

    private void evaluateMinBorders() {
        double zero[] = new double[2];
        zero[0] = zero[1] = 0;
        for (int i = 0; i < m; ++i)
            for (int j = 0; j < n; ++j) {
                double x = function.getArgument1(i), y = function
                        .getArgument2(j);
                if (function.hasArgument(x, y)) {
                    double val = function.getValue(i, j);
                    if (options.evalFunction(val, -1)) {
                        if (options.evalDiff(function, x, y, zero)) {
                            if (options.evalDiff2(function, x, y))
                                this.evaluations[i][j] = PixelType.MayBeMin;
                        }
                    }
                }
            }
    }

    // определяем минимумы функций там, где это возможно:
    private void createMinBorders() {
        evaluateMinBorders();
        // TODO: а теперь создаём связные линии
    }

    // соединяем полосы:
    private void linkBorders() {
        // TODO: сделать паросочетание в графе
    }

    // заполняем массив k:
    private void restoreK() {
        // TODO: сделать восстановление по полосам
    }

    public void createStrips() {
        createMaxBorders();
        createMinBorders();
        this.status = Status.BordersRestored; // TODO: проверить, что всё
                                              // хорошо, иначе Error
        if (status == Status.BordersRestored) {
            linkBorders();
            restoreK();
            this.status = Status.StripsRestored; // TODO: проверить, что всё
                                                 // хорошо, иначе Error
        }
    }
}
