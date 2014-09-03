package org.interferometer.linear;

import java.math.BigDecimal;

import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.quadratic.QuadraticExpressionsModel;

/**
 * Задача квадратичного программирования, в которой неизвестные образуют не
 * вектор, а прямоугольную матрицу
 */
public class RectangleQuadraticTask {
    int mx, my;
    double q[][]; // квадратичная часть целевой функции
    double c[]; // линейная часть целевой функции

    public RectangleQuadraticTask(int mx, int my) {
        this.mx = mx;
        this.my = my;
        this.q = new double[mx * my][mx * my];
        this.c = new double[mx * my];
    }

    /** Создаёт массив переменных: */
    public Variable[] createVars() {
        Variable vars[] = new Variable[mx * my];
        for (int ix = 0; ix < mx; ++ix)
            for (int iy = 0; iy < my; ++iy)
                // имена x1, x2, ... xm
                vars[ix * my + iy] = Variable.make(String.format("x_%d,%d",
                        ix + 1, iy + 1));
        return vars;
    }

    public ExpressionsBasedModel<?> createModel() {
        return new QuadraticExpressionsModel(createVars());
    }

    public void makeZero() {
        int m = mx * my;
        for (int i = 0; i < m; ++i)
            for (int j = 0; j < m; ++j)
                q[i][j] = 0;
        for (int j = 0; j < m; ++j)
            c[j] = 0;
    }

    public void addQuadratic(int x1, int y1, int x2, int y2, double value) {
        q[x1 * my + y1][x2 * my + y2] += value;
        if (x1 != y1 || x2 != y2) // не на диагонали
            q[x2 * my + y2][x1 * my + y1] += value;
    }

    public void addLinear(int x, int y, double value) {
        c[x * my + y] += value;
    }

    public void initQuadraticFunction(Expression objex) {
        int m = mx * my;
        for (int i = 0; i < m; ++i)
            for (int j = 0; j < m; ++j)
                objex.setQuadraticFactor(i, j, new BigDecimal(q[i][j]));
        for (int j = 0; j < m; ++j)
            objex.setLinearFactor(j, new BigDecimal(c[j]));
    }

    // TODO: сделать здесь же добавление ограничений и решение

}