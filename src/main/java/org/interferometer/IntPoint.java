package org.interferometer;

import org.interferometer.linear.Vector2;

	public class IntPoint implements Comparable<IntPoint>
	{
		int x;
		int y;
		
		public enum Neighbor
		{
			// TODO: подумать, включать ли в число соседей саму точку
			Right(0),
			RightUp(1),
			Up(2),
			LeftUp(3),
			Left(4),
			LeftDown(5),
			Down(6),
			RightDown(7);
			
			static Neighbor neighbors[] = new Neighbor[8];
			static
			{
				neighbors[0] = Right;
				neighbors[1] = RightUp;
				neighbors[2] = Up;
				neighbors[3] = LeftUp;
				neighbors[4] = Left;
				neighbors[5] = LeftDown;
				neighbors[6] = Down;
				neighbors[7] = RightDown;				
			}
			
			private byte myByte;

		    Neighbor(final int aByte) {
		        myByte = (byte) aByte;
		    }
		    
		    /** Близкие соседи - по горизонтали и вертикали */
		    public boolean isNear()
		    {
		    	return myByte % 2 == 0;
		    }
		    /** Дальние соседи - по диагонали */
		    public boolean isFar()
		    {
		    	return myByte % 2 == 1;
		    }
		    
		    public Neighbor next()
		    {
		    	return neighbors[(myByte + 1) % 8];
		    }
		    public Neighbor prev()
		    {
		    	return neighbors[(myByte - 1) % 8];
		    }
		    public Neighbor rotate90Clockwise()
		    {
		    	return neighbors[(myByte - 2) % 8];
		    }
		    public Neighbor rotate90CounterClockwise()
		    {
		    	return neighbors[(myByte + 2) % 8];
		    }
		    public Neighbor reverse()
		    {
		    	return neighbors[(myByte + 4) % 8];
		    }
		    
		    public Vector2 getVector()
		    {
		    	switch(this)
		    	{
		    	case Right: return new Vector2(1, 0);
				case RightUp: return new Vector2(1, 1);
				case Up: return new Vector2(0, 1);
				case LeftUp: return new Vector2(-1, 1);
				case Left: return new Vector2(-1, 0);
				case LeftDown: return new Vector2(-1, -1);
				case Down: return new Vector2(0, -1);
				case RightDown: return new Vector2(1, -1);
				default: return null;
		    	}
		    }
		}
		
		public IntPoint(int x, int y)
		{
			this.x = x;
			this.y = y;
		}
		public int getX()
		{
			return x;
		}
		public int getY()
		{
			return y;
		}
		
		static public IntPoint getNeighbor(int x, int y, Neighbor n)
		{
			switch(n)
			{
			case Right: return new IntPoint(x+1, y);
			case RightUp: return new IntPoint(x+1, y+1);
			case Up: return new IntPoint(x, y+1);
			case LeftUp: return new IntPoint(x-1, y+1);
			case Left: return new IntPoint(x-1, y);
			case LeftDown: return new IntPoint(x-1, y-1);
			case Down: return new IntPoint(x, y-1);
			case RightDown: return new IntPoint(x+1, y-1);
			default: return null;
			}
		}
		public IntPoint getNeighbor(Neighbor n)
		{
			return getNeighbor(getX(), getY(), n);
		}
		
		public <T> T getValue(T array[][])
		{
			return array[ x ][ y ];
		}
		public <T> T getNeighborValue(T array[][], Neighbor n)
		{
			switch(n)
			{
			case Right: return array[x+1][y];
			case RightUp: return array[x+1][y+1];
			case Up: return array[x][y+1];
			case LeftUp: return array[x-1][y+1];
			case Left: return array[x-1][y];
			case LeftDown: return array[x-1][y-1];
			case Down: return array[x][y-1];
			case RightDown: return array[x+1][y-1];
			default: return null;
			}

		}
		
		/** Считает количество ближних соседей, которые равны value, и записывает в values */
		private <T> int getNearNeighborsCount(T array[][], T value, Neighbor values[], int from)
		{
			if(x+1 < array.length && array[x+1][y].equals(value))
				values[from++] = Neighbor.Right;
			if(y+1 < array[x].length && array[x][y+1].equals(value))
				values[from++] = Neighbor.Up;
			if(x > 0 && array[x-1][y].equals(value))
				values[from++] = Neighbor.Left;
			if(y > 0 && array[x][y-1].equals(value))
				values[from++] = Neighbor.Down;
			return from;
		}
		/** Считает количество ближних соседей, которые равны value, и записывает в values */
		public <T> int getNearNeighborsCount(T array[][], T value, Neighbor values[])
		{
			return getNearNeighborsCount(array, value, values, 0);
		}

		/** Считает количество дальних соседей, которые равны value, и записывает в values  */
		private <T> int getFarNeighborsCount(T array[][], T value, Neighbor values[], int from)
		{
			if(x+1 < array.length && y+1 < array[x+1].length && array[x+1][y+1].equals(value))
				values[from++] = Neighbor.RightUp;
			if(x > 0 && y+1 < array[x-1].length && array[x-1][y+1].equals(value))
				values[from++] = Neighbor.LeftUp;
			if(x > 0 && y > 0 && array[x-1][y-1].equals(value))
				values[from++] = Neighbor.LeftDown;
			if(x+1 < array.length && y > 0 && array[x+1][y-1].equals(value))
				values[from++] = Neighbor.RightDown;
			return from;
		}
		/** Считает количество дальних соседей, которые равны value, и записывает в values  */
		public <T> int getFarNeighborsCount(T array[][], T value, Neighbor values[])
		{
			return getFarNeighborsCount(array, value, values, 0);
		}
		
		/** Считает количество соседей, которые равны value, и записывает в values последнего */
		public <T> int getNeighborsCount(T array[][], T value, Neighbor values[])
		{
			int result_near = getNearNeighborsCount(array, value, values, 0);
			return getNearNeighborsCount(array, value, values, result_near);
		}

		public boolean equals(IntPoint p)
		{
			return x == p.x && y == p.y;
		}
		public int compareTo(IntPoint p) {
			return x > p.x? 
					1:
					x < p.x?
					-1:
						y > p.y? 
							1:
						y < p.y?
							-1 : 0;
		}
	}
