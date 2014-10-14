package org.interferometer.difference;

import java.math.BigDecimal;
import java.util.Arrays;

import org.interferometer.function.AbstractFunction1;
import org.interferometer.function.TableFunction2;
import org.interferometer.linear.RectangleQuadraticTask;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.OptimisationSolver;
import org.ojalgo.optimisation.OptimisationSolver.Result;

/**
 * функция, восстановленная из разности area должно удовлетворять одному из 3
 * свойств: 1) прямоугольно-выпуклое - hasProperty("RectConvex"), или
 * hasProperty("QuasiRectConvexV"), или hasProperty("QuasiRectConvexH") 2)
 * связное объединение прямоугольно-выпуклых - Union или BigUnion и каждая
 * компонента - прямоугольно-выпуклая 3) объединение непересекающихся компонент
 * типа 2 - Union или BigUnion со включенным isDisjoint
 * */
public class RestoredTableFunction2 extends TableFunction2 {
    AbstractFunction1 cont_modul; // оценка сверху модуля непрерывности
    double h; // характерный интервал, на котором имеет смысл оценивать модуль
              // непрерывности

    /** значения переменных x, y, от которых будут отсчитываться остальные */
    class BaseNumbers {
        int m_x, m_y;
        int base_x[][], base_y[][];

        private void clear() {
            for (int i = 0; i < m_x; ++i)
                for (int j = 0; j < m_y; ++j) {
                    base_x[i][j] = -1;
                    base_y[i][j] = -1; // не инициализировано
                }
        }

        // устанавливает базовые значения методом северо-западного угла
        private void setBaseNumbers() {
            clear();
            for (int i = 0; i < getSizeX() + 1; ++i)
                for (int j = 0; j < getSizeY() + 1; ++j) {
                    if (hasArgument(getArgument1(i), getArgument2(j)))
                        if (base_x[i % m_x][j % m_y] == -1) {
                            base_x[i % m_x][j % m_y] = i;
                            base_y[i % m_x][j % m_y] = j;
                        }
                }
        }

        public BaseNumbers(int m_x, int m_y) {
            this.m_x = m_x;
            this.m_y = m_y;
            this.base_x = new int[m_x][m_y];
            this.base_y = new int[m_x][m_y];
            setBaseNumbers();
        }

        public int getBaseX(int i, int j) {
            return base_x[i % m_x][j % m_y];
        }

        public int getBaseY(int i, int j) {
            return base_y[i % m_x][j % m_y];
        }

        public int getBaseVariable(int i, int j) {
            return ((i % m_x) * m_y + (j % m_y));
        }

        public int getVarsCount(int i, int j) {
            int result = 0;
            int nx = getSizeX() + 1, ny = getSizeY() + 1;
            for (int x = i; x < nx; x += m_x)
                for (int y = j; y < ny; y += m_y)
                    if (hasArgument(getArgument1(x), getArgument2(y)))
                        result++;
            return result;
        }

        public double getVarsSum(int i, int j, double vars[][]) {
            double result = 0;
            int nx = getSizeX() + 1, ny = getSizeY() + 1;
            for (int x = i; x < nx; x += m_x)
                for (int y = j; y < ny; y += m_y)
                    if (hasArgument(getArgument1(x), getArgument2(y)))
                        result += vars[x][y];
            return result;
        }

