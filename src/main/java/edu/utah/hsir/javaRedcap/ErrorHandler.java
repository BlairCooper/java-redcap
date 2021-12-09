/* --------------------------------------------------------------------------
 * Copyright (C) 2021 University of Utah Health Systems Research & Innovation
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 * Derived from work by The Trustees of Indiana University Copyright (C) 2019 
 * --------------------------------------------------------------------------
 */

package edu.utah.hsir.javaRedcap;


/**
 * Default error handler for redcapJava. redcapJava will call the
 * throwException method of this class when an error occurs.
 */
public class ErrorHandler
	implements ErrorHandlerInterface
{

	@Override
	public void throwException(String message, int code) throws JavaREDCapException {
		throw new JavaREDCapException(message, code);
	}

	@Override
	public void throwException(String message, int code, Throwable previousException) throws JavaREDCapException {
		throw new JavaREDCapException(message, code, previousException);
	}

	@Override
	public void throwException(String message, int code, int connectionErrorNumber, int httpStatusCode,
			Throwable previousException) throws JavaREDCapException {
		throw new JavaREDCapException(message, code, connectionErrorNumber, httpStatusCode, previousException);
	}
	
	@Override
	public ErrorHandlerInterface clone() {
		ErrorHandler clone = null;

		try {
			clone = (ErrorHandler)super.clone();
		} catch (CloneNotSupportedException e) {
			clone = new ErrorHandler();
		}

		return clone;
	}
}
