package org.interferometer;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections15.set.ListOrderedSet;
import org.interferometer.linear.Vector2;
import org.interferometer.math.Utils;
import org.interferometer.util.ArraysEx;
import org.interferometer.util.IntPoint;
import org.interferometer.util.Pair;

public class BordersInfo
  {
      int m, n;
      InterferometerRestoreFunction function;
      ListOrderedSet<Border> min_borders,
                      max_borders,
                      nil_borders;
      IntPoint far_point; // "бесконечно удалённая точка" - с ней соединяются все границы, уходящие за пределы поля
      StripsOptions options;      
      
      public enum Status
      {
        NotRestored,
        BordersRestored,
        BordersLinked,
        Error;
      }
      private Status status;
    
      public enum PixelType {
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
        IThinkNil; // окончательно идентифицирована как точка, где функция равна 0, и часть линии уровня
        
        public boolean inLine() {
            return !(this == Nothing || this == MayBeMax || this == MayBeMin || this == MayBeNil);
        }
         
        public void write(PrintStream out) {
            switch(this)
            {
            case Nothing: 
                out.printf("0    "); break;
            case MayBeMax:
                out.printf("+1   "); break;
            case PossibleMax:
                out.printf("+2   "); break;
            case PossibleMaxAndBegin:
                out.printf("+2.1 "); break;
            case IThinkMax:
                out.printf("+3   "); break;
            case MayBeMin:
                out.printf("-1   "); break;
            case PossibleMin:
                out.printf("-2   "); break;
            case PossibleMinAndBegin:
                out.printf("-2.1 "); break;
            case IThinkMin:
                out.printf("-3   "); break;
            case MayBeNil: 
                out.printf("0.1  "); break;
            case PossibleNil:
                out.printf("0.2  "); break;
            case PossibleNilAndBegin: 
                out.printf("0.21 "); break;
            case IThinkNil: 
                out.printf("0.3  "); break;
            }
        }     
      }
      PixelType[][] evaluations;

      private void clearEvals() {
          this.evaluations = new PixelType[ m ] [ n ];
          for(int i=0; i<m; ++i)
              for(int j=0; j<n; ++j)
                  evaluations[i][j] = PixelType.Nothing;
          this.min_borders = new ListOrderedSet<Border>();
          this.max_borders = new ListOrderedSet<Border>();
          this.nil_borders = new ListOrderedSet<Border>();
          this.status = Status.NotRestored;     
      }
      
      public BordersInfo(InterferometerRestoreFunction function, StripsOptions options) {
          this.function = function;
          this.far_point = new IntPoint(Integer.MAX_VALUE, Integer.MAX_VALUE);
          this.m = function.getSizeX() + 1;
          this.n = function.getSizeY() + 1;
          this.options = options;
          clearEvals();
      }
      
      public Status getStatus() {
          return status;
      }
      
      public boolean hasArgument(IntPoint pt) {
          return this.function.hasArgumentInt(pt);
      }
      
      public PixelType getType(int i, int j) {
          return evaluations[i][j];
      }
      public PixelType getType(IntPoint p) {
          return getType(p.getX(), p.getY());
      }
      public void setType(int i, int j, PixelType type) {
          evaluations[i][j] = type;
      }
      public void setType(IntPoint p, PixelType type) {
          if(!p.equals(far_point))
              setType(p.getX(), p.getY(), type);
      }
      
      private Set<Border> getBordersByType(Border.Type type) {
          switch(type) {
          case Max: 
              return this.max_borders;
          case Min: 
              return this.min_borders;
          case Nil:
              return this.nil_borders;
          default: 
              return null;
          }
      }
           
      public void write(PrintStream out) {
        for(int i=0; i<m; ++i) {
            for(int j=0; j<n; ++j)
              if(function.hasArgumentInt(i, j))
                  evaluations[i][j].write(out);
              else
                  out.printf("--   ");
            out.print('\n');
        }
      }
              
      private void evaluateBorders(StripsOptions.EvaluateOptions opts, 
                                    double must_value, double must_grad, double must_det, double must_trace,
                                    PixelType eval_type) {
          for(int i=0; i<m; ++i)
          for(int j=0; j<n; ++j) {
              double x = function.getArgument1(i),
                     y = function.getArgument2(j); 
              if(function.hasArgument(x, y)) {
                  double val = function.getValue(i, j);
                  if(opts.evalFunction(val, must_value)) {
                          if(opts.evalDiff(function, x, y, must_grad) && opts.isConstantCenter(function, x, y)) {
                              if(opts.evalDiff2(function, x, y, must_det, must_trace)) 
                                      this.evaluations[i][j] = eval_type;
                          }
                  }
              }
          } 
      }
      
      private Border findBorder(Border.Type type, IntPoint point) {
          return ArraysEx.find(this.getBordersByType(type), new Border(this, point));
      }
      private Border addBorder(Border.Type type, IntPoint pt) {
          Border result = new Border(this, type, pt);
          this.getBordersByType(type).add(result);
          return result;
      }
      private void removeBorder(Border b) {
          this.getBordersByType(b.getType()).remove(b);
      }
      private void increaseBorder(Border b, IntPoint pt) {
          b.addPixel(pt);
      }
      private void decreaseBorder(Border b) {
          b.removePixel();
          if(b.isEmpty())
              removeBorder(b);
      }
      private void linkBorder(Border b1, Border b2) {
          if(b1 == b2) // замыкаем в кольцо
              b1.setRing();
          else {
              b1.addEnd(b2, false);
              removeBorder(b2);
          }
      }
      
      private int getNearNeighbors(IntPoint last_point, IntPoint point, PixelType type, IntPoint.Neighbor neighbors[]) {
  //        if(last_point != null)
  //            System.out.printf("\n getNearNeighbors: last_point=(%d, %d), point=(%d, %d)", last_point.getX(), last_point.getY(), point.getX(), point.getY());
          int result = point.getNearNeighborsCount(this.evaluations, type, neighbors);
          int from = 0,
              real_from = 0;
          for(;; ++from, ++real_from) {
//              System.out.println(real_from);
//              System.out.println(array[real_from]);
                while(real_from < result && point.getNeighbor(neighbors[real_from]).equals(last_point))
                    real_from++;
                if(real_from >= result)
                    break;
                neighbors[from] = neighbors[real_from];
            }
          return from;
      }
      private int getFarNeighbors(IntPoint last_point, IntPoint point, PixelType type, IntPoint.Neighbor neighbors[]) {
          int result = point.getFarNeighborsCount(this.evaluations, type, neighbors);
          int from = 0,
          real_from = 0;
          for(;; ++from, ++real_from) {
//                  System.out.println(real_from);
//                  System.out.println(array[real_from]);
                    while(real_from < result && point.getNeighbor(neighbors[real_from]).equals(last_point))
                        real_from++;
                    if(real_from >= result)
                        break;
                    neighbors[from] = neighbors[real_from];
                }
          return from;
    }
      
      /** Рисует предположительную границу, начиная с заданной точки */   
      private void createBorderLine(Border.Type type, Border newborder, IntPoint current_point) {
          System.out.printf("\n Begin: (i, j) = (%d, %d)", current_point.getX(), current_point.getY());
          Border temp_border;
          IntPoint temp_pt;
          IntPoint.Neighbor neighbors[] = new IntPoint.Neighbor[4];
          border_cycle: while(true) {
              // проверяем ближних соседей:
              int good_neighbors = getNearNeighbors(newborder.getPrevLast(), current_point, type.getPossibleBeginType(), neighbors);
              System.out.printf("\n (i, j) = (%d, %d); good_neighbors = %d", current_point.getX(), current_point.getY(), good_neighbors);
              int bad_neighbors = 0;
              switch(good_neighbors) {
                case 0:
                    good_neighbors = getNearNeighbors(newborder.getPrevLast(), current_point, type.getMayBeType(), neighbors);
                    switch(good_neighbors) {
                    case 0: 
                        // проверяем дальних соседей:
                        // TODO: сделать учёт собственного вектора матрицы 2-х производных
                        bad_neighbors = getFarNeighbors(newborder.getPrevLast(), current_point, type.getPossibleType(), neighbors);
                        // в середину линии мы утыкаться не должны - в этом случае делаем откат:
                        if(bad_neighbors > 0) {
                            this.decreaseBorder(newborder);
                            break border_cycle;
                        }
                        good_neighbors = getFarNeighbors(newborder.getPrevLast(), current_point, type.getPossibleBeginType(), neighbors);
                        switch(good_neighbors) {
                        case 0: 
                            good_neighbors = getFarNeighbors(newborder.getPrevLast(), current_point, type.getMayBeType(), neighbors);
                            switch(good_neighbors) {
                            case 1:
                                temp_pt = current_point.getNeighbor(neighbors[0]);
                                increaseBorder(newborder, temp_pt);
                                current_point = temp_pt;
                                continue border_cycle;
                            case 2:
                                if(newborder.isOnlyBegin()) {
                                    IntPoint pt1 = current_point.getNeighbor(neighbors[0]),
                                            pt2 = current_point.getNeighbor(neighbors[1]);
                                    newborder.addBeginPixel(pt1);
                                    increaseBorder(newborder, pt2);
                                    current_point = pt2;
                                    continue border_cycle;
                                }
                            case 3:
                            case 4: 
                            case 0:
                                this.decreaseBorder(newborder);
                                break border_cycle;
                            }
                        case 1:
                            temp_border = this.findBorder(type, current_point.getNeighbor(neighbors[0]));
                            linkBorder(newborder, temp_border);
                            break border_cycle;
                        case 2:
                            if(newborder.isOnlyBegin()) {
                                Border border1 = this.findBorder(type, current_point.getNeighbor(neighbors[0])),
                                        border2 = this.findBorder(type, current_point.getNeighbor(neighbors[1]));
                                border1.reverse();
                                linkBorder(border1, newborder);
                                linkBorder(border1, border2);
                                break border_cycle;
                            }
                        case 3:
                        case 4:
                            this.decreaseBorder(newborder);
                            break border_cycle;
                        }
                    case 1: 
                        temp_pt = current_point.getNeighbor(neighbors[0]);
                        increaseBorder(newborder, temp_pt);
                        current_point = temp_pt;
                        continue border_cycle;
                    case 2: 
                        if(newborder.isOnlyBegin()) {
                            IntPoint pt1 = current_point.getNeighbor(neighbors[0]),
                                    pt2 = current_point.getNeighbor(neighbors[1]);
                            newborder.addBeginPixel(pt1);
                            increaseBorder(newborder, pt2);
                            current_point = pt2;
                            continue border_cycle;
                        }
                    case 3:
                    case 4:
                        this.decreaseBorder(newborder);
                        break border_cycle;
                    }
              case 1:
                  // System.out.println(neighbors[0]);
                  // System.out.printf("\n neighbor = (%d, %d)", current_point.getNeighbor(neighbors[0]).getX(), current_point.getNeighbor(neighbors[0]).getY());
                  temp_border = this.findBorder(type, current_point.getNeighbor(neighbors[0]));
                  linkBorder(newborder, temp_border);
                  break border_cycle;
              case 2:
                  if(newborder.isOnlyBegin()) {
                      Border border1 = this.findBorder(type, current_point.getNeighbor(neighbors[0])),
                            border2 = this.findBorder(type, current_point.getNeighbor(neighbors[1]));
                      border1.reverse();
                      linkBorder(border1, newborder);
                      linkBorder(border1, border2);
                      break border_cycle;
                  }
              case 3:
              case 4:
                  this.decreaseBorder(newborder);
                  break border_cycle;
              }
          }  
      }
      
      /** Рисует предположительные границы */
      private void createBorderLines(Border.Type type) {
          for(int i=0; i<this.m; ++i)
          for(int j=0; j<this.n; ++j) {
              if(getType(i, j) != type.getMayBeType())
                  continue;
              IntPoint current_point = new IntPoint(i, j);
              Border newborder = this.addBorder(type, current_point);
              // добавляем в границу точки, пока это возможно:
              this.createBorderLine(type, newborder, current_point);
          }
      }
      
      // определяем линии уровня функций там, где это возможно:
      private void createBorders(Border.Type type) {
          if(options.mustCreateBorders(type)) {
              double koef2 = Utils.sqr(function.getXCoef());
              evaluateBorders(options.getOptions(type), type.getValue(),
                                type.getGradNorm() * function.getXCoef(),
                                type.getDiff2Det() * koef2, type.getDiff2Trace() * koef2,
                                type.getMayBeType());
              createBorderLines(type);               
          }
      }
      
      /** насколько хороша точка для проведения через нее линии */
      private double getPointDifficult(IntPoint pt, Border.Type type) {
          if(this.function.hasArgumentInt(pt) && !this.getType(pt).inLine()) {
              return Math.abs(type.getValue() - function.getValue(pt));
          }
          else
              return Double.POSITIVE_INFINITY;
      }
      
      /** соединяем 2 точки */
      private Border getShortestBorder(IntPoint a, IntPoint b, Border.Type type)
      {
          Border result = new Border(this, a);
          // делаем тупо движение в нужном направлении, пока не упрёмся в b:
          IntPoint current_point = a;
          while(!current_point.equals(b)){
              Vector2 direction = IntPoint.getVector(a, b);
              Pair<IntPoint.Neighbor, IntPoint.Neighbor> neighbors = IntPoint.Neighbor.getNearestDirections(direction);
              IntPoint pt1 = current_point.getNeighbor(neighbors.first),
                      pt2 = current_point.getNeighbor(neighbors.second);
              if(result.canBeAdd(pt1) && (getPointDifficult(pt1, type) < getPointDifficult(pt2, type) || !result.canBeAdd(pt2))) {
                  result.addPixel(pt1);
                  current_point = pt1;
              }
              else if(result.canBeAdd(pt2)) {
                  result.addPixel(pt2);
                  current_point = pt2;
              }
              else // никуда не сдвинуться
                  return null;
          }
          // TODO: может быть, лучше сделать алгоритмом Дейкстры или A*?
          return result;
      }
      
      /** соединяем точку с границей области определения */
      private Border getShortestBorder(IntPoint a, Border.Type type) {
          Border result = null;
          // проверяем 8 направлений: где конец области определения ближе?
          IntPoint.Neighbor n = IntPoint.Neighbor.Right;
          double mindist = Double.POSITIVE_INFINITY;
          for(int i=0; i<8; i++) {
              Border current_border = new Border(this, a);
              IntPoint current_point = a;      
              double dist = 0, 
                     ptdist;
              while(true) {
                  current_point = current_point.getNeighbor(n);
                  ptdist = getPointDifficult(current_point, type);
                  if(!current_border.canBeAdd(current_point))
                      break;
                  current_border.addPixel(current_point);
                  dist += ptdist;
              }
              if(dist < mindist && !this.getType(current_point).inLine()) {
                  current_border.addPixel(this.far_point);
                  result = current_border;
                  mindist = dist;
              }
              n = n.next();
          }
          return result;
      }
      
      private BorderGraph makeGraph(Border.Type type) {
          BorderGraph result = new BorderGraph();
          Set<Border> borders = this.getBordersByType(type);
          Iterator<Border> itr = borders.iterator();
          while(itr.hasNext()) {
              Border border = itr.next();
              result.addVertex(border.getFirst()); // не будет ли совпадающих точек?????????
              result.addVertex(border.getLast());
              result.addEdge(border, border.getFirst(), border.getLast());
          }
          return result;
      }
      
      /** пытаемся соединить точку с каким-нибудь бордером или с краем области определения */
      private void linkPoint(IntPoint pt, Border.Type type, Set<Border> borders) {
       // действуем тупо: сперва пробуем соединить с границей, потом - с другими бордерами
          Border border_to_finish = this.getShortestBorder(pt, type);
          if(border_to_finish != null) {
              this.setType(pt, type.getIThinkType());
              border_to_finish.setType(type);
              borders.add(border_to_finish);
          }
          else {
              Iterator<Border> itr = borders.iterator();;
              while(itr.hasNext()) {
                  Border border2 = itr.next();
                  IntPoint pt21 = border2.getFirst(),
                           pt22 = border2.getLast();
                  Border border_between = this.getShortestBorder(pt, pt21, type);
                  if(border_between != null) {
                      this.setType(pt, type.getIThinkType());
                      this.setType(pt21, type.getIThinkType());
                      border_between.setType(type);
                      borders.add(border_between);
                      break;
                  }
                  else {
                      border_between = this.getShortestBorder(pt, pt22, type);
                      if(border_between != null) {
                          this.setType(pt, type.getIThinkType());
                          this.setType(pt22, type.getIThinkType());
                          border_between.setType(type);
                          borders.add(border_between);
                          break;
                      }
                  }
              }                  
          }                  
      }
      
      /** соединяем границы, чтобы они удовлетворяли условию замкнутости */
      private void linkBorders(Border.Type type) {
          BorderGraph graph = makeGraph(type);
          Set<Border> newborders = new ListOrderedSet<Border>();
          Set<Border> borders = this.getBordersByType(type);
          Iterator<Border> itr = borders.iterator();
          while(itr.hasNext()) {
              Border border1 = itr.next();
              IntPoint pt11 = border1.getFirst(),
                       pt12 = border1.getLast();
              if(!pt11.equals(far_point) && this.getType(pt11) != type.getIThinkType()) 
                  linkPoint(pt11, type, newborders);
              if(!pt12.equals(far_point) && this.getType(pt12) != type.getIThinkType()) 
                  linkPoint(pt12, type, newborders);
          }
          borders.addAll(newborders);
          // TODO: сделать паросочетание минимальной стоимости в графе
      }
            
      public void createBorders() {
          createBorders(Border.Type.Max);
          createBorders(Border.Type.Min);
          createBorders(Border.Type.Nil);
          this.status = Status.BordersRestored; // TODO: проверить, что всё хорошо, иначе Error
          if(status == Status.BordersRestored) {
              // TODO: сделать линковку в цикле, пока они не станут линковаться хорошо
              linkBorders(Border.Type.Nil);
              linkBorders(Border.Type.Max);
              linkBorders(Border.Type.Min);
              this.status = Status.BordersLinked; // TODO: проверить, что всё хорошо, иначе Error        
          }
      }
  }
