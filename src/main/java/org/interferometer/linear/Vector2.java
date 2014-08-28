package org.interferometer.linear;

public class Vector2
{
	double a1, a2;
	
	public Vector2(double a1, double a2)
	{
		this.a1 = a1;
		this.a2 = a2;
	}
	public Vector2()
	{
		this(0, 0);
	}
	
	public double get1()
	{
		return a1;
	}
	public double get2()
	{
		return a2;
	}
	
	public Vector2 add(Vector2 b)
	{
		return new Vector2(a1 + b.a1, a2 + b.a2);
	}
	
	public Vector2 sub(Vector2 b)
	{
		return new Vector2(a1 - b.a1, a2 - b.a2);
	}
	
	public double getNorm1()
	{
		return Math.abs(get1()) + Math.abs(get2());
	}
	
	public double getSqrNorm2()
	{
		return get1() * get1() + get2() * get2();
	}
	
	public double getNorm2()
	{
		return Math.sqrt(getSqrNorm2());
	}
	
	public double getNormInf()
	{
		return Math.max(Math.abs(a1), Math.abs(a2));
	}
	
	// eps - погрешность в определении элементов вектора
	public boolean isNorm1Equal(double value, double eps)
	{
		double normeps = 2 * eps; // погрешность в определении детерминанта
		return Math.abs(getNorm1() - value) <= normeps;
	}	
	
	// eps - погрешность в определении элементов вектора
	public boolean isNorm2Equal(double value, double eps)
	{
		double normeps = 2 * getNorm1() * eps; // погрешность в определении детерминанта
		return Math.abs(getSqrNorm2() - value * value) <= normeps;
	}	
	
	// TODO: isNormInfEqual
	
	public double product(Vector2 a)
	{
		return this.a1 * a.a1 + this.a2 * a2;
	}
	
	public double angle(Vector2 b)
	{
		return Math.acos(this.product(b) / (this.getNorm2() * b.getNorm2()));
	}
	
}