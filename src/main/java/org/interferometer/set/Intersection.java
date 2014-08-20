package org.interferometer.set;

public class Intersection extends FlatSet
{
	FlatSet A, B;
	
	public Intersection(FlatSet A, FlatSet B)
	{
		this.A = A;
		this.B = B;
	}
	
	public boolean has(double x, double y)
	{
		return A.has(x, y) && B.has(x, y);
	}
	
	public FlatSet getSet1()
	{
		return A;
	}
	public FlatSet getSet2()
	{
		return B;
	}
}