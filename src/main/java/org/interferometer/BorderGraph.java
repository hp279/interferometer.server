package org.interferometer;

import org.interferometer.util.IntPoint;

import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class BorderGraph extends UndirectedSparseGraph<IntPoint, Border> {
    IntPoint far_point; // "бесконечно удалённая точка" - с ней соединяются все границы, уходящие за пределы поля
    
	public BorderGraph() {  
        this.far_point = new IntPoint(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}
	
	public void addBorder(Border b)	{
	    IntPoint pt1 = b.getFirst(),
	             pt2 = b.getLast();
	    this.addVertex(pt1);
	    this.addVertex(pt2);
	    this.addEdge(b, pt1, pt2);
	}

	private static final long serialVersionUID = 1L;
	
}