/* --------------------------------------------------------------------------
 * Copyright (C) 2021 University of Utah Health Systems Research & Innovation
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 * Derived from work by The Trustees of Indiana University Copyright (C) 2019 
 * --------------------------------------------------------------------------
 */

package edu.utah.hsir.javaRedcap;


/**
 * Exception class for JavaRedcap exceptions. This is the exception that
 * JavaRedcap will throw when it encounters an error.
 *
 * Example usage:
 *
 * <pre>
 * <code class="java">
 * try {
 *     Object projectInfo = project.exportProjectInfo();
 * }
 * catch (JavaRedcapException exception) {
 *     print "The following error occurred: {exception.getMessage()}\n";
 *     print "Error code: {exception.getCode()}\n";
 *     connectionErrorNumber = exception.getConnectionErrorNumber();
 *     if (null != connectionErrorNumber) {
 *         print "A connection error occurred.\n";
 *         print "Connection error number: {connectionErrorNumber}\n";
 *     }
 *     print "Stack trace:\n{exception.getTraceAsString()}\n";
 * }
 * </code>
 * </pre>
 */
public class JavaREDCapException extends Exception
{
	private static final long serialVersionUID = 3996085084391681356L;

	private int code;

	/** Connection error number */  
	private Integer connectionErrorNumber;
	
    /** HTTP status code */
    private Integer httpStatusCode;
    
    
    /**
     * Constructor.
     *
     * @param message The error message
     * @param code The error code
     */
    public JavaREDCapException (String message, int code) {
    	super(message);
    	this.code = code;
    }

    /**
     * Constructor.
     *
     * @param message The error message.
     * @param code The error code
     * @param previousException A previous exception
     */
    public JavaREDCapException (String message, int code, Throwable previousException) {
    	super(message, previousException);
    	this.code = code;
    }

    /**
     * Constructor.
     *
     * @param message The error message.
     * @param code The connection error number
     * @param connectionErrorNumber The connection error number if available.
     * @param httpStatusCode The HTTP status code if available.
     * @param previousException The previous exception. Can be null if there is no previous exception.
     */
    public JavaREDCapException (
    		String message,
    		int code,
    		Integer connectionErrorNumber,
    		Integer httpStatusCode,
    		Throwable previousException
    		) {
    	super (message, previousException);
    	this.code = code;
    	this.connectionErrorNumber = connectionErrorNumber;
    	this.httpStatusCode = httpStatusCode;
    }

    /**
     * Returns the error code for the exception.
     * 
     * @return The error code
     */
    public int getCode()
    {
    	return code;
    }
    
    /**
     * Returns the connection error number, or null if there was no
     * connection error. The possible numbers returned will depend on the
     * type of connection class being used.
     *
     * @return Connection error number, or null if there was no connection error.
     */
    public Integer getConnectionErrorNumber()
    {
        return connectionErrorNumber;
    }
    

    /**
     * Returns the HTTP status code, or null if this was not set.
     *
     * @return HTTP status code, or null if this was not set.
     */
    public Integer getHttpStatusCode()
    {
        return httpStatusCode;
    }
}
