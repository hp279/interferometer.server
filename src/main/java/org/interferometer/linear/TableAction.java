package org.interferometer.linear;

import org.ojalgo.access.Access2D;

public interface TableAction<Table extends Access2D<Double> >
{
	public void act(Table a, int row, int col);
}