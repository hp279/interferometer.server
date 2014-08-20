package org.interferometer;

import org.interferometer.difference.ShiftFunction;
import org.interferometer.function.AbstractFunction1;
import org.ojalgo.function.UnaryFunction;

// F(x) = cos(pi(delta f(x) + ax + b)/lambda)
public class InterferometerFunction extends AbstractFunction1
{
  ShiftFunction deltaf;
  double a, b, lambda;
  public InterferometerFunction(UnaryFunction<Double> f, double s, double a, double b, double lambda)
  {
	this.deltaf = new ShiftFunction(f, s);
	this.a = a;
	this.b = b;
	this.lambda = lambda;
  }
  
  public double invoke(double x)
  {
	return Math.cos(Math.PI * (deltaf.invoke(x) + a*x + b) / lambda);
  }
}