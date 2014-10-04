package org.interferometer;

import java.util.Iterator;
import java.util.LinkedList;

import org.interferometer.BordersInfo;
import org.interferometer.BordersInfo.PixelType;
import org.interferometer.util.IntPoint;
import org.interferometer.util.Pair;

/** Последовательность точек, соединённая в границу.
 * Может быть замкнута в кольцо (тогда считаем, что за последней точкой идёт первая).
 * Класс не отслеживает самопересечения и замыкания в кольцо - за этим следит класс StripsInfo.
  */
public class Border implements Comparable<Border>
{
	public enum Type
	{
		Max,
		Min,
		Nil,
		Empty; // псевдограница - её не существует, она нужна только для поиска
		
		public double getValue() {
		    switch(this) {
		    case Max:
		        return 1;
		    case Min:
		        return -1;
		    case Nil: 
		        return 0;
		    case Empty:
		    default:
		        return Double.NaN;
		    }
		}
		
	      public double getGradNorm() {
	            switch(this) {
	            case Max:
	                return 0;
	            case Min:
	                return 0;
	            case Nil: 
	                return 1;
	            case Empty:
	            default:
	                return Double.NaN;
	            }
	        }	      
	      
	       public double getDiff2Det() {
	           return 0;
           }
	       
	       public double getDiff2Trace() {
	                switch(this) {
	                case Max:
	                    return -1;
	                case Min:
	                    return 1;
	                case Nil: 
	                    return 0;
	                case Empty:
	                default:
	                    return Double.NaN;
	                }
	            }
	
		public BordersInfo.PixelType getMayBeType()	{
			switch(this) {
			case Max: 
				return PixelType.MayBeMax;
			case Min:
				return PixelType.MayBeMin;
			case Nil:
				return PixelType.MayBeNil;
			default:
				return null;
			}
		}
		public BordersInfo.PixelType getPossibleType() {
			switch(this) {
			case Max: 
				return PixelType.PossibleMax;
			case Min:
				return PixelType.PossibleMin;
			case Nil:
				return PixelType.PossibleNil;
			default:
				return null;
			}
		}
		public BordersInfo.PixelType getPossibleBeginType() {
			switch(this) {
			case Max: 
				return PixelType.PossibleMaxAndBegin;
			case Min:
				return PixelType.PossibleMinAndBegin;
			case Nil:
				return PixelType.PossibleNilAndBegin;
			default:
				return null;
			}
		}
		public BordersInfo.PixelType getIThinkType() {
            switch(this) {
            case Max: 
                return PixelType.IThinkMax;
            case Min:
                return PixelType.IThinkMin;
            case Nil:
                return PixelType.IThinkNil;
            default:
                return null;
            }
        }
	}
	Type type;
	
	public static class EdgePoint extends Pair<IntPoint, Boolean> 
	                       implements Comparable<EdgePoint> {
        public EdgePoint(IntPoint first, Boolean second) {
            super(first, second);
        }
        
        @Override
        public int compareTo(EdgePoint o) { // true идёт перед false
            return first.compareTo(o.first) * 4 - Boolean.compare(second, o.second);
        }
	}
	
	LinkedList<IntPoint> pixels;
	BordersInfo field;
	boolean is_ring, // замкнут в кольцо
 	        is_final; // определен окончательно
	
	public Border(BordersInfo field, Type type, IntPoint pt) {
		this.field = field;
		this.type = type;
		this.pixels = new LinkedList<IntPoint>();
		this.pixels.addLast(pt);
		if(this.type != Type.Empty)
			this.field.setType(pt, type.getPossibleBeginType());
		this.is_ring = false;
		this.is_final = false;
	}
	public Border(BordersInfo field, IntPoint pt)	{
		this(field, Type.Empty, pt);
	}
	
	public void fullField() {
	    if(this.field != null && type != Type.Empty) {
	        Iterator<IntPoint> itr=pixels.iterator();
	        if(is_final) {
	            while(itr.hasNext())
                    this.field.setType(itr.next(), type.getIThinkType());
	        }
	        else {
	             this.field.setType(itr.next(), type.getPossibleBeginType());
	             while(itr.hasNext())
	                 this.field.setType(itr.next(), type.getPossibleType());
	        }
	    }
	}
	
	public Type getType() {
		return type;
	}
	
	public void setType(Type type) {
	    this.type = type;
	    fullField();
	}
	
	public boolean isEmpty() {
		return this.pixels.isEmpty();
	}
	
	public boolean isOnlyBegin() {
		return this.pixels.size() == 1;
	}
	
	public boolean hasBegin(IntPoint pt) {
		return this.pixels.getFirst().equals(pt);
	}
	
	public IntPoint getFirst() {
		return this.pixels.getFirst();
	}
	public IntPoint getLast() {
		return this.pixels.getLast();
	}
	public IntPoint getPrevLast() {
		if(this.pixels.size() >= 2)	{
			Iterator<IntPoint> iterator = this.pixels.descendingIterator();
			iterator.next();
			return iterator.next();
		}
		else
			return null;
	}
	
