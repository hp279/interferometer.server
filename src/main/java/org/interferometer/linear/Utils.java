package org.interferometer.linear;

import java.math.BigDecimal;

import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.Variable;

public class Utils {
    // Создаёт массив переменных:
    static public Variable[] createVars(int mx, int my) {
        Variable vars[] = new Variable[mx * my];
        for (int ix = 0; ix < mx; ++ix)
            for (int iy = 0; iy < my; ++iy)
                // имена x1, x2, ... xm
                vars[ix * my + iy] = Variable.make(String.format("x_%d,%d",
                        ix + 1, iy + 1));
        return vars;
    }

    static public void makeZeroQuadraticFunction(double q[][], double c[]) {
        int m = c.length;
        for (int i = 0; i < m; ++i)
            for (int j = 0; j < m; ++j)
                q[i][j] = 0;
        for (int j = 0; j < m; ++j)
            c[j] = 0;
    }

    static public void initQuadraticFunction(Expression objex, double q[][],
            double c[]) {
        int m = c.length;
        for (int i = 0; i < m; ++i)
            for (int j = 0; j < m; ++j)
                objex.setQuadraticFactor(i, j, new BigDecimal(q[i][j]));
        for (int j = 0; j < m; ++j)
            objex.setLinearFactor(j, new BigDecimal(c[j]));
    }

}