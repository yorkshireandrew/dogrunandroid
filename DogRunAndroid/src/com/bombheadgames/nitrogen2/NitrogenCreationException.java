package com.bombheadgames.nitrogen2;

/** Exception that is thrown if the nitrogen package is unable to create a component */
public class NitrogenCreationException extends Exception{
	private static final long serialVersionUID = 1L;
	
	NitrogenCreationException(){super();}
	NitrogenCreationException(String s){super(s);}
}