        // массив разностей, позволяющих восстановить функцию во всей области по
        // значениям в квадрате
        // TODO: сейчас возможно только вычисление по возрастанию x и y - то
        // есть только базовые северо-западные номера
        // сделать для любых базовых номеров
        public double[][] getDifferences(Difference2Info diff) {
            int nx = getSizeX() + 1, ny = getSizeY() + 1; // количество значений
            double diffs[][] = new double[nx][ny];
            for (int ix = 0; ix < m_x; ++ix)
                for (int iy = 0; iy < m_y; ++iy) {
                    int xbase = base_x[ix][iy], ybase = base_y[ix][iy];
                    for (int jx = ix; jx < nx; jx += m_x) {
                        double x = getArgument1(jx)
                                + (jx == 0 ? 1e-10
                                        : (jx == nx - 1 ? -1e-10 : 0)); // чтобы
                                                                        // всё
                                                                        // попало
                                                                        // в
                                                                        // интервалы
                        // движемся при данном x по y от ybase сперва в
                        // уменьшение, потом в увеличение
                        boolean back = true; // движемся назад, иначе - вперёд
                        for (int jy = ybase + m_y;;) {
                            if (back)
                                jy -= m_y;
                            else
                                jy += m_y;
                            if (jy < 0) {
                                jy = ybase + m_y;
                                back = false;
                            }
                            if (jy >= ny)
                                break;
                            // System.out.printf("x=%d, y=%d\n", jx, jy);
                            double y = getArgument2(jy)
                                    + (jy == 0 ? 1e-10 : (jy == ny - 1 ? -1e-10
                                            : 0)); // чтобы всё попало в
                                                   // интервалы
                            if (!hasArgument(x, y))
                                continue;
                            if (jx == xbase)
                                if (jy == ybase)
                                    diffs[jx][jy] = 0;
                                else
                                    diffs[jx][jy] = diffs[jx][jy - m_y]
                                            + diff.getDeltaY().invoke(x, y);
                            else if (jy == ybase)
                                diffs[jx][jy] = diffs[jx - m_x][jy]
                                        + diff.getDeltaX().invoke(x, y);
                            else {
                                if (hasArgument(x - diff.getS(), y)) {
                                    if (back)
                                        if (hasArgument(x, y + diff.getS()))
                                            // чтобы минимизировать погрешности,
                                            // берем среднее арифметическое
                                            diffs[jx][jy] = (diffs[jx - m_x][jy]
                                                    + diff.getDeltaX().invoke(
                                                            x, y)
                                                    + diffs[jx][jy + m_y] - diff
                                                    .getDeltaY().invoke(x,
                                                            y + diff.getS())) / 2;
                                        else
                                            diffs[jx][jy] = diffs[jx - m_x][jy]
                                                    + diff.getDeltaX().invoke(
                                                            x, y);
                                    else if (hasArgument(x, y - diff.getS()))
                                        // чтобы минимизировать погрешности,
                                        // берем среднее арифметическое
                                        diffs[jx][jy] = (diffs[jx - m_x][jy]
                                                + diff.getDeltaX().invoke(x, y)
                                                + diffs[jx][jy - m_y] + diff
                                                .getDeltaY().invoke(x, y)) / 2;
                                    else
                                        diffs[jx][jy] = diffs[jx - m_x][jy]
                                                + diff.getDeltaX().invoke(x, y);
                                } else if (back)
                                    diffs[jx][jy] = diffs[jx][jy + m_y]
                                            - diff.getDeltaY().invoke(x,
                                                    y + diff.getS());
                                else
                                    diffs[jx][jy] = diffs[jx][jy - m_y]
                                            + diff.getDeltaY().invoke(x, y);
                            }
                        }
                    }
                }
            return diffs;
        }

        void addDiffByXSquares(Difference2Info diff_info, double diffs[][],
                RectangleQuadraticTask task) {
            // TODO: что делать, если s не делится на число пикселей?
            int nx = getSizeX() + 1, ny = getSizeY() + 1; // количество значений
            int six = (int) (diff_info.getS() / getStepX());
            for (int ix = six; ix < nx; ix++)
                for (int iy = 0; iy < ny; iy++) {
                    double x = getArgument1(ix), y = getArgument2(iy);
                    if (hasArgument(x, y)
                            && hasArgument(x - diff_info.getS(), y)) {
                        int jx = ix % m_x, jy = iy % m_y;
                        int jsix = (ix - six) % m_x;
                        task.addQuadratic(jx, jy, jx, jy, 1);
                        task.addQuadratic(jsix, jy, jsix, jy, 1);
                        task.addQuadratic(jx, jy, jsix, jy, -1);
                        task.addLinear(jx, jy, 2 * diffs[ix][iy]);
                        task.addLinear(jx, jy, -2 * diff_info.getDeltaX(x, y));
                        task.addLinear(jx, jy, -2 * diffs[ix - six][iy]);
                        task.addLinear(jsix, jy, 2 * diffs[ix - six][iy]);
                        task.addLinear(jsix, jy, 2 * diff_info.getDeltaX(x, y));
                        task.addLinear(jsix, jy, -2 * diffs[ix][iy]);
                    }
                }
        }

