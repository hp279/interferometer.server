package org.interferometer.function;

import java.util.Random;

import org.ojalgo.function.UnaryFunction;

public class RandomNoise2 extends AbstractFunction2
{	
	double q; // среднеквадратичное отклонение
	
	public RandomNoise2(double q)
	{
		this.q = q;
	}
	  public double invoke(double x, double y)
	  {
		  return new Random().nextGaussian() * q;
	  }
}