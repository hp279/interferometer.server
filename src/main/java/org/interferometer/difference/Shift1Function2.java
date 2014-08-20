package org.interferometer.difference;

import org.interferometer.function.AbstractFunction2;
import org.interferometer.set.Intersection;
import org.interferometer.set.ShiftedSet;
 
// delta f(x) = f(x) - f(x - s)
public class Shift1Function2 extends AbstractFunction2
{
  AbstractFunction2 f;
  double s;
  
  public Shift1Function2(AbstractFunction2 f, double s)
  {
	this.f = f;
	this.s = s;
	if(f.getArea() != null)
		this.setArea(new Intersection(f.getArea(), new ShiftedSet(f.getArea(), s, 0)));
  }
  
  public double invoke(double x, double y)
  {
	return f.invoke(x, y) - f.invoke(x-s, y);
  }
}