	public EdgePoint getFirstVertex() {
	    return new EdgePoint(getFirst(), true);
	}
	public EdgePoint getLastVertex() {
        return new EdgePoint(getLast(), false);
    }

	public void setRing() {
		this.is_ring = true;
	}
	public void setFinal() {
	    this.is_final = true;
	    fullField();
	}
	
	/** Длина с точки зрения поля */
	public double getLength() {
	    double result = 0;
	    for(IntPoint pt: this.pixels) {
	        result += this.field.getPointDifficult(pt, this.type);
	    }
	    return result;
	}
	
	/** Точку можно добавить так, чтобы она не пересекла другую границу */
	public boolean canBeAdd(IntPoint pt) {
	    IntPoint last_pt = this.getLast(),
	             left_pt = pt.getNeighbor(IntPoint.Neighbor.Left),
	             up_pt = pt.getNeighbor(IntPoint.Neighbor.Up),
	             right_pt = pt.getNeighbor(IntPoint.Neighbor.Right),
	             down_pt = pt.getNeighbor(IntPoint.Neighbor.Down);
	    return this.field.hasArgument(pt) && !this.field.getType(pt).inLine() &&
	           (!this.field.hasArgument(left_pt) || left_pt.equals(last_pt) || !this.field.getType(left_pt).inLine()) &&
	           (!this.field.hasArgument(up_pt) || up_pt.equals(last_pt) || !this.field.getType(up_pt).inLine()) &&
	           (!this.field.hasArgument(right_pt) || right_pt.equals(last_pt) || !this.field.getType(right_pt).inLine()) &&
	           (!this.field.hasArgument(down_pt) ||  down_pt.equals(last_pt) ||!this.field.getType(down_pt).inLine());
	}
	
	public void addPixel(IntPoint pt) {
//		System.out.printf("\n add pixel: (%d, %d)", pt.getX(), pt.getY());
		this.pixels.addLast(pt);
		this.is_ring = false;
		if(this.field != null && this.type != Type.Empty)
			this.field.setType(pt, is_final? type.getIThinkType() : type.getPossibleType());
	}
	
	public void removePixel() {
		if(this.field != null && this.type != Type.Empty)
			this.field.setType(this.pixels.getLast(), PixelType.Nothing);
		this.pixels.removeLast();
		this.is_ring = false;
	}
	
	public void addBeginPixel(IntPoint pt) {
		if(this.field != null && this.type != Type.Empty) {
			this.field.setType(this.pixels.getFirst(), is_final? type.getIThinkType() : type.getPossibleType());
			this.field.setType(pt, is_final? type.getIThinkType() : type.getPossibleBeginType());
		}
		this.pixels.addFirst(pt);
		this.is_ring = false;
	}
	
    public void reverse() {
        LinkedList<IntPoint> newpixels = new LinkedList<IntPoint>();
        Iterator<IntPoint> desc = this.pixels.descendingIterator();
        while(desc.hasNext())
            newpixels.addLast(desc.next());
        this.pixels = newpixels;
        if(this.field != null && this.type != Type.Empty) {
            field.setType(pixels.getLast(), is_final? type.getIThinkType() : type.getPossibleType());
            field.setType(pixels.getFirst(), is_final? type.getIThinkType() : type.getPossibleBeginType());
        }
    }
    /** Устанавливает правильное направление, исходя из начальной или конечной точки */
    public Border setOrientation(IntPoint firstpt, IntPoint lastpt) {
        if((firstpt != null && this.getFirst() != firstpt) || (lastpt != null && this.getLast() != lastpt) )
            reverse();
        return this;
    }
	
	/** link_point - имеют ли две кривые общую крайнюю точку */
	public void addBegin(Border b, boolean link_point) {
		if(this.field != null && this.type != Type.Empty)
			field.setType(pixels.getFirst(), is_final? type.getIThinkType() : type.getPossibleType());
		if(link_point) {
		    b.setOrientation(null, pixels.getFirst());
            b.pixels.removeLast();		    
		}
		b.pixels.addAll(this.pixels);
		this.pixels = b.pixels;
		this.is_ring = false;
	}
	/** link_point - имеют ли две кривые общую крайнюю точку */
	public void addEnd(Border b, boolean link_point) {
//											b.pixels.getFirst();
//											type.getPossibleType();
		if(this.field != null && this.type != Type.Empty)
			this.field.setType(b.pixels.getFirst(), is_final? type.getIThinkType() : type.getPossibleType());
		if(link_point) {
		    b.setOrientation(pixels.getLast(), null);
            b.pixels.removeFirst();		    
		}
		this.pixels.addAll(b.pixels);
		this.is_ring = false;
	}
	
	public boolean equals(Border arg) {
		return pixels.isEmpty()? arg.pixels.isEmpty() :
			 					(!arg.pixels.isEmpty() && pixels.getFirst().equals( arg.pixels.getFirst() ));
	}
	public boolean equals(Object o) {
		return equals((Border)o);
	}
	@Override
	public int compareTo(Border arg0) {
		return pixels.isEmpty()? (arg0.pixels.isEmpty()? 0 : -1) :
								 (arg0.pixels.isEmpty()? 1 : pixels.getFirst().compareTo( arg0.pixels.getFirst() ));
	}
}