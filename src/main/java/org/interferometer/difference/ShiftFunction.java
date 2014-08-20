package org.interferometer.difference;

import org.interferometer.function.AbstractFunction1;
import org.ojalgo.function.UnaryFunction;

// delta f(x) = f(x) - f(x - s)
public class ShiftFunction extends AbstractFunction1
{
  UnaryFunction<Double> f;
  double s;
  
  public ShiftFunction(UnaryFunction<Double> f, double s)
  {
	this.f = f;
	this.s = s;
  }
  
  public double invoke(double x)
  {
	return f.invoke(x) - f.invoke(x-s);
  }
}