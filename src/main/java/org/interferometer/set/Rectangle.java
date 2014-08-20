package org.interferometer.set;

public class Rectangle extends FlatSet
{
	double left, top, right, bottom;
	
	public Rectangle(double left, double top, double right, double bottom)
	{
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}
	
	public boolean has(double x, double y)
	{
		return (x >= left) && (x <= right) && (y >= top) && (y <= bottom);
	}
}