package org.interferometer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

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

    public TableFunction2(double minx, double maxx, double miny, double maxy,
            double z[][]) {
        init(minx, maxx, miny, maxy, z);
    }

    public TableFunction2(double minx, double maxx, double miny, double max,
            int m, int n) {
        z = new double[m][n];
        init(minx, maxx, miny, maxy, z);
    }

    public final double minX() {
        return minx;
    }

    public final double maxX() {
        return maxx;
    }

    public final double minY() {
        return miny;
    }

    public final double maxY() {
        return maxy;
    }

    public final double sizeX() {
        return z.length - 1;
    }

    public final double sizeY() {
        return z[0].length - 1;
    }

    public final double stepX() {
        return deltax;
    }

    public final double stepY() {
        return deltay;
    }

    protected void setValue(int i, int j, double f) {
        z[i][j] = f;
    }

    public void read(DataInputStream in) {
        read(new Scanner(in));
    }

    public void read(Scanner s) {
        // s.useDelimiter(" ");
        for (int i = 0; i < sizeX(); ++i)
            for (int j = 0; j < sizeY(); ++j) {
                setValue(i, j, s.nextDouble());
            }
    }

    public void write(DataOutputStream out) {
        write(new PrintStream(out));
    }

    public void write(PrintStream out) {
        for (int i = 0; i < sizeX(); ++i)
            for (int j = 0; j < sizeY(); ++j) {
                out.printf("%f\n", z[i][j]);
            }
    }

    // линейная интерполяция (может быть, лучше
    // квадратичная????????????????????)
    public double invoke(double x, double y) {
        double partx = (x - minx) / deltax, party = (y - miny) / deltay;
        int i = (int) Math.floor(partx), j = (int) Math.floor(party);
        return (z[i][j] * (partx - i + party - j) + z[i + 1][j]
                * (i + 1 - partx + party - j) + z[i][j + 1]
                * (partx - i + j + 1 - party) + z[i + 1][j + 1]
                * (i + 1 - partx + j + 1 - party)) / 2;
    }

}