        void addDiffByYSquares(Difference2Info diff_info, double diffs[][],
                RectangleQuadraticTask task) {
            // TODO: что делать, если s не делится на число пикселей?
            int nx = getSizeX() + 1, ny = getSizeY() + 1; // количество значений
            int siy = (int) (diff_info.getS() / getStepY());
            for (int ix = 0; ix < nx; ix++)
                for (int iy = siy; iy < ny; iy++) {
                    double x = getArgument1(ix), y = getArgument2(iy);
                    if (hasArgument(x, y)
                            && hasArgument(x, y - diff_info.getS())) {
                        int jx = ix % m_x, jy = iy % m_y;
                        int jsiy = (iy - siy) % m_y;
                        task.addQuadratic(jx, jy, jx, jy, 1);
                        task.addQuadratic(jx, jsiy, jx, jsiy, 1);
                        task.addQuadratic(jx, jy, jx, jsiy, -1);
                        task.addLinear(jx, jy, 2 * diffs[ix][iy]);
                        task.addLinear(jx, jy, -2 * diff_info.getDeltaY(x, y));
                        task.addLinear(jx, jy, -2 * diffs[ix][iy - siy]);
                        task.addLinear(jx, jsiy, 2 * diffs[ix][iy - siy]);
                        task.addLinear(jx, jsiy, 2 * diff_info.getDeltaY(x, y));
                        task.addLinear(jx, jsiy, -2 * diffs[ix][iy]);
                    }
                }
        }
    }

    public RestoredTableFunction2(double minx, double maxx, double miny,
            double maxy, int m, int n, AbstractFunction1 cont_modul, double h) {
        super(minx, maxx, miny, maxy, m, n);
        this.cont_modul = cont_modul;
        this.h = h;
    }

    // устанавливает ограничения, определяемые модулем непрерывности
    private void setRestrictions(ExpressionsBasedModel<?> model, double s,
            double[][] diffs) {
        int mx = (int) (s / getStepX() + 1e-10), my = (int) (s / getStepY() + 1e-10), m = mx
                * my; // количество переменных
        int nx = getSizeX() + 1, ny = getSizeY() + 1; // общее количество
                                                      // значений
        int dx = (int) (this.h / getStepX()), dy = (int) (this.h / getStepY());
        // ограничения на разности значений берём по минимуму:
        double mindiffs[][] = new double[m][m];
        for (int j1x = 0; j1x < mx; ++j1x)
            for (int j1y = 0; j1y < my; ++j1y)
                for (int j2x = Math.max(j1x - dx, 0); j2x < Math.min(j1x + dx,
                        mx); ++j2x)
                    for (int j2y = Math.max(j1y - dy, 0); j2y < Math.min(j1y
                            + dy, my); ++j2y) {
                        int j1 = j1x * my + j1y, j2 = j2x * my + j2y;
                        // сравнивать переменную саму с собой нет смысла
                        if (j1 == j2) {
                            // System.out.printf("(%d, %d)", j1x, j1y);
                            continue;
                        }
                        mindiffs[j1][j2] = Double.POSITIVE_INFINITY;
                        for (int i1x = j1x; i1x < nx; i1x += mx)
                            for (int i1y = j1y; i1y < ny; i1y += my)
                                for (int i2x = Math.max(i1x - dx, 0); i2x < Math
                                        .min(i1x + dx, nx); i2x += mx)
                                    for (int i2y = Math.max(i1y - dy, 0); i2y < Math
                                            .min(i1y + dy, ny); i2y += my) {
                                        double maxdiff = cont_modul
                                                .invoke(getDistance(i1x, i1y,
                                                        i2x, i2y));
                                        mindiffs[j1][j2] = Math
                                                .min(mindiffs[j1][j2],
                                                        maxdiff
                                                                - (diffs[i2x][i2y] - diffs[i1x][i1y]));
                                    }
                        Expression ex = model.addEmptyLinearExpression(String
                                .format("((%d, %d), (%d, %d))", j1x, j1y, j2x,
                                        j2y));
                        for (int j = 0; j < m; ++j) {
                            double aij = (j == j2 ? 1 : (j == j1 ? -1 : 0));
                            ex.setLinearFactor(j, new BigDecimal(aij));
                        }
                        // ограничения сверху и снизу:
                        ex.setUpperLimit(new BigDecimal(mindiffs[j1][j2]));
                    }
    }

