package org.interferometer;

import java.io.PrintStream;

import org.interferometer.function.AbstractFunction2;
import org.interferometer.function.TableFunction2;

// F(x) = cos(pi(delta f(x) + ax + b)/lambda)
public class InterferometerRestoreFunction extends TableFunction2 {
    double a, b, lambda;
    double deltaz;

    StripsInfo currentStripsInfo;

    public enum Type {
        ByX, ByY
    }

    private Type type;

    public InterferometerRestoreFunction(Type type, double minx, double maxx,
            double miny, double maxy, int m, int n, double a, double b,
            double lambda, double deltaz) {
        super(minx, maxx, miny, maxy, m, n);
        this.type = type;
        this.a = a;
        this.b = b;
        this.lambda = lambda;
        this.deltaz = deltaz;
        this.currentStripsInfo = null;
    }

    public double getPeriod() {
        return 2 * lambda / a;
    }

    // загрузка из картинки
    public void load(String filename) {
        // TODO: сделать загрузку из файла
    }

    public void write(PrintStream out) {
        super.write(out);
        if (currentStripsInfo != null) {
            out.printf("\nS\n");
            currentStripsInfo.write(out);
        }
    }

    public void setStripsInfo(StripsInfo.StripsOptions options) {
        this.currentStripsInfo = new StripsInfo(this, options);
        // TODO: сделать поддержку нескольких вариантов располосовки
    }

    public void createStrips() {
        double T = getPeriod();
        double h1 = T / 2, // постепенно будем уменьшать до max(deltax, deltay,
                           // 4*deltaz)
        h2 = T / 2; // постепенно будем уменьшать до max(deltax, deltay,
                    // Math.sqrt(8*deltaz))
        double e0 = deltaz, // постепенно будем увеличивать до 0.5
        e1 = Math.max(deltaz, 2 * deltaz / h1), // постепенно будем увеличивать
                                                // до 0.5
        e2 = Math.max(deltaz, 4 * deltaz / (h2 * h2)); // постепенно будем
                                                       // увеличивать до 0.5
        // TODO: сделать цикл, пока не получим приличные полоски
        h1 = Math.max(Math.max(this.getStepX(), this.getStepY()), 4 * deltaz);
        h2 = Math.max(Math.max(this.getStepX(), this.getStepY()),
                Math.sqrt(8 * deltaz));
        e0 = 0.5;
        e1 = 0.5;
        e2 = 0.5;

        StripsInfo.StripsOptions options = new StripsInfo.StripsOptions(h1, h2,
                e0, e1, e2);
        this.setStripsInfo(options);
        this.currentStripsInfo.createStrips();
    }

    private double getOriginal(int i, int j) {
        return Utils.acos2(getValue(i, j), currentStripsInfo.getK(i, j))
                * lambda / Math.PI;
    }

    public TableFunction2 restore() {
        if (currentStripsInfo == null)
            createStrips();
        if (currentStripsInfo.getStatus() != StripsInfo.Status.StripsRestored)
            currentStripsInfo.createStrips();
        InterferometerRestoreFunction temp = new InterferometerRestoreFunction(
                this.type, this.getMinX(), this.getMaxX(), this.getMinY(),
                this.getMaxY(), this.getSizeX(), this.getSizeY(), this.a,
                this.b, this.lambda, this.deltaz);
        for (int i = 0; i < getSizeX() + 1; ++i)
            for (int j = 0; j < getSizeY() + 1; ++j)
                temp.setValue(i, j, this.getOriginal(i, j));
        TableFunction2 result = new TableFunction2(this.getMinX(),
                this.getMaxX(), this.getMinY(), this.getMaxY(),
                this.getSizeX(), this.getSizeY());
        result.setArea(this.getArea());
        result.assign(temp);
        result.append(new AbstractFunction2() {
            public double invoke(double x, double y) {
                return type == Type.ByX ? (-a * x - b) : (-a * y - b);
            }
        });
        return result;
    }

}