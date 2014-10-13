package org.interferometer.set;

public class Rectangle extends FlatSet {
    double left, top, right, bottom;

    public Rectangle(double x1, double y1, double x2, double y2) {
        this.left = x1;
        this.top = y1;
        this.right = x2;
        this.bottom = y2;
    }

    public double getMinX() {
        return left;
    }

    public double getMinY() {
        return top;
    }

    public double getMaxX() {
        return right;
    }

    public double getMaxY() {
        return bottom;
    }

    public double getWidth() {
        return right - left;
    }

    public double getHeight() {
        return bottom - top;
    }

    public boolean has(double x, double y) {
        return (x >= left) && (x <= right) && (y >= top) && (y <= bottom);
    }

    public static Rectangle makeIntersection(Rectangle a, Rectangle b) {
        return new Rectangle(Math.max(a.left, b.left), Math.max(a.top, b.top),
                Math.min(a.right, b.right), Math.min(a.bottom, b.bottom));
    }

    public static FlatSet makeUnion(Rectangle a, Rectangle b) {
        return new Union(a, b);
    }

    /** прямоугольная оболочка объединения */
    public static Rectangle makeRectUnion(Rectangle a, Rectangle b) {
        return new Rectangle(Math.min(a.left, b.left), Math.min(a.top, b.top),
                Math.max(a.right, b.right), Math.max(a.bottom, b.bottom));
    }
}