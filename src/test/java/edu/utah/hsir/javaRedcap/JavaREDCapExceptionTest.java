/* --------------------------------------------------------------------------
 * Copyright (C) 2021 University of Utah Health Systems Research & Innovation
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 * Derived from work by The Trustees of Indiana University Copyright (C) 2019 
 * --------------------------------------------------------------------------
 */

package edu.utah.hsir.javaRedcap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class JavaREDCapExceptionTest
{
	@Test
	public void testInvalidArgument()
    {
        String message = "Argument has wrong type.";
        int code    = ErrorHandlerInterface.INVALID_ARGUMENT;
        
        try {
            throw new JavaREDCapException(message, code);
        } catch (JavaREDCapException exception) {
            assertEquals(exception.getMessage(), message);
            assertEquals(exception.getCode(), code);
            assertNull(exception.getConnectionErrorNumber());
            assertNull(exception.getHttpStatusCode());
            assertNull(exception.getCause());
        }
    }

	@Test
    public void testConnectionError()
    {
        String message = "Unsupported protocol";
        int code    = ErrorHandlerInterface.CONNECTION_ERROR;
        Integer connectionErrorNumber = 1;
        Integer httpCode = 405;

        try {
            throw new JavaREDCapException(message, code, connectionErrorNumber, httpCode, null);
        } catch (JavaREDCapException exception) {
            assertEquals(exception.getMessage(), message);
            assertEquals(exception.getCode(), code);
            assertEquals(exception.getConnectionErrorNumber(), connectionErrorNumber);
            assertEquals(exception.getHttpStatusCode(), httpCode);
            assertNull(exception.getCause());
        }
    }

	@Test
    public void testConnectionError_nullCodes()
    {
        String message = "WithNullCodes";
        int code    = ErrorHandlerInterface.INPUT_FILE_ERROR;

        try {
            throw new JavaREDCapException(message, code, null, null, null);
        } catch (JavaREDCapException exception) {
            assertEquals(exception.getMessage(), message);
            assertEquals(exception.getCode(), code);
            assertNull(exception.getConnectionErrorNumber());
            assertNull(exception.getHttpStatusCode());
            assertNull(exception.getCause());
        }
    }
	
	@Test
    public void testConnectionError_previousException()
    {
        String message = "With Previous Exception";
        int code    = ErrorHandlerInterface.INVALID_URL;
        Exception previousException = new RuntimeException();

        try {
            throw new JavaREDCapException(message, code, previousException);
        } catch (JavaREDCapException exception) {
            assertEquals(exception.getMessage(), message);
            assertEquals(exception.getCode(), code);
            assertNull(exception.getConnectionErrorNumber());
            assertNull(exception.getHttpStatusCode());
            assertSame(previousException, exception.getCause());
        }
    }
	
	@Test
    public void testConnectionError_previousExceptionFull()
    {
        String message = "With Previous Exception";
        int code    = ErrorHandlerInterface.INVALID_URL;
        Integer connectionErrorNumber = -15;
        Integer httpCode = 500;
        Exception previousException = new RuntimeException();

        try {
            throw new JavaREDCapException(message, code, connectionErrorNumber, httpCode, previousException);
        } catch (JavaREDCapException exception) {
            assertEquals(exception.getMessage(), message);
            assertEquals(exception.getCode(), code);
            assertEquals(connectionErrorNumber, exception.getConnectionErrorNumber());
            assertEquals(httpCode, exception.getHttpStatusCode());
            assertSame(previousException, exception.getCause());
        }
    }
}
