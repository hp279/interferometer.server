package org.interferometer.difference;

import org.interferometer.function.AbstractFunction1;

public class DifferenceInfo implements Comparable<DifferenceInfo>
{
	AbstractFunction1 deltaf;
	double s;
	
	public DifferenceInfo(AbstractFunction1 deltaf, double s)
	{
		this.deltaf = deltaf;
		this.s = s;
	}

	public AbstractFunction1 getDelta() {
		return deltaf;
	}
	
	public double getDelta(double x)
	{
		return getDelta().invoke(x);
	}


	public double getS() {
		return s;
	}
	
	public int compareTo(DifferenceInfo obj)
	{
		return (s < obj.s?
				-1:
				(s > obj.s? 
				 1:
				 0)
				);
	}
}

