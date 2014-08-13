package org.interferometer;

import java.math.BigDecimal;

import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.OptimisationSolver;
import org.ojalgo.optimisation.OptimisationSolver.Result;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.quadratic.QuadraticExpressionsModel;

// функция, восстановленная из разности
public class RestoredTableFunction extends TableFunction {
    AbstractFunction cont_modul; // оценка сверху модуля непрерывности

    public RestoredTableFunction(double min, double max, int n,
            AbstractFunction cont_modul) {
        super(min, max, n);
        this.cont_modul = cont_modul;
    }

    // восстановление функции по сдвигу с заданным шагом и величиной сдвига
    public void restoreByShift(AbstractFunction deltaf, double s) {
        // пока будем считать для простоты, что s/delta - целое число,
        // на которое делится
        // y.length!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        int m = (int) (s / delta + 1e-10); // количество переменных
        int n = y.length; // общее количество значений
        int K = n / m; // количество разностей, укладывающихся в области
                       // определения
        System.out.printf("s=%f, delta=%f, n=%d m=%d K=%d", s, delta, n, m, K);
        // создаём массив разностей:
        double diffs[] = new double[n];
        for (int i = 0; i < m; ++i)
            diffs[i] = 0;
        for (int i = m; i < n; ++i) {
            double xi = this.getArgument(i)
                    + (i == 0 ? 1e-10 : (i == n - 1 ? -1e-10 : 0)); // чтобы всё
                                                                    // попало в
                                                                    // интервалы
            diffs[i] = diffs[i - m] + deltaf.invoke(xi);
        }
        Variable vars[] = new Variable[m];
        for (int i = 0; i < m; ++i)
            // имена x1, x2, ... xm
            vars[i] = Variable.make(String.format("x%d", i + 1));
        ExpressionsBasedModel<?> model = new QuadraticExpressionsModel(vars);
        // ограничения на разности значений берём по минимуму:
        double mindiffstop[][] = new double[m][m], mindiffsbottom[][] = new double[m][m];
        for (int j1 = 0; j1 < m; ++j1)
            for (int j2 = 0; j2 < m; ++j2) {
                if (j1 == j2) // сравнивать переменную саму с собой нет смысла
                    continue;
                mindiffstop[j1][j2] = mindiffsbottom[j1][j2] = 1e300; // как
                                                                      // по-человечески
                                                                      // сделать
                                                                      // бесконечность??????????????????
                for (int k1 = 0; k1 < K; ++k1)
                    for (int k2 = 0; k2 < K; ++k2) {
                        int i1 = k1 * m + j1, i2 = k2 * m + j2;
                        if (i1 > i2) {
                            double maxdiff = cont_modul.invoke(this
                                    .getArgument(i1) - this.getArgument(i2));
                            double xdiff = diffs[i1] - diffs[i2];
                            mindiffstop[j1][j2] = Math.min(mindiffstop[j1][j2],
                                    maxdiff + xdiff);
                            mindiffsbottom[j1][j2] = Math.min(
                                    mindiffsbottom[j1][j2], -maxdiff + xdiff);
                        } else {
                            double maxdiff = cont_modul.invoke(this
                                    .getArgument(i2) - this.getArgument(i1));
                            double xdiff = diffs[i2] - diffs[i1];
                            mindiffstop[j1][j2] = Math.min(mindiffstop[j1][j2],
                                    maxdiff - xdiff);
                            mindiffsbottom[j1][j2] = Math.min(
                                    mindiffsbottom[j1][j2], -maxdiff - xdiff);
                        }
                    }
                Expression ex = model.addEmptyLinearExpression(String.format(
                        "(%d, %d)", j1, j2));
                for (int j = 0; j < m; ++j) {
                    double aij = (j == j2 ? 1 : (j == j1 ? -1 : 0));
                    ex.setLinearFactor(j, new BigDecimal(aij));
                }
                // ограничения сверху и снизу:
                ex.setLowerLimit(new BigDecimal(mindiffsbottom[j1][j2]));
                ex.setUpperLimit(new BigDecimal(mindiffstop[j1][j2]));
            }
        Expression objex = model.addEmptyCompoundExpression("y");
        objex.setContributionWeight(new BigDecimal(1)); // 1 - минимизация
        // Добавляем квадратичную часть целевой функции.
        for (int i = 0; i < m; ++i)
            for (int j = 0; j < m; ++j) {
                double qij = (i == j ? K : 0);
                objex.setQuadraticFactor(i, j, new BigDecimal(qij));
            }
        // Добавляем линейную часть целевой функции:
        for (int j = 0; j < m; ++j) {
            double cj = 0;
            for (int i = 0; i < K; ++i)
                cj += 2 * diffs[i * m + j];
            objex.setLinearFactor(j, new BigDecimal(cj));
        }
        // Решаем задачу:
        OptimisationSolver solver = model.getDefaultSolver();
        System.out.printf("Matrices: \n%s", solver.toString());
        Result result = solver.solve();
        if (result.getState().isLessThan(org.ojalgo.optimisation.State.OPTIMAL))
            this.setError();
        else {
            BasicMatrix vector = result.getSolution();
            for (int i = 0; i < n; ++i)
                this.setValue(i, vector.toBigDecimal(i % m, 0).doubleValue()
                        + diffs[i]);
            this.state = State.Defined;
        }
    }

    // восстановление функции по двум сдвигам с заданным шагом и величинами
    // сдвигов
    public void restoreByShift(AbstractFunction delta1f, double s1,
            AbstractFunction delta2f, double s2) {
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }

}
