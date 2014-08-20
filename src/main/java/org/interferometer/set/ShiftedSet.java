package org.interferometer.set;

public class ShiftedSet extends FlatSet
{
	FlatSet A;
	double dx, dy;
	
	public ShiftedSet(FlatSet A, double dx, double dy)
	{
		this.A = A;
		this.dx = dx;
		this.dy = dy;
	}
	
	public boolean has(double x, double y)
	{
		return A.has(x-dx, y-dy);
	}
}