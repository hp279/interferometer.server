package org.interferometer.set;

import org.interferometer.linear.Vector2;

public abstract class FlatSet extends ObjectWithProperties {
    
	public abstract boolean has(double x, double y);
    
    public boolean has(Vector2 vector) {
    	return has(vector.get1(), vector.get2());
    }
}