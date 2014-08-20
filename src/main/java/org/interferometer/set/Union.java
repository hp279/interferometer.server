package org.interferometer.set;

public class Union extends FlatSet
{
	FlatSet A, B;
	boolean disjoint;
	
	public Union(FlatSet A, FlatSet B, boolean disjoint)
	{
		this.A = A;
		this.B = B;
		this.disjoint = disjoint;
	}
	public Union(FlatSet A, FlatSet B)
	{
		this(A, B, false);
	}
	
	public boolean has(double x, double y)
	{
		return A.has(x, y) || B.has(x, y);
	}
	
	public FlatSet getSet1()
	{
		return A;
	}
	public FlatSet getSet2()
	{
		return B;
	}
	
	/** объединение непересекающихся множеств */
	public boolean isDisjoint()
	{
		return disjoint;
	}
}