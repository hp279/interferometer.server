package org.interferometer;

	public class Pixel implements Comparable<Pixel>
	{
		public int x;
		public int y;

		public int compareTo(Pixel p) {
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
