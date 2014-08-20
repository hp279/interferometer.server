package org.interferometer;

import java.util.LinkedList;

public class Border
{
	LinkedList<Pixel> pixels;
	
	public enum Type
	{
		Max,
		Min,
		Undefined;
	}
	
	private Type type;
	
	public Border(final Type type)
	{
		this.type = type;
		this.pixels = new LinkedList<Pixel>();
	}
	public Border()
	{
		this(Type.Undefined);
	}
}