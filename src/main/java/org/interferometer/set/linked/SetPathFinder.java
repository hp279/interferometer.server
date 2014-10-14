package org.interferometer.set.linked;

import org.interferometer.linear.Vector2;
import org.interferometer.set.FlatSet;

public interface SetPathFinder<S extends FlatSet> {
    /** любая точка множества, если оно непусто */
    public Vector2 getPoint(S set);

    /** обходит множество, выполняя некоторое действие */
    public void bypass(S set, Vector2 begin);
    // TODO: добавить соединение 2 точек
}