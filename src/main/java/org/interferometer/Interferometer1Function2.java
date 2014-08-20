package org.interferometer;

import org.interferometer.difference.Shift1Function2;
import org.interferometer.function.AbstractFunction2;

// F(x) = cos(pi(delta f(x) + ax + b)/lambda)
public class Interferometer1Function2 extends AbstractFunction2
{
  Shift1Function2 deltaf;
  double a, b, lambda;
  public Interferometer1Function2(AbstractFunction2 f, double s, double a, double b, double lambda)
  {
	this.deltaf = new Shift1Function2(f, s);
	this.a = a;
	this.b = b;
	this.lambda = lambda;
	if(deltaf.getArea() != null)
		this.setArea(deltaf.getArea());
  }
  
  public double invoke(double x, double y)
  {
	return Math.cos(Math.PI * (deltaf.invoke(x, y) + a*y + b) / lambda);
  }
}