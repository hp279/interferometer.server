package org.interferometer.function;

import java.util.Random;

public class RandomNoise1 extends AbstractFunction1
{	
	double q; // среднеквадратичное отклонение
	
	public RandomNoise1(double q)
	{
		this.q = q;
	}
	  public double invoke(double x)
	  {
		  return new Random().nextGaussian() * q;
	  }
}