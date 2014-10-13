package org.interferometer.set.grid;

import org.interferometer.linear.Vector2;
import org.interferometer.set.FlatSet;
import org.interferometer.set.Intersection;
import org.interferometer.set.Rectangle;
import org.interferometer.set.Union;

/** дискретное множество, определенное на решетке */
public class GridSet extends FlatSet {
    Rectangle rect;
    double xstep, ystep;
    FlatSet basic_set;

    public GridSet(Rectangle rect, double xstep, double ystep, FlatSet basic_set) {
        this.rect = rect;
        this.xstep = xstep;
        this.ystep = ystep;
        this.basic_set = basic_set;
    }

    private double getEpsilonX() {
        return 1e-15 * rect.getWidth();
    }

    private double getEpsilonY() {
        return 1e-15 * rect.getHeight();
    }

    public int getMaxXNumber() {
        return (int) Math.floor(rect.getWidth() / xstep);
    }

    public int getMaxYNumber() {
        return (int) Math.floor(rect.getHeight() / ystep);
    }

    public double getArgumentX(int i) {
        return rect.getMinX() + xstep * i;
    }

    public double getArgumentY(int j) {
        return rect.getMinY() + ystep * j;
    }

    public Vector2 getArgument(int i, int j) {
        return new Vector2(getArgumentX(i), getArgumentY(j));
    }

    public boolean hasInt(int x, int y) {
        return basic_set.has(getArgumentX(x), getArgumentY(y));
    }

    @Override
    public boolean has(double x, double y) {
        return Math.IEEEremainder(x - rect.getMinX() + getEpsilonX(), xstep) <= getEpsilonX()
                && Math.IEEEremainder(y - rect.getMinY() + getEpsilonY(), xstep) <= getEpsilonY()
                && basic_set.has(x, y);
    }

    /** пересечение - предполагаем, что решетки совпадают */
    public GridSet makeIntersection(GridSet a, GridSet b) {
        return new GridSet(Rectangle.makeIntersection(a.rect, b.rect), a.xstep,
                a.ystep, new Intersection(a.basic_set, b.basic_set));
    }

    /** объединение - предполагаем, что решетки совпадают */
    public GridSet makeUnion(GridSet a, GridSet b) {
        return new GridSet(Rectangle.makeRectUnion(a.rect, b.rect), a.xstep,
                a.ystep, new Union(a.basic_set, b.basic_set));
    }

}