    // Восстанавливает функцию на основе решения ЗКП
    private void restoreSolverResult(BaseNumbers numbers, Result result,
            double[][] diffs) {
        int nx = getSizeX() + 1, ny = getSizeY() + 1;
        BasicMatrix vector = result.getSolution();
        for (int ix = 0; ix < nx; ++ix)
            for (int iy = 0; iy < ny; ++iy)
                this.setValue(ix, iy,
                        vector.toBigDecimal(numbers.getBaseVariable(ix, iy), 0)
                                .doubleValue() + diffs[ix][iy]);
        this.setDefined();
    }

    // восстановление функции по сдвигу по с заданным шагом и величиной сдвига
    public void restoreByShift(Difference2Info diff) {
        double s = diff.getS();
        int mx = (int) (s / getStepX() + 1e-10), my = (int) (s / getStepY() + 1e-10), m = mx
                * my; // количество переменных
        // System.out.printf("s=%f, delta=%f, n=%d m=%d", s, delta, n, m);
        // создаём массив разностей:
        BaseNumbers numbers = new BaseNumbers(mx, my);
        double diffs[][] = numbers.getDifferences(diff);
        RectangleQuadraticTask task = new RectangleQuadraticTask(mx, my);
        ExpressionsBasedModel<?> model = task.createModel();
        this.setRestrictions(model, s, diffs);
        Expression objex = model.addEmptyCompoundExpression("y");
        objex.setContributionWeight(new BigDecimal(1)); // 1 - минимизация
        // Добавляем квадратичную часть целевой функции.
        for (int i = 0; i < m; ++i)
            for (int j = 0; j < m; ++j) {
                double qij;
                if (i == j)
                    qij = numbers.getVarsCount(i / my, i % my);
                else
                    qij = 0;
                objex.setQuadraticFactor(i, j, new BigDecimal(qij));
            }
        // Добавляем линейную часть целевой функции:
        for (int jx = 0; jx < mx; ++jx)
            for (int jy = 0; jy < my; ++jy) {
                double cj = 2 * numbers.getVarsSum(jx, jy, diffs);
                objex.setLinearFactor(jx * my + jy, new BigDecimal(cj));
            }
        // Решаем задачу:
        System.out.println("begin solving...");
        OptimisationSolver solver = model.getDefaultSolver();
        Result result = solver.solve();
        System.out.println("end solving!");
        if (result.getState().isLessThan(org.ojalgo.optimisation.State.OPTIMAL))
            this.setError();
        else
            this.restoreSolverResult(numbers, result, diffs);
    }

    // восстановление функции по нескольким сдвигам с заданным шагом и
    // величинами сдвигов
    public void restoreByShift(Difference2Info[] diffs) {
        if (diffs.length == 1) {
            this.restoreByShift(diffs[0]);
            return;
        }
        Arrays.sort(diffs);
        double s = diffs[0].getS();
        int mx = (int) (s / getStepX() + 1e-10), my = (int) (s / getStepY() + 1e-10); // количество
                                                                                      // переменных
        // System.out.printf("s=%f, delta=%f, n=%d m=%d", s, delta, n, m);
        // создаём массив разностей:
        BaseNumbers numbers = new BaseNumbers(mx, my);
        double diff_values[][] = numbers.getDifferences(diffs[0]);
        RectangleQuadraticTask task = new RectangleQuadraticTask(mx, my);
        ExpressionsBasedModel<?> model = task.createModel();
        this.setRestrictions(model, s, diff_values);
        Expression objex = model.addEmptyCompoundExpression("y");
        objex.setContributionWeight(new BigDecimal(1)); // 1 - минимизация
        task.makeZero();
        // Определяем линейную и квадратичную часть:
        for (int i = 1; i < diffs.length; ++i) {
            numbers.addDiffByXSquares(diffs[i], diff_values, task);
            numbers.addDiffByYSquares(diffs[i], diff_values, task);
        }

        // Инициализируем целевую функцию:
        task.initQuadraticFunction(objex);
        // Решаем задачу:
        System.out.println("begin solving...");
        OptimisationSolver solver = model.getDefaultSolver();
        // System.out.printf("Matrices: \n%s", solver.toString());
        Result result = solver.solve();
        System.out.println("end solving!");
        if (result.getState().isLessThan(org.ojalgo.optimisation.State.OPTIMAL))
            this.setError();
        else
            this.restoreSolverResult(numbers, result, diff_values);
    }
}