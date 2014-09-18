package org.interferometer.set.linked;

import org.interferometer.linear.Vector2;
import org.interferometer.set.FlatSet;

public interface SetPathFinder<S extends FlatSet>
{
    public Vector2 getPoint(S set);
    public void bypass(S set, Vector2 begin);
    // TODO: добавить соединение 2 точек
}