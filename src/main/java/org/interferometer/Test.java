package org.interferometer;

import org.interferometer.difference.Difference2Info;
import org.interferometer.difference.RestoredTableFunction;
import org.interferometer.difference.RestoredTableFunction2;
import org.interferometer.difference.Shift1Function2;
import org.interferometer.difference.Shift2Function2;
import org.interferometer.difference.ShiftFunction;
import org.interferometer.difference.DifferenceInfo;
import org.interferometer.function.AbstractFunction1;
import org.interferometer.function.AbstractFunction2;
import org.interferometer.function.RandomNoise2;
import org.interferometer.function.TableFunction;
import org.interferometer.function.TableFunction2;
import org.interferometer.set.Difference;
import org.interferometer.set.FlatSet;
import org.interferometer.set.Intersection;
import org.interferometer.set.Rectangle;
import org.interferometer.set.Round;
import org.ojalgo.matrix.BasicMatrix;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.OptimisationSolver;
import org.ojalgo.optimisation.OptimisationSolver.Result;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.quadratic.QuadraticExpressionsModel;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;

public class Test
{
	static void testSolver() 		// проверка, как работает квадратичное программирование
	{
		int n = 2; // количество переменных
		int m = 2; // количество ограничений
		Variable vars[] = new Variable[n];
		for(int i=0; i<n; ++i) // имена x1, x2, ... xn
			vars[i] = Variable.make(String.format("x%d", i+1));
		ExpressionsBasedModel<?> model = new QuadraticExpressionsModel(vars);
		for(int j=0; j<m; ++j) // добавляем линейные ограничения
		{
			Expression ex = model.addEmptyLinearExpression(String.format("%d", j+1));
			for(int i=0; i<n; ++i)
			{
				double aij = (i==j? 1:0); // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				ex.setLinearFactor(i, new BigDecimal(aij));
			}
			double bj = 0; // !!!!!!!!!!!!!!!!!!!!!!!!!
			// ограничения сверху и снизу:
			ex.setLowerLimit(new BigDecimal(bj));
			ex.setUpperLimit(new BigDecimal(bj+1));
		}
		Expression objex = model.addEmptyCompoundExpression("y");
		objex.setContributionWeight(new BigDecimal(1)); // 1 - минимизация, -1 - максимизация!
		// Добавляем квадратичную часть целевой функции.
		// Если минимизируем, матрица должна быть неотрицательно определённой. 
		// Если максимизируем - неположительно. Иначе работать не будет!
		for(int i=0; i<n; ++i)
			for(int j=0; j<n; ++j)
			{
				double qij = (i==j? 1:0); // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				objex.setQuadraticFactor(i, j, new BigDecimal(qij));
			}
		// Добавляем линейную часть целевой функции:
		for(int i=0; i<n; ++i)
		{
			double ci = -4+i; // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			objex.setLinearFactor(i, new BigDecimal(ci));
		}
		// Решаем задачу:
		OptimisationSolver solver = model.getDefaultSolver();
		// Если только равенства - работает LagrangeSolver. Он сводит решение к системе линейных уравнений
		// и вызывает EquationSystemSolver (решение в 1 шаг).
		// Если есть неравенства - ActiveSetSolver. Он на каждом шаге устанавливает активные неравенства
		// и использует LagrangeSolver, в котором активные неравенства заменяются на равенства, а остальные откидываются.
		System.out.printf("Matrices: \n%s", solver.toString());		
		Result result = solver.solve();
		BasicMatrix vector = result.getSolution();
		switch(result.getState())
		{
		// Глюка:
		case NEW: System.out.printf("New!\n");
		case FAILED: System.out.printf("Failed!\n");
					break;
		// Решений нет:
		case ITERATION:  System.out.printf("Iteration!\n");
		case INFEASIBLE: System.out.printf("Infeasible!\n");
					break;
		// Задача неограничена:
		case UNBOUNDED: System.out.printf("Unbounded!\n");
					break;
		// Решение найдено:
		case UNIQUE: System.out.printf("Unique!\n");
		case OPTIMAL: System.out.printf("Optimal!\n");
		case FEASIBLE: System.out.printf("Feasible!\n");
		// Выводим его на экран:
					for(int i=0; i<vector.getRowDim(); ++i)
						System.out.printf("x%d = %f\n", i+1, vector.toBigDecimal(i, 0).doubleValue());
					break;
		}
	}
	
