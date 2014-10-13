package org.interferometer.math;

public class Utils {
    /** квадрат */
    static public double sqr(double x) {
        return x * x;
    }

    /** Арккосинус с учётом полосы */
    static public double acos2(double x, int k) {
        double result = (x >= 1 ? 0 : (x <= -1 ? Math.PI : Math.acos(x)));
        return k >= 0 ? (result + 2 * Math.PI * k)
                : (-result - 2 * Math.PI * k);
    }
}