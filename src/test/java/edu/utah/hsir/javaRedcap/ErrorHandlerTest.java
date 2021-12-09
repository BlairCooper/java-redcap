/* --------------------------------------------------------------------------
 * Copyright (C) 2021 University of Utah Health Systems Research & Innovation
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 * Derived from work by The Trustees of Indiana University Copyright (C) 2019 
 * --------------------------------------------------------------------------
 */

package edu.utah.hsir.javaRedcap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class ErrorHandlerTest
{

	@Test
	public void testCtor()
	{
		ErrorHandler handler = new ErrorHandler();
		
		assertNotNull(handler);
		assertTrue(handler instanceof ErrorHandlerInterface);
	}
	
	@Test
	public void testThrowException_withCode()
	{
		String testMsg = "Testing Throw with code";
		int testCode = ErrorHandlerInterface.JSON_ERROR;

		ErrorHandler handler = new ErrorHandler();

		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			handler.throwException(testMsg, testCode);
		});
		
		assertEquals(testMsg, exception.getMessage());
		assertEquals(testCode, exception.getCode());
	}

	@Test
	public void testThrowException_withPrevious()
	{
		String testMsg = "Testing Throw with code";
		int testCode = ErrorHandlerInterface.JSON_ERROR;
		Exception testException = new RuntimeException();

		ErrorHandler handler = new ErrorHandler();

		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			handler.throwException(testMsg, testCode, testException);
		});
		
		assertEquals(testMsg, exception.getMessage());
		assertEquals(testCode, exception.getCode());
		assertSame(testException, exception.getCause());
	}

	@Test
	public void testThrowException_withCodesAndPrevious()
	{
		String testMsg = "Testing Throw with code";
		int testCode = ErrorHandlerInterface.JSON_ERROR;
		Integer testConnCode = -127;
		Integer testHttpCode = 201;
		Exception testException = new RuntimeException();

		ErrorHandler handler = new ErrorHandler();

		JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
			handler.throwException(testMsg, testCode, testConnCode, testHttpCode, testException);
		});
		
		assertEquals(testMsg, exception.getMessage());
		assertEquals(testCode, exception.getCode());
		assertEquals(testConnCode, exception.getConnectionErrorNumber());
		assertEquals(testHttpCode, exception.getHttpStatusCode());
		assertSame(testException, exception.getCause());
	}
	
	@Test
	public void testClone() throws CloneNotSupportedException
	{
		ErrorHandler orgHandler = new ErrorHandler();
		Object cloneHandler = orgHandler.clone();
		
		assertNotNull(cloneHandler);
		assertTrue(cloneHandler instanceof ErrorHandler);
	}
	
	@Test
	public void testCustomImpl() {
		/**
		 * This test is a bit of a noop. It's purpose to to ensure that a custom
		 * ErrorHandler can be created without a compile error.
		 */
		ErrorHandlerInterface handler = new ErrorHandlerInterface() {
			@Override
			public void throwException(String message, int code) throws JavaREDCapException {
			}

			@Override
			public void throwException(String message, int code, Throwable previousException)
					throws JavaREDCapException {
			}

			@Override
			public void throwException(String message, int code, int connectionErrorNumber, int httpStatusCode,
					Throwable previousException) throws JavaREDCapException {
			}
			
			@Override
			public ErrorHandlerInterface clone() {
				ErrorHandlerInterface clone = null;

				try {
					clone = (ErrorHandlerInterface) super.clone();
				} catch (CloneNotSupportedException e) {
				}
				
				return clone;
			}
		};
		
		assertNotNull(handler);
	}

}