	static void testRestore1() // проверка восстановления 1-мерной функции по разности
	{
		  // создаём табличную функцию из значений в input.txt
		TableFunction fun = new TableFunction(0, 100, 100);
		double s = 10;
		try
		{
		  FileInputStream fin = new FileInputStream("input.txt");
		  fun.read( new DataInputStream(fin) );
		
		// создаём разностную функцию
		  AbstractFunction1 deltaf = new ShiftFunction(fun, s);
		  DifferenceInfo diff = new DifferenceInfo(deltaf, s);
		  
		// на всякий случай её записываем:
		  TableFunction tabledeltaf = new TableFunction(10, 99, 89);
		  tabledeltaf.assign(deltaf);
		  FileOutputStream fdeltaout = new FileOutputStream("outputd.txt");
		  tabledeltaf.write( new DataOutputStream(fdeltaout) );
		
		// восстанавливаем исходную функцию по разностной
		  final double hl = 1;
		  final double halpha = 0.5; 
		  AbstractFunction1 helder = new AbstractFunction1() // предполагаем, что функция удовлетворяет условию Гёльдера с такими константами
		  {
			  public double invoke(double x)
			  {
				return hl*Math.pow(x, halpha);
			  }
		  };
		
		  RestoredTableFunction fun2 = new RestoredTableFunction(0, 100, 100, helder);
		  fun2.restoreByShift(diff);
		  System.out.printf("Function is restored!");
		// записываем восстановленную функцию в output.txt - должно получиться близко к исходной функции
		  FileOutputStream fout = new FileOutputStream("output.txt");
		  fun2.write( new DataOutputStream(fout) );
		 }
		 catch(FileNotFoundException ex)
		 {
		  ex.printStackTrace();
		 }	
	}
	
	
	
