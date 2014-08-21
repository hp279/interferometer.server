package org.interferometer;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import org.interferometer.function.AbstractFunction2;
import org.interferometer.function.TableFunction2;
import org.interferometer.linear.Matrix22;
import org.interferometer.linear.Vector2;

public class StripsInfo
  {
	  int m, n;
	  int k[][];
	  InterferometerRestoreFunction function;
	  LinkedList<Strip> strips;
	  TreeSet<Border> min_borders,
	  				  max_borders,
	  				  nil_borders;
	  
	  public static class StripsOptions
	  {
		  public class EvaluateOptions
		  {
			  double h1, h2;
		  	  double e0, e1, e2;
		  	  public EvaluateOptions(double h1, double h2, double e0, double e1, double e2)
		  	  {
		  		  this.h1 = h1;
		  		  this.h2 = h2;
		  		  this.e0 = e0;
		  		  this.e1 = e1;
		  		  this.e2 = e2;
		  	  }
		  	  
		  	  public boolean evalFunction(double val, double what)
		  	  {
		  		return val > what-e0 && val < what+e0;
		  	  }
		  	  
		  	  public boolean evalDiff(TableFunction2 function, double x, double y, double norm)
		  	  {
		  		  //System.out.printf("\n(x, y, h1) = (%.15f, %.15f, %.15f)", x, y, h1);
		  		  if(!eval_diff1)
		  			  return true;
		  		  if(function.hasSquare(x, y, h1))
				  {
		  			//System.out.printf(" good (x, y, h1) in [%f; %f]*[%f; %f]", function.getMinX(), function.getMaxX(), function.getMinY(), function.getMaxY());
					  Vector2 diff1 = function.diff(x, y, h1);
					  return diff1.isNorm2Equal(norm, e1);//diff1.sub(what).getNormInf() < e1;
				  }
		  		  return false;
		  	  }
		  	  
		  	  public boolean evalDiff2(AbstractFunction2 function, double x, double y, double det, double trace)
		  	  {
		  		  if(!eval_diff2)
		  			  return true;
		  		  if(function.hasSquare(x, y, h2*2))
				  {
					  Matrix22 diff2 = function.diff2(x, y, h2);
					  return diff2.isDetEqual(det, e2) && diff2.isTraceEqual(trace, e2);
				  }
		  		  return false;
		  	  }
		  	  
		  	  public boolean isConstantCenter(AbstractFunction2 function, double x, double y)
		  	  {
		  		  if(!check_constant_center)
		  			  return true;
		  		  double value = function.invoke(x, y);
		  		  boolean xmin_edge = false,
		  				  xmax_edge = false,
		  				  ymin_edge = false,
		  				  ymax_edge = false;
		  		  int xmin_dist = 1,
		  			  xmax_dist = 1,
		  			  ymin_dist = 1,
		  			  ymax_dist = 1;
		  		  do
		  			  if(!function.hasArgument(x-h1*xmin_dist, y))
		  			  {
		  				  xmin_edge = true;
		  				  break;
		  			  }
		  		  while(evalFunction(function.invoke(x-h1*xmin_dist++, y), value));
		  		  do
		  			  if(!function.hasArgument(x+h1*xmax_dist, y))
		  			  {
		  				  xmax_edge = true;
		  				  break;
		  			  }
		  		  while(evalFunction(function.invoke(x+h1*xmax_dist++, y), value));
		  		  if((!xmin_edge && xmin_dist < xmax_dist) || (!xmax_edge && xmin_dist > xmax_dist))
		  			  return false;
		  		  do
		  			  if(!function.hasArgument(x, y-h1*ymin_dist))
		  			  {
		  				  ymin_edge = true;
		  				  break;
		  			  }
		  		  while(evalFunction(function.invoke(x, y-h1*ymin_dist++), value));
		  		  do
		  			  if(!function.hasArgument(x, y+h1*ymax_dist))
		  			  {
		  				  ymax_edge = true;
		  				  break;
		  			  }
		  		  while(evalFunction(function.invoke(x, y+h1*ymax_dist++), value));
		  		  if((!ymin_edge && ymin_dist < ymax_dist) || (!ymax_edge && ymin_dist > ymax_dist))
		  			  return false;
		  		  return true;
		  	  }
		  }
		  EvaluateOptions min_options,
		  				  max_options,
		  				  nil_options;
	  	  boolean eval_diff1; // проверять ли градиент		  
	  	  boolean eval_diff2; // проверять ли матрицу 2-х производных
	  	  boolean check_constant_center; // проверять ли принадлежность к центру полосы, где функция постоянна
	  	  boolean create_nil_borders; // строить ли границы, где значения функции равны 0
	  	  
	  	  public StripsOptions(boolean eval_diff1, boolean eval_diff2, boolean check_constant_center, boolean create_nil_borders)
	  	  {
	  		  this.min_options = null;
	  		  this.max_options = null;
			  this.eval_diff1 = eval_diff1;
			  this.eval_diff2 = eval_diff2;
			  this.check_constant_center = check_constant_center;
			  this.create_nil_borders = create_nil_borders;
	  	  }
	  	  public StripsOptions()
	  	  {
	  		  this(false, true, true, true);
	  	  }
	  	  public EvaluateOptions getMinOptions()
	  	  {
	  		  return min_options;
	  	  }
	  	  public EvaluateOptions getMaxOptions()
	  	  {
	  		  return max_options;
	  	  }
	  	  public EvaluateOptions getNilOptions()
	  	  {
	  		  return nil_options;
	  	  }
	  	  public void setMinOptions(double h1, double h2, double e0, double e1, double e2)
	  	  {
	  		  min_options = new EvaluateOptions(h1, h2, e0, e1, e2);
	  	  }
	  	  public void setMaxOptions(double h1, double h2, double e0, double e1, double e2)
	  	  {
	  		  max_options = new EvaluateOptions(h1, h2, e0, e1, e2);
	  	  }
	  	  public void setNilOptions(double h1, double h2, double e0, double e1, double e2)
	  	  {
	  		  nil_options = new EvaluateOptions(h1, h2, e0, e1, e2);
	  	  }
	  	  
	  	  public boolean mustCreateNilBorders()
	  	  {
	  		  return this.create_nil_borders;
	  	  }
	  }
	  StripsOptions options;	  
	  
	  public enum Status
	  {
		NotRestored,
		BordersRestored,
		StripsRestored,
		Error;
	  }
	  private Status status;
	
	  public enum PixelType
	  {
		Nothing, // не идентифицирована
		MayBeMax, // возможная точка максимума
		PossibleMax, // точка максимума, причисленная к какой-то линии
		PossibleMaxAndBegin, // точка максимума, являющаяся началом или концом какой-то линии
		IThinkMax, // окончательно идентифицирована как точка максимума и часть линии
		MayBeMin, // возможная точка минимума
		PossibleMin, // точка минимума, причисленная к какой-то линии
		PossibleMinAndBegin, // точка минимума, являющаяся началом или концом какой-то линии
		IThinkMin, // окончательно идентифицирована как точка минимума и часть линии
		MayBeNil, // возможная точка, где функция равна 0
		PossibleNil, // точка равенства 0, причисленная к какой-то линии
		PossibleNilAndBegin, // точка равенства 0, являющаяся началом или концом какой-то линии
		IThinkNil // окончательно идентифицирована как точка, где функция равна 0, и часть линии уровня
	  }
	  
	  PixelType[][] evaluations;

	  private void clearEvals()
	  {
		  this.k = new int[ m ] [ n ];
		  for(int i=0; i<m; ++i)
			  for(int j=0; j<n; ++j)
				  k[i][j] = 0;
		  this.evaluations = new PixelType[ m ] [ n ];
		  for(int i=0; i<m; ++i)
			  for(int j=0; j<n; ++j)
				  evaluations[i][j] = PixelType.Nothing;
		  this.strips = new LinkedList<Strip>();
		  this.min_borders = new TreeSet<Border>();
		  this.max_borders = new TreeSet<Border>();
		  this.nil_borders = new TreeSet<Border>();
		  this.status = Status.NotRestored;		
	  }
	  
	  public StripsInfo(InterferometerRestoreFunction function, StripsOptions options)
	  {
		  this.function = function;
		  this.m = function.getSizeX() + 1;
		  this.n = function.getSizeY() + 1;
		  this.options = options;
		  clearEvals();
	  }
	  
	  public Status getStatus()
	  {
		  return status;
	  }
	  
	  public PixelType getType(int i, int j)
	  {
		  return evaluations[i][j];
	  }
	  
	  public int getK(int i, int j)
	  {
		  return k[i][j];
	  }
	  
	  public void write(PrintStream out)
	  {
		for(int i=0; i<m; ++i)
		{
			for(int j=0; j<n; ++j)
			  if(function.hasArgument(function.getArgument1(i), function.getArgument2(j)))
				switch(evaluations[i][j])
				{
				case Nothing: out.printf("0    "); break;
				case MayBeMax: out.printf("+1   "); break;
				case PossibleMax: out.printf("+2   "); break;
				case PossibleMaxAndBegin: out.printf("+2.1 "); break;
				case IThinkMax: out.printf("+3   "); break;
				case MayBeMin: out.printf("-1   "); break;
				case PossibleMin: out.printf("-2   "); break;
				case PossibleMinAndBegin: out.printf("-2.1 "); break;
				case IThinkMin: out.printf("-3   "); break;
				case MayBeNil: out.printf("0.1  "); break;
				case PossibleNil: out.printf("0.2  "); break;
				case PossibleNilAndBegin: out.printf("0.21 "); break;
				case IThinkNil: out.printf("0.3  "); break;
				}
			  else
				  out.printf("--   ");
			out.print('\n');
		}
		for(int i=0; i<m; ++i)
		{
			for(int j=0; j<n; ++j)
			  if(function.hasArgument(function.getArgument1(i), function.getArgument2(j)))
				out.printf("%d ", k[i][j]);
			out.print('\n');
		}
	  }
	      	  
	  private void evaluateBorders(StripsOptions.EvaluateOptions opts, 
			  						double must_value, double must_grad, double must_det, double must_trace,
			  						PixelType eval_type)
	  {
		  for(int i=0; i<m; ++i)
		  for(int j=0; j<n; ++j)
		  {
			  double x = function.getArgument1(i),
					 y = function.getArgument2(j); 
			  if(function.hasArgument(x, y))
			  {
				  double val = function.getValue(i, j);
				  if(opts.evalFunction(val, must_value))
				  {
						  if(opts.evalDiff(function, x, y, must_grad) && opts.isConstantCenter(function, x, y))
						  {
							  if(opts.evalDiff2(function, x, y, must_det, must_trace)) 
									  this.evaluations[i][j] = eval_type;
						  }
				  }
			  }
		  } 
	  }
	  
	  private void createBorderLines(Set<Border> borders, PixelType may_be_type, PixelType possible_type, PixelType possible_type_and_begin)
	  {
		  // TODO: сделать
	  }
	  
	  // определяем минимумы функций там, где это возможно:
	  private void createMinBorders()
	  {
		  evaluateBorders(options.getMinOptions(), -1,
				  			0, 
				  			0, Utils.sqr(function.getXCoef()),
				  			PixelType.MayBeMin);
		  createBorderLines(this.min_borders, PixelType.MayBeMin, PixelType.PossibleMin, PixelType.PossibleMinAndBegin); 
	  }
	  
	  // определяем максимумы функций там, где это возможно:
	  private void createMaxBorders()
	  {
		  evaluateBorders(options.getMaxOptions(), 1,
				  			0,
				  			0, -Utils.sqr(function.getXCoef()),
				  			PixelType.MayBeMax);
		  createBorderLines(this.max_borders, PixelType.MayBeMax, PixelType.PossibleMax, PixelType.PossibleMaxAndBegin); 
	  }
	  
	  private void createNilBorders()
	  {
		  if(options.mustCreateNilBorders())
		  {
			  evaluateBorders(options.getNilOptions(), 0, 
					  		function.getXCoef(), 
					  		0, 0, 
					  		PixelType.MayBeNil);
			  System.out.printf("Nil borders created!");
			  createBorderLines(this.nil_borders, PixelType.MayBeNil, PixelType.PossibleNil, PixelType.PossibleNilAndBegin); 
		  }
	  }
	  
	  // соединяем полосы:
	  private void linkBorders()
	  {
		  // TODO: сделать паросочетание в графе
	  }
	  
	  // заполняем массив k:
	  private void restoreK()
	  {
		 // TODO: сделать восстановление по полосам  
	  }	  
	  
	  public void createStrips()
	  {
		  createMaxBorders();
		  createMinBorders();
		  createNilBorders();
		  this.status = Status.BordersRestored; // TODO: проверить, что всё хорошо, иначе Error
		  if(status == Status.BordersRestored)
		  {
			  linkBorders();
			  restoreK();
			  this.status = Status.StripsRestored; // TODO: проверить, что всё хорошо, иначе Error		  
		  }
	  }
  }
