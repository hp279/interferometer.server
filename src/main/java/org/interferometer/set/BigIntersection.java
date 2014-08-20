package org.interferometer.set;

import java.util.Iterator;
import java.util.LinkedList;

public class BigIntersection extends FlatSet
{
	LinkedList<FlatSet> A;
	
	public BigIntersection()
	{
		this.A = new LinkedList<FlatSet>();
	}
	
	public void add(FlatSet a)
	{
		this.A.add(a);
	}
	
	public boolean has(double x, double y)
	{
		for(Iterator<FlatSet> i = A.iterator(); i.hasNext();)
			if(!i.next().has(x, y))
				return false;
		return true;
	}
}