	static FlatSet getTestSet(double xcenter, double ycenter, double r, double R)
	{
		// колечко с большим радиусом R и маленьким r
		FlatSet biground = new Round(xcenter, ycenter, R),
				smallround = new Round(xcenter, ycenter, r);
		FlatSet circle = new Difference(biground, smallround);
		// сперва вернём верхнюю левую четверть:
		Rectangle ulrect = new Rectangle(-Double.MAX_VALUE, -Double.MAX_VALUE, xcenter, ycenter);
		return new Intersection(circle, ulrect);
	}
	
	
	// проверка восстановления 2-мерной функции по разности
	static void testRestore2(int size, int[] sarray, double h, double sigma) 
	{
		  // создаём табличную функцию из значений в input.txt
		TableFunction2 fun = new TableFunction2(0, size, 0, size, size, size);
		 // устанавливаем множество:
		 FlatSet area = getTestSet(size/2, size/2, size/4, size/2);
		 fun.setArea(area);
		try
		{
		  FileInputStream fin = new FileInputStream("input.txt");
		  fun.read( new DataInputStream(fin) );
		  FileOutputStream fareaout = new FileOutputStream("area.txt");
		  fun.write( new DataOutputStream(fareaout) );

		  // случайный шум:
		  AbstractFunction2 noise = new RandomNoise2(sigma);
		
		// создаём разностные функции
		  int difnum = sarray.length;
		  Difference2Info[] diff = new Difference2Info[difnum];
		  for(int i=0; i<difnum; ++i)
		  {
			  int s = sarray[i];
			  AbstractFunction2 delta1f = new Shift1Function2(fun, s);
			  AbstractFunction2 delta2f = new Shift2Function2(fun, s);
			  TableFunction2 tabledelta1f = new TableFunction2(s, size, 0, size, size-s, size),
					  		 tabledelta2f = new TableFunction2(0, size, s, size, size, size-s);
			  tabledelta1f.setArea(delta1f.getArea());
			  tabledelta2f.setArea(delta2f.getArea());
			  tabledelta1f.assign(delta1f);
			  tabledelta2f.assign(delta2f);
			  // добавляем случайный шум:
			  tabledelta1f.append(noise);
			  tabledelta2f.append(noise);
			  
			  // на всякий случай их записываем:
			  String filename1 = String.format("outputs%dd1.txt", i+1),
					 filename2 = String.format("outputs%dd2.txt", i+1); 
			  FileOutputStream fdelta1out = new FileOutputStream(filename1);
			  tabledelta1f.write( new DataOutputStream(fdelta1out) );
			  FileOutputStream fdelta2out = new FileOutputStream(filename2);
			  tabledelta2f.write( new DataOutputStream(fdelta2out) );
			  diff[i] = new Difference2Info(tabledelta1f, tabledelta2f, s);		  
		  }
	
		  // восстанавливаем исходную функцию по разностной
		  final double hl = 1;
		  final double halpha = 0.5; 
		  AbstractFunction1 helder = new AbstractFunction1() // предполагаем, что функция удовлетворяет условию Гёльдера с такими константами
		  {
			  public double invoke(double x)
			  {
				return hl*Math.pow(x, halpha);
			  }
		  };
		
		  RestoredTableFunction2 fun2 = new RestoredTableFunction2(0, size, 0, size, size, size, helder, h);
		  fun2.setArea(area);
		  fun2.restoreByShift(diff);
		  System.out.printf("Function is restored!");
		// записываем восстановленную функцию в output.txt - должно получиться близко к исходной функции
		  FileOutputStream fout = new FileOutputStream("output.txt");
		  fun2.write( new DataOutputStream(fout) );
		 }
		 catch(FileNotFoundException ex)
		 {
		  ex.printStackTrace();
		 }	
	}
	
