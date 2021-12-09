/* --------------------------------------------------------------------------
 * Copyright (C) 2021 University of Utah Health Systems Research & Innovation
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 * Derived from work by The Trustees of Indiana University Copyright (C) 2019 
 * --------------------------------------------------------------------------
 */

package edu.utah.hsir.javaRedcap;

/**
 * Interface for error handlers in JavaRedcap
 */
public interface ErrorHandlerInterface extends Cloneable {
	// Error codes

	/** Invalid argument passed to a JavaRedcap method. */
	public static final int INVALID_ARGUMENT = 1;

	/** Too many arguments were passed to the method. */
	public static final int TOO_MANY_ARGUMENTS = 2;

	/** An invalid URL was used. */
	public static final int INVALID_URL = 3;

	/** A CA certificate file was specified, but it could not be found. */
	public static final int CA_CERTIFICATE_FILE_NOT_FOUND = 4;

	/** The CA certificate file could not be read. */
	public static final int CA_CERTIFICATE_FILE_UNREADABLE = 5;

	/** A connection error occurred. */
	public static final int CONNECTION_ERROR = 6;

	/** The REDCap API generated an error. */
	public static final int REDCAP_API_ERROR = 7;

	/** A JSON error occurred. This would typically happen when JavaRedcap is
	 * expecting the REDCap API to return data in JSON format, but the result
	 * returned is not valid JSON.
	 */
	public static final int JSON_ERROR = 8;

	/** The output file could not be found, or was found and could not be written */
	public static final int OUTPUT_FILE_ERROR     = 9;

	/** The input file could not be found. */
	public static final int INPUT_FILE_NOT_FOUND  = 10;

	/** The input file was found, but is unreadable. */
	public static final int INPUT_FILE_UNREADABLE = 11;

	/** The input file contents are invalid. */
	public static final int INPUT_FILE_ERROR      = 12;

	/** An error with the HTTP communications. */
	public static final int COMMUNICATION_ERROR      = 14;

	/**
	 * Constructs and throws an exception for the specified values.
	 * 
	 * @param message Message describing the error that occurred.
	 * @param code The error code.
	 * 
	 * @throws JavaREDCapException An exception representing the values. 
	 */
	public void throwException(String message, int code) throws JavaREDCapException;

	/**
	 * Constructs and throws an exception for the specified values.
	 * 
	 * @param message Message describing the error that occurred.
	 * @param code The error code.
	 * @param previousException A previous exception that is likely the reason this exception is being thrown. May be null.
	 * 
	 * @throws JavaREDCapException An exception representing the values. 
	 */
	public void throwException(String message, int code, Throwable previousException) throws JavaREDCapException;
	
	/**
	 * Constructs and throws an exception for the specified values.
	 * 
	 * @param message Message describing the error that occurred.
	 * @param code The error code.
	 * @param connectionErrorNumber The error number from the underlying
	 * 			connection used, of null if no connection error occurred.<br>
	 * 			For example, if cURL is being used (the default) this will
	 * 			be the cURL error number if a connection error occurs.
	 * @param httpStatusCode HTTP status code, which would typically be set
	 * 			if an error occurs with the HTTP response from the REDCap API.
	 * @param previousException The previous exception that occurred that
	 *     		caused this exception, if any.
	 *     
	 * @throws JavaREDCapException An exception representing the values. 
	 */
	public void throwException(String message, int code, int connectionErrorNumber, int httpStatusCode, Throwable previousException) throws JavaREDCapException;

    /**
     * Create a copy of the object
     * 
     * @return A cloned copy of the error handler object.
     */
	public ErrorHandlerInterface clone();
}
