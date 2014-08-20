package org.interferometer.set;

public class Round extends FlatSet
{
	double x, y, r;
	
	public Round(double x, double y, double r)
	{
		this.x = x;
		this.y = y;
		this.r = r;
	}
	
	public boolean has(double x, double y)
	{
		return (x-this.x)*(x-this.x) + (y-this.y)*(y-this.y) <= r * r;
	}
}