package org.interferometer.difference;

import org.interferometer.function.AbstractFunction2;

public class Difference2Info implements Comparable<Difference2Info>
{
	AbstractFunction2 deltaf_x,
					  deltaf_y;
	double s;
	
	public Difference2Info(AbstractFunction2 deltaf_x, AbstractFunction2 deltaf_y, double s)
	{
		this.deltaf_x = deltaf_x;
		this.deltaf_y = deltaf_y;
		this.s = s;
	}

	public AbstractFunction2 getDeltaX() {
		return deltaf_x;
	}
	
	public double getDeltaX(double x, double y)
	{
		return getDeltaX().invoke(x, y);
	}
	
	public AbstractFunction2 getDeltaY() {
		return deltaf_y;
	}
	
	public double getDeltaY(double x, double y)
	{
		return getDeltaY().invoke(x, y);
	}

	public double getS() {
		return s;
	}
	
	public int compareTo(Difference2Info obj)
	{
		return (s < obj.s?
				-1:
				(s > obj.s? 
				 1:
				 0)
				);
	}
}