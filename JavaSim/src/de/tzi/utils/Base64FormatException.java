package de.tzi.utils;
/*
 * Copyright (C) World Wide Web Consortium, (Massachusetts Institute of Technology, 
 * Institut National de Recherche en Informatique et en Automatique, Keio University).
 * All Rights Reserved. http://www.w3.org/Consortium/Legal/
 */

/**
 * Exception for invalid BASE64 streams.
 */
public class Base64FormatException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4821262831486609896L;

	/**
	 * Create that kind of exception
	 * @param msg The associated error message 
	 */
	public Base64FormatException(String msg)
	{
		super(msg);
	}

}
