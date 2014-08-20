package org.interferometer.set;

import java.util.TreeSet;

public class ObjectWithProperties
{
	TreeSet<String> properties;
	
	public ObjectWithProperties()
	{
		properties = new TreeSet<String>();
	}
	
	public void addProperty(String str)
	{
		properties.add(str);
	}
	
	public boolean hasProperty(String s)
	{
		return properties.contains(s);
	}
}