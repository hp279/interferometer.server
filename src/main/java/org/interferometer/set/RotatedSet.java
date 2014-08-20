package org.interferometer.set;

public class RotatedSet extends FlatSet
{
	FlatSet A;
	double angle;
	
	public RotatedSet(FlatSet A, double angle)
	{
		this.A = A;
		this.angle = angle;
	}
	
	public boolean has(double x, double y)
	{
		return A.has(x*Math.cos(angle) + y*Math.sin(angle), -x*Math.sin(angle) + y*Math.cos(angle));
	}
}