package org.interferometer;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

public class StripsInfo
  {
	  int m, n;
	  int k[][];
	  InterferometerRestoreFunction function;
	  LinkedList<Strip> strips;
	  TreeSet<Border> min_borders,
	  				  max_borders,
	  				  nil_borders;
	  
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
	  public PixelType getType(IntPoint p)
	  {
		  return getType(p.getX(), p.getY());
	  }
	  public void setType(int i, int j, PixelType type)
	  {
		  evaluations[i][j] = type;
	  }
	  public void setType(IntPoint p, PixelType type)
	  {
		  setType(p.getX(), p.getY(), type);
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
	  
	  private Border findBorder(Border.Type type, IntPoint point)
	  {
		  switch(type)
		  {
		  case Max: return this.max_borders.lower(new Border(point));
		  case Min: return this.min_borders.lower(new Border(point));
		  case Nil: return this.nil_borders.lower(new Border(point));
		  default: return null;
		  }
	  }
	  private Border addBorder(Border.Type type, IntPoint pt)
	  {
		  Border result = new Border(this, type, pt);
		  switch(type)
		  {
		  case Max: this.max_borders.add(result);
		  case Min: this.min_borders.add(result);
		  case Nil: this.nil_borders.add(result);
		  }
		  return result;
	  }
	  private void removeBorder(Border b)
	  {
		  switch(b.getType())
		  {
		  case Max: this.max_borders.remove(b);
		  case Min: this.min_borders.remove(b);
		  case Nil: this.nil_borders.remove(b);
		  }
	  }
	  private void increaseBorder(Border b, IntPoint pt)
	  {
		  b.addPixel(pt);
	  }
	  private void decreaseBorder(Border b)
	  {
		  b.removePixel();
		  if(b.isEmpty())
			  removeBorder(b);
	  }
	  private void linkBorder(Border b1, Border b2)
	  {
		  b1.addEnd(b2);
		  removeBorder(b2);
	  }
	  
	  /** Рисует предположительную границу, начиная с заданной точки */	  
	  private void createBorderLine(Border.Type type, Border newborder, IntPoint current_point)
	  {
		  System.out.printf("\n (i, j) = (%d, %d)", current_point.getX(), current_point.getY());
		  Border temp_border;
		  IntPoint last_pt = null,
				   temp_pt;
		  IntPoint.Neighbor neighbors[] = new IntPoint.Neighbor[4];
		  // TODO: учесть возможность замыкания в кольцо
		  // TODO: сделать удаление из массива предыдущей точки линии, если мы не предполагаем кольцевания
		  border_cycle: while(true)
		  {
			  // проверяем ближних соседей:
			  int good_neighbors = current_point.getNearNeighborsCount(this.evaluations, type.getPossibleBeginType(), neighbors);
			  int bad_neighbors = 0;
			  switch(good_neighbors)
			  {
			  case 0: good_neighbors = current_point.getNearNeighborsCount(this.evaluations, type.getMayBeType(), neighbors);
			  		switch(good_neighbors)
			  		{
			  		case 0: // проверяем дальних соседей:
			  				// TODO: сделать учёт собственного вектора матрицы 2-х производных
			  				bad_neighbors = current_point.getFarNeighborsCount(this.evaluations, type.getPossibleType(), neighbors);
			  				// в середину линии мы утыкаться не должны - в этом случае делаем откат:
			  				if((newborder.isOnlyBegin() && bad_neighbors > 0) || bad_neighbors > 1)
			  				{
			  					this.decreaseBorder(newborder);
				  				break border_cycle;
			  				}
			  				good_neighbors = current_point.getFarNeighborsCount(this.evaluations, type.getPossibleBeginType(), neighbors);
			  				switch(good_neighbors)
			  				{
			  				case 0: good_neighbors = current_point.getFarNeighborsCount(this.evaluations, type.getMayBeType(), neighbors);
			  						switch(good_neighbors)
			  						{
			  				  		case 1: temp_pt = current_point.getNeighbor(neighbors[0]);
			  				  				increaseBorder(newborder, temp_pt);
			  				  				current_point = temp_pt;
			  				  				continue border_cycle;
			  				  		case 2: if(newborder.isOnlyBegin())
					  				{
					  					IntPoint pt1 = current_point.getNeighbor(neighbors[0]),
					  							pt2 = current_point.getNeighbor(neighbors[1]);
					  					newborder.addBeginPixel(pt1);
					  					increaseBorder(newborder, pt2);
					  					current_point = pt2;
					  					continue border_cycle;
					  				}
			  				  		case 3:
			  				  		case 4: 
			  				  		case 0:this.decreaseBorder(newborder);
			  				  				break border_cycle;
			  						
			  						}
			  				case 1: temp_border = this.findBorder(type, current_point.getNeighbor(neighbors[0]));
			  						linkBorder(newborder, temp_border);
			  						break border_cycle;
			  				case 2: if(newborder.isOnlyBegin())
			  				{
			  					Border border1 = this.findBorder(type, current_point.getNeighbor(neighbors[0])),
			  							border2 = this.findBorder(type, current_point.getNeighbor(neighbors[1]));
			  					border1.reverse();
			  					linkBorder(border1, newborder);
			  					linkBorder(border1, border2);
			  					break border_cycle;
			  				}
			  				case 3:
			  				case 4: this.decreaseBorder(newborder);
			  						break border_cycle;

			  				}
			  		case 1: temp_pt = current_point.getNeighbor(neighbors[0]);
			  				increaseBorder(newborder, temp_pt);
			  				current_point = temp_pt;
		  					continue border_cycle;
			  		case 2: if(newborder.isOnlyBegin())
			  				{
			  					IntPoint pt1 = current_point.getNeighbor(neighbors[0]),
			  							pt2 = current_point.getNeighbor(neighbors[1]);
			  					newborder.addBeginPixel(pt1);
			  					increaseBorder(newborder, pt2);
			  					current_point = pt2;
			  					continue border_cycle;
			  				}
			  		case 3:
			  		case 4: this.decreaseBorder(newborder);
			  				break border_cycle;
			  }
			  case 1: System.out.println(neighbors[0]);
			  		  System.out.printf("\n neighbor = (%d, %d)", current_point.getNeighbor(neighbors[0]).getX(), current_point.getNeighbor(neighbors[0]).getY());
				  	  temp_border = this.findBorder(type, current_point.getNeighbor(neighbors[0]));
			  		  linkBorder(newborder, temp_border);
			  		  break border_cycle;
			  case 2: if(newborder.isOnlyBegin())
			  		{
				  		Border border1 = this.findBorder(type, current_point.getNeighbor(neighbors[0])),
				  				border2 = this.findBorder(type, current_point.getNeighbor(neighbors[1]));
				  		border1.reverse();
				  		linkBorder(border1, newborder);
				  		linkBorder(border1, border2);
				  		break border_cycle;
			  		}
			  case 3:
			  case 4: this.decreaseBorder(newborder);
			  		  break border_cycle;
			  }
		  }  
	  }
	  
	  /** Рисует предположительные границы */
	  private void createBorderLines(Border.Type type)
	  {
		  for(int i=0; i<this.m; ++i)
		  for(int j=0; j<this.n; ++j)
		  {
			  if(getType(i, j) != type.getMayBeType())
				  continue;
			  IntPoint current_point = new IntPoint(i, j);
			  Border newborder = this.addBorder(type, current_point);
			  // добавляем в границу точки, пока это возможно:
			  this.createBorderLine(type, newborder, current_point);
		  }
	  }
	  
	  // определяем минимумы функций там, где это возможно:
	  private void createMinBorders()
	  {
		  evaluateBorders(options.getMinOptions(), -1,
				  			0, 
				  			0, Utils.sqr(function.getXCoef()),
				  			PixelType.MayBeMin);
		  createBorderLines(Border.Type.Min); 
	  }
	  
	  // определяем максимумы функций там, где это возможно:
	  private void createMaxBorders()
	  {
		  evaluateBorders(options.getMaxOptions(), 1,
				  			0,
				  			0, -Utils.sqr(function.getXCoef()),
				  			PixelType.MayBeMax);
		  createBorderLines(Border.Type.Max); 
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
			  createBorderLines(Border.Type.Nil); 
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
