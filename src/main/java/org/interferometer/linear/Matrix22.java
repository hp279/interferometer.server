package org.interferometer.linear;

import org.ojalgo.matrix.PrimitiveMatrix;

public class Matrix22
{
	PrimitiveMatrix matrix;
	
	public Matrix22()
	{
		matrix = (PrimitiveMatrix)PrimitiveMatrix.FACTORY.makeZero(2, 2);
	}
	
	public Matrix22(double a11, double a12, double a21, double a22)
	{
		this();
		matrix.add(0, 0, a11);
		matrix.add(0, 1, a12);
		matrix.add(1, 0, a21);
		matrix.add(1, 1, a22);
	}
	
	public double get11()
	{
		return matrix.get(0, 0);
	}
	public double get12()
	{
		return matrix.get(0, 1);
	}
	public double get21()
	{
		return matrix.get(1, 0);
	}
	public double get22()
	{
		return matrix.get(1, 1);
	}
	
	public double getDeterminant()
	{
		return matrix.getDeterminant().getReal();
	}
	
	public double getTrace()
	{
		return matrix.getTrace().getReal();
	}
	
	public double getNorm1()
	{
		return Math.abs(get11()) + Math.abs(get12()) + Math.abs(get21()) + Math.abs(get22());
	}
	
	public double getNorm2()
	{
		return Math.sqrt(get11() * get11() + get12() * get12() + get21() * get21() + get22() * get22());
	}
	
	// eps - погрешность в определении элементов матрицы
	public boolean isDetEqual(double value, double eps)
	{
		double deteps = 2 * getNorm1() * eps; // погрешность в определении детерминанта
		return Math.abs(getDeterminant() - value) <= deteps;
	}
	
	// eps - погрешность в определении значений матрицы
	public boolean isTraceEqual(double value, double eps)
	{
		double traceeps = 2 * eps; // погрешность в определении следа
		return Math.abs(getTrace() - value) <= traceeps;
	}
	
}