package org.interferometer.function;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

public abstract class AbstractFunction
{
	  public enum State
	  {
		  Error(-1),
		  NotDefined (0),
		  Defined (1);
		  
		  private byte myByte;
		  
		  State(final int aByte)
		  {
			  myByte = (byte)aByte;
		  }
	  }
	  
	  protected State state;
	  
	  protected AbstractFunction()
	  {
		  state = State.Defined;
	  }
	  protected void setDefined()
	  {
		  state = State.Defined;
	  }
	  protected void setNotDefined()
	  {
		  state = State.NotDefined;
	  }
	  protected void setError()
	  {
		  state = State.Error;
	  }
	  
	  public void read(DataInputStream in)
	  {
		  read(new Scanner(in)); 
	  } 
	  
	  public void read(Scanner s) // кому надо будет - переопределит
	  // главное - не забыть поставить setDefined или setError
	  {
	  }
	  
	  public void write(DataOutputStream out)
	  {
		  if(state != State.Defined)
			  return;
		  write(new PrintStream(out)); 
	  }
	  
	  public void write(PrintStream out) // кому надо будет - переопределит
	  {
	  }
}