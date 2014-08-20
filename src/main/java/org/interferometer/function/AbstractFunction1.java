package org.interferometer.function;

import org.ojalgo.function.UnaryFunction;

public abstract class AbstractFunction1 extends AbstractFunction implements UnaryFunction<Double>
{	
	// аргумент принадлежит области определения
	public boolean hasArgument(double x)
	{
		return true;
	}
	
	// отрезок с центром в точке x размера 2h принадлежит области определения
	public boolean hasSegment(double x, double h)
	{
		return hasArgument(x-h)	&&
			   hasArgument(x+h);
	}
	
	  public Double invoke(Double x)
	  {
		  return this.invoke(x.doubleValue());
	  }
	
	  public double diff(double x, double h)
	  {
		  return (invoke(x+h) - invoke(x-h))/(2*h);
	  }
  
	  public double diff2(double x, double h)
	  {
		  return (invoke(x+h) - 2*invoke(x) + invoke(x-h))/(h*h);
	  }
}