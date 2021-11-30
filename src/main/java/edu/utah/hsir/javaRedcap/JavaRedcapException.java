/* --------------------------------------------------------------------------
 * Copyright (C) 2021 University of Utah Health Systems Research & Innovation
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 * Derived from work by The Trustees of Indiana University Copyright (C) 2019 
 * --------------------------------------------------------------------------
 */

package edu.utah.hsir.javaRedcap;


/**
 * This file contains the PHPCapException class.
 */

/**
 * Exception class for JavaRedcap exceptions. This is the exception that
 * JavaRedcap will throw when it encounters an error.
 *
 * Example usage:
 *
 * <pre>
 * <code class="java">
 * try {
 *     Object projectInfo = project->exportProjectInfo();
 * }
 * catch (JavaRedcapException exception) {
 *     print "The following error occurred: {exception->getMessage()}\n";
 *     print "Error code: {exception->getCode()}\n";
 *     connectionErrorNumber = exception->getConnectionErrorNumber();
 *     if (null != connectionErrorNumber) {
 *         print "A connection error occurred.\n";
 *         print "Connection error number: {connectionErrorNumber}\n";
 *     }
 *     print "Stack trace:\n{exception->getTraceAsString()}\n";
 * }
 * </code>
 * </pre>
 *
 * @see http://php.net/manual/en/class.exception.php
 *         Information on additional methods provided by parent class Exception.
 */
public class JavaRedcapException extends Exception
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
     * @param The error message.
     */
    public JavaRedcapException (String message, int code) {
    	super(message);
    	this.code = code;
    }

    /**
     * Constructor.
     *
     * @param The error message.
     */
    public JavaRedcapException (String message, int code, Throwable previousException) {
    	super(message, previousException);
    	this.code = code;
    }

    /**
     * Constructor.
     *
     * @param The error message.
     * @param The connection error number
     * @param The HTTP status code
     * @param The previous exception.
     */
    public JavaRedcapException (
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

    public int getCode()
    {
    	return code;
    }
    
    /**
     * Returns the connection error number, or null if there was no
     * connection error. The possible numbers returned will depend on the
     * type of connection class being used. For example, if cURL is being
     * used, then the cURL error number would be returned.
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
