package org.interferometer.function;

import java.io.PrintStream;
import java.util.Scanner;

import org.ojalgo.function.UnaryFunction;

 
// функция задаётся таблицей значений, в промежутках - линейная интерполяция
// размер таблицы - не меньше 2
public class TableFunction extends AbstractFunction1
{
  double a, b;
  double delta;
  double y[];
    
  public TableFunction(double a, double b, double y[])
  {
	this.a = a;
	this.b = b;
	this.delta = (b-a) / (y.length-1);
	this.y = y;
  }
  
  public TableFunction(double a, double b, int n)
  {
	this(a, b, new double[n+1]);
	setNotDefined();
  }
  
  public final double min()
  {
	return a;
  }
  public final double max()
  {
	return b;
  }
  
  public boolean hasArgument(double x)
  {
	  return x >= min() &&
			 x <= max(); 
  }
  
  public final int size()
  {
	return y.length - 1;
  }
  public final double step()
  {
	return delta;
  }
  
  public double getArgument(int i)
  {
	  return (a + delta * i) * (i==0? (1+1e-15) : (i==y.length-1? (1-1e-15) : 1)); // чтобы всё попадало в интервалы
  }
  
  public double getValue(int i)
  {
	  return y[i];
  }
  
  protected void setValue(int i, double f)
  {
	y[i] = f;
  }
  
  protected void setValues(double y[])
  {
	  for(int i=0; i<this.y.length; ++i)
		  this.y[i] = y[i];
	  setDefined();
  }
  
  public void assign(UnaryFunction<Double> f) // записываем значение другой функции f
  {
	  for(int i=0; i<y.length; ++i)
		  setValue(i, f.invoke(this.getArgument(i)));
	  setDefined();
  }
  public void append(UnaryFunction<Double> f) // прибавляем значение другой функции f
  {
	  for(int i=0; i<y.length; ++i)
		  setValue(i, getValue(i) + f.invoke(this.getArgument(i)));
  }
  public void transform(UnaryFunction<Double> f) // применяем ко всем значениям другую функцию f
  {
	  for(int i=0; i<y.length; ++i)
		  setValue(i, f.invoke(this.getValue(i)));
  }
  
  public void read(Scanner s)
  {
	//s.useDelimiter(" ");
	for(int i=0; i<y.length; ++i)
	{
	  setValue(i, s.nextDouble()); // а если ошибка при чтении?????????????????
	}		
	setDefined();
  } 
    
  public void write(PrintStream out)
  {
	  for(int i=0; i<y.length; ++i)
	  {
		  out.printf("%f\n", y[i]);
	  }
  }
    
  // линейная интерполяция (может быть, лучше квадратичная????????????????????)
  public double invoke(double x)
  {
	double part = (x-a)/delta;
	int i = (int)Math.floor(part);
	return y[i] + (y[i+1]-y[i]) * (part - i);
  }
  
}