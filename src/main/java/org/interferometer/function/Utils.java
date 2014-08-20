package org.interferometer.function;

public class Utils
{
	 /** Ближайшее в большую сторону */
	 public static double nextValue(double value)
	 {
		 return (value > 0? value * (1+1e-15) :
				 (value < 0? value * (1-1e-15) :
					Double.MIN_VALUE ));
	 }
	 
	 /** Ближайшее в меньшую сторону */
	 public static double prevValue(double value)
	 {
		 return (value > 0? value * (1-1e-15) :
				 (value < 0? value * (1+1e-15) :
					-Double.MIN_VALUE ));
	 }

}