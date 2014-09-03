package org.interferometer;

import org.interferometer.function.AbstractFunction2;
import org.interferometer.function.TableFunction2;
import org.interferometer.linear.Matrix22;
import org.interferometer.linear.Vector2;

public class StripsOptions {
    public class EvaluateOptions {
        double h1, h2;
        double e0, e1, e2;

        public EvaluateOptions(double h1, double h2, double e0, double e1,
                double e2) {
            this.h1 = h1;
            this.h2 = h2;
            this.e0 = e0;
            this.e1 = e1;
            this.e2 = e2;
        }

        public boolean evalFunction(double val, double what) {
            return val > what - e0 && val < what + e0;
        }

        public boolean evalDiff(TableFunction2 function, double x, double y,
                double norm) {
            // System.out.printf("\n(x, y, h1) = (%.15f, %.15f, %.15f)", x, y,
            // h1);
            if (!eval_diff1)
                return true;
            if (function.hasSquare(x, y, h1)) {
                // System.out.printf(" good (x, y, h1) in [%f; %f]*[%f; %f]",
                // function.getMinX(), function.getMaxX(), function.getMinY(),
                // function.getMaxY());
                Vector2 diff1 = function.diff(x, y, h1);
                return diff1.isNorm2Equal(norm, e1);// diff1.sub(what).getNormInf()
                                                    // < e1;
            }
            return false;
        }

        public boolean evalDiff2(AbstractFunction2 function, double x,
                double y, double det, double trace) {
            if (!eval_diff2)
                return true;
            if (function.hasSquare(x, y, h2 * 2)) {
                Matrix22 diff2 = function.diff2(x, y, h2);
                return diff2.isDetEqual(det, e2)
                        && diff2.isTraceEqual(trace, e2);
            }
            return false;
        }

        public boolean isConstantCenter(AbstractFunction2 function, double x,
                double y) {
            if (!check_constant_center)
                return true;
            double value = function.invoke(x, y);
            boolean xmin_edge = false, xmax_edge = false, ymin_edge = false, ymax_edge = false;
            int xmin_dist = 1, xmax_dist = 1, ymin_dist = 1, ymax_dist = 1;
            do
                if (!function.hasArgument(x - h1 * xmin_dist, y)) {
                    xmin_edge = true;
                    break;
                }
            while (evalFunction(function.invoke(x - h1 * xmin_dist++, y), value));
            do
                if (!function.hasArgument(x + h1 * xmax_dist, y)) {
                    xmax_edge = true;
                    break;
                }
            while (evalFunction(function.invoke(x + h1 * xmax_dist++, y), value));
            if ((!xmin_edge && xmin_dist < xmax_dist)
                    || (!xmax_edge && xmin_dist > xmax_dist))
                return false;
            do
                if (!function.hasArgument(x, y - h1 * ymin_dist)) {
                    ymin_edge = true;
                    break;
                }
            while (evalFunction(function.invoke(x, y - h1 * ymin_dist++), value));
            do
                if (!function.hasArgument(x, y + h1 * ymax_dist)) {
                    ymax_edge = true;
                    break;
                }
            while (evalFunction(function.invoke(x, y + h1 * ymax_dist++), value));
            if ((!ymin_edge && ymin_dist < ymax_dist)
                    || (!ymax_edge && ymin_dist > ymax_dist))
                return false;
            return true;
        }
    }

    EvaluateOptions min_options, max_options, nil_options;
    boolean eval_diff1; // проверять ли градиент
    boolean eval_diff2; // проверять ли матрицу 2-х производных
    boolean check_constant_center; // проверять ли принадлежность к центру
                                   // полосы, где функция постоянна
    boolean create_nil_borders; // строить ли границы, где значения функции
                                // равны 0

    public StripsOptions(boolean eval_diff1, boolean eval_diff2,
            boolean check_constant_center, boolean create_nil_borders) {
        this.min_options = null;
        this.max_options = null;
        this.eval_diff1 = eval_diff1;
        this.eval_diff2 = eval_diff2;
        this.check_constant_center = check_constant_center;
        this.create_nil_borders = create_nil_borders;
    }

    public StripsOptions() {
        this(false, true, true, true);
    }

    public EvaluateOptions getMinOptions() {
        return min_options;
    }

    public EvaluateOptions getMaxOptions() {
        return max_options;
    }

    public EvaluateOptions getNilOptions() {
        return nil_options;
    }

    public void setMinOptions(double h1, double h2, double e0, double e1,
            double e2) {
        min_options = new EvaluateOptions(h1, h2, e0, e1, e2);
    }

    public void setMaxOptions(double h1, double h2, double e0, double e1,
            double e2) {
        max_options = new EvaluateOptions(h1, h2, e0, e1, e2);
    }

    public void setNilOptions(double h1, double h2, double e0, double e1,
            double e2) {
        nil_options = new EvaluateOptions(h1, h2, e0, e1, e2);
    }

    public boolean mustCreateNilBorders() {
        return this.create_nil_borders;
    }
}
