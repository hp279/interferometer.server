package org.interferometer;

import java.io.PrintStream;

public class StripsInfo
  {
	  int m, n;
	  int k[][];
	  int maxk;
	  InterferometerRestoreFunction function;
	  BordersInfo borders;
  	  StripsOptions options;	  
	  
	  public enum Status
	  {
		NotRestored,
		BordersRestored,
		StripsRestored,
		Error;
	  }
	  private Status status;
	
	  private void clearEvals() {
		  this.k = new int[ m ] [ n ];
		  this.maxk = 0;
		  for(int i=0; i<m; ++i)
			  for(int j=0; j<n; ++j)
				  k[i][j] = 0;
		  this.status = Status.NotRestored;		
	  }
	  
	  public StripsInfo(InterferometerRestoreFunction function, StripsOptions options) {
		  this.borders = new BordersInfo(function, options);
		  this.function = function;
		  this.m = function.getSizeX() + 1;
		  this.n = function.getSizeY() + 1;
		  this.options = options;
		  clearEvals();
	  }
	  
	  public Status getStatus() {
		  return status;
	  }
	    
	  public int getK(int i, int j) {
		  return k[i][j];
	  }
	  
	  public void write(PrintStream out) {
	      this.borders.write(out);
		for(int i=0; i<m; ++i) {
			for(int j=0; j<n; ++j)
			  if(function.hasArgumentInt(i, j))
				out.printf("%d ", k[i][j]);
			out.print('\n');
		}
	  }
	      	  	  
	  // заполняем массив k:
	  private void restoreK() {
		 // TODO: сделать восстановление по полосам  
	  }	  
	  
	  public void createStrips() {
	      borders.createBorders();
		  if(borders.getStatus() == BordersInfo.Status.BordersRestored) {
			  restoreK();
			  this.status = Status.StripsRestored; // TODO: проверить, что всё хорошо, иначе Error		  
		  }
	  }
	  
	  // оценка количества полос
	  public int evalStripsNumber() {
	      int sumborders = 0;
	      switch(this.status) {
	      case BordersRestored:
	              // TODO: использовать тут AggregatorFunction
	          for(int i=0; i<=function.getSizeX(); ++i)
	          for(int j=0; j<=function.getSizeY(); ++j)
                if(function.hasArgumentInt(i, j))
                    sumborders += (this.borders.getType(i, j) == BordersInfo.PixelType.Nothing? 0:1);
	          // среднее количество точек на единицу координаты x:
	          return (int)((double)sumborders / (function.getSizeX()+1) / (options.mustCreateNilBorders()? 3:2));
	      case StripsRestored:
	          return maxk;
	      case Error:
	      case NotRestored:
	      default:
	          return -1;  
	      } 
	  }
  }