 static void testInterfer(int size, int[] sarray, double h, double a, double b, double lambda, double deltaz, double sigma)
 {
	  // создаём табличную функцию из значений в input.txt
	TableFunction2 fun = new TableFunction2(0, size, 0, size, size, size);
	// устанавливаем множество:
	//FlatSet area = getTestSet(size/4, size/2);
	//fun.setArea(area);
	// случайный шум:
	  AbstractFunction2 noise = new RandomNoise2(sigma);
	try
	{
			  FileInputStream fin = new FileInputStream("input.txt");
			  fun.read( new DataInputStream(fin) );
			
			// создаём разностные функции
			  int difnum = sarray.length;
			  Difference2Info[] diff = new Difference2Info[difnum];
			  for(int i=0; i<difnum; ++i)
			  {
				  int s = sarray[i];
				  AbstractFunction2 delta1f = new Interferometer1Function2(fun, s, a, b, lambda);
				  AbstractFunction2 delta2f = new Interferometer2Function2(fun, s, a, b, lambda);
				  			  
				  // создаём восстанавливающие функции 
				  InterferometerRestoreFunction picture1 = new InterferometerRestoreFunction(InterferometerRestoreFunction.Type.ByX, 
						  																	  s, size, 0, size,
						  																	  size-s, size,
						  																	  a, b, lambda, deltaz);
				  picture1.setArea(delta1f.getArea());
				  picture1.assign(delta1f);
				  picture1.append(noise);
				  picture1.createStrips();
				  InterferometerRestoreFunction picture2 = new InterferometerRestoreFunction(InterferometerRestoreFunction.Type.ByY,
						  																	  0, size, s, size,
						  																	  size, size-s,
						  																	  a, b, lambda, deltaz);
				  picture2.setArea(delta2f.getArea());
				  picture2.assign(delta2f);
				  picture2.append(noise);
				  picture2.createStrips();
				  
				  // на всякий случай их записываем:
				  String filename1 = String.format("outputs%di1.txt", i+1),
						 filename2 = String.format("outputs%di2.txt", i+1); 
				  FileOutputStream fdelta1out = new FileOutputStream(filename1);
				  picture1.write( new DataOutputStream(fdelta1out) );
				  FileOutputStream fdelta2out = new FileOutputStream(filename2);
				  picture2.write( new DataOutputStream(fdelta2out) );

				  // восстанавливаем разности				  
				  TableFunction2 diff1 = picture1.restore();
				  TableFunction2 diff2 = picture2.restore();
				  diff[i] = new Difference2Info(diff1, diff2, s);
				  
				  // Записываем разности:
				  filename1 = String.format("outputs%dd1.txt", i+1);
				  filename2 = String.format("outputs%dd2.txt", i+1); 
				  fdelta1out = new FileOutputStream(filename1);
				  diff1.write( new DataOutputStream(fdelta1out) );
				  fdelta2out = new FileOutputStream(filename2);
				  diff2.write( new DataOutputStream(fdelta2out) );
			  }		

			  // восстанавливаем исходную функцию по разностной
			  final double hl = 1;
			  final double halpha = 0.5; 
			  AbstractFunction1 helder = new AbstractFunction1() // предполагаем, что функция удовлетворяет условию Гёльдера с такими константами
			  {
				  public double invoke(double x)
				  {
					return hl*Math.pow(x, halpha);
				  }
			  };
			
			  // восстанавливаем то, что в наименьшем прямоугольнике - то есть размером s[difnum-1]
			  int maxs = sarray[difnum-1];
			  RestoredTableFunction2 fun2 = new RestoredTableFunction2(maxs, size - maxs, maxs, size - maxs,
					  												   size - maxs, size - maxs,
					  												   helder, h);
			  //fun2.setArea(area);
			  fun2.restoreByShift(diff);
			  System.out.printf("Function is restored!");
			// записываем восстановленную функцию в output.txt - должно получиться близко к исходной функции
			  FileOutputStream fout = new FileOutputStream("output.txt");
			  fun2.write( new DataOutputStream(fout) );
			 }
			 catch(FileNotFoundException ex)
			 {
			  ex.printStackTrace();
			 }	
 }

static void testInterferFromFile(String filename, FlatSet area, double a)
{
	InterferometerRestoreFunction funx = new InterferometerRestoreFunction(InterferometerRestoreFunction.Type.ByX,
																			a, 0, 1),
								  funy = new InterferometerRestoreFunction(InterferometerRestoreFunction.Type.ByY,
										  									a, 0, 1);
	try {
		funx.setArea(area);
		funy.setArea(area);
		funx.load(filename + ".bmp");
		funy.load(filename + ".bmp");
		funx.createStrips();
		funy.createStrips();
		String filename1 = String.format(filename + "_x.txt"),
				filename2 = String.format(filename + "_y.txt"); 
		FileOutputStream fdelta1out, fdelta2out;
		fdelta1out = new FileOutputStream(filename1);
		funx.write( new DataOutputStream(fdelta1out) );
		fdelta2out = new FileOutputStream(filename2);
		funy.write( new DataOutputStream(fdelta2out) );
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}
	
  public static void main(String args[])
  {
	  // testSolver();
	 // testRestore1();
	  int[] sarray = new int[2];
	  sarray[0] = 3;
	  sarray[1] = 5;
//	  sarray[2] = 7;
//	  sarray[3] = 8;
	 // testRestore2(26, sarray, 1, 0.05);
	  // a, b, lambda!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	  //testInterfer(13, sarray, 1, 1, 1, 3, 1e-2, 0.1);
	  String file1 = "Кольцо Can",
			  file2 = "54",
			  file3 = "123_sphere_3";
	  FlatSet area1 = getTestSet(218, 207, 85, 210),
			  area2 = getTestSet(218, 207, 85, 210), // для 54.bmp
			  area3 = getTestSet(197, 197, 106, 197); // для 123_sphere_3
	  double a1 = 1,
			 a2 = 30.0 / 420,
			 a3 = 30.0 / 396;
	  testInterferFromFile(file3, area3, a3);
  }
}
