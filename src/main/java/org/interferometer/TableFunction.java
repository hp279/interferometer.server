package org.interferometer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

// функция задаётся таблицей значений, в промежутках - линейная интерполяция
// размер таблицы - не меньше 2
public class TableFunction extends AbstractFunction {
    double a, b;
    double delta;
    double y[];

    public TableFunction(double a, double b, double y[]) {
        this.a = a;
        this.b = b;
        this.delta = (b - a) / (y.length - 1);
        this.y = y;
        this.state = State.Defined;
    }

    public TableFunction(double a, double b, int n) {
        this(a, b, new double[n + 1]);
        this.state = State.NotDefined;
    }

    public final double min() {
        return a;
    }

    public final double max() {
        return b;
    }

    public final double size() {
        return y.length - 1;
    }

    public final double step() {
        return delta;
    }

    public double getArgument(int i) {
        return a + delta * i;
    }

    public double getValue(int i) {
        return y[i];
    }

    protected void setValue(int i, double f) {
        y[i] = f;
    }

    protected void setValues(double y[]) {
        for (int i = 0; i < this.y.length; ++i)
            this.y[i] = y[i];
        state = State.Defined;
    }

    public void assign(AbstractFunction f) // записываем значение другой функции
                                           // f
    {
        for (int i = 0; i < y.length; ++i)
            setValue(i, f.invoke(this.getArgument(i)));
        state = State.Defined;
    }

    public void read(DataInputStream in) {
        read(new Scanner(in));
    }

    public void read(Scanner s) {
        // s.useDelimiter(" ");
        for (int i = 0; i < y.length; ++i) {
            setValue(i, s.nextDouble()); // а если ошибка при
                                         // чтении?????????????????
        }
        state = State.Defined;
    }

    public void write(DataOutputStream out) {
        write(new PrintStream(out));
    }

    public void write(PrintStream out) {
        if (state != State.Defined)
            return;
        for (int i = 0; i < y.length; ++i) {
            out.printf("%f\n", y[i]);
        }
    }

    // линейная интерполяция (может быть, лучше
    // квадратичная????????????????????)
    public double invoke(double x) {
        double part = (x - a) / delta;
        int i = (int) Math.floor(part);
        return y[i] + (y[i + 1] - y[i]) * (part - i);
    }

}