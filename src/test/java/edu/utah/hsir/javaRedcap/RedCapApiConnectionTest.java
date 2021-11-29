/* --------------------------------------------------------------------------
 * Copyright (C) 2021 University of Utah Health Systems Research & Innovation
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 * Derived from work by The Trustees of Indiana University Copyright (C) 2019 
 * --------------------------------------------------------------------------
 */

package edu.utah.hsir.javaRedcap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import edu.utah.hsir.javaRedcap.enums.RedCapApiContent;
import edu.utah.hsir.javaRedcap.enums.RedCapApiFormat;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public class RedCapApiConnectionTest
{
	private static final String TEST_URL = "http://host.somedomain.org/an-end-point/";

    private File testCACertificateFile;

	private MockWebServer mockWebServer;
	private RedCapApiConnection testConnection;


	@Before
	public void setupTest() throws JavaRedcapException, IOException {
	    mockWebServer = new MockWebServer();
		testConnection = new RedCapApiConnection(mockWebServer.url("/").toString(), false, (String)null);

	    testCACertificateFile = File.createTempFile("redcap", null);
	    testCACertificateFile.deleteOnExit();
	}

	@Test
    public void testConnectionCreation() throws IOException
    {
		boolean sslVerify = true;
        ErrorHandler errorHandler = new ErrorHandler();
        
		try {
	        RedCapApiConnection connection = new RedCapApiConnection(
			    TEST_URL,
			    sslVerify,
			    testCACertificateFile.getAbsolutePath(),
			    errorHandler
			);

			assertEquals("URL check failed", TEST_URL, connection.getUrl());
	        assertEquals("SSL Verify check failed", sslVerify, connection.getSslVerify());
	        assertEquals("CA certificate file check failed", testCACertificateFile.getAbsolutePath(), connection.getCaCertificateFile());
	        assertSame("Error handler check failed", errorHandler, connection.getErrorHandler());
		} catch (JavaRedcapException e) {
			fail(e.getMessage());
		}
        
    }

	@Test
    public void testCall() throws JavaRedcapException
    {
        String expectedResponse = "call test";
        
        mockWebServer.enqueue(new MockResponse()
        	      .addHeader("Content-Type", "text/plain; charset=utf-8")
        	      .setBody(expectedResponse)
        	      .setResponseCode(200));
        	         	 
        RedCapApiParams data = new RedCapApiParams("ABCDEF1234567890ABCDEF1234567890", RedCapApiContent.RECORD, null);
        data.setData("string data");
        
        String response = testConnection.call(data);
   
        assertEquals("Response check failed", expectedResponse, response);
    }

	@Test
    public void testCallWithMap()
    {
        String expectedResponse = "call test";
        
        mockWebServer.enqueue(new MockResponse()
        	      .addHeader("Content-Type", "text/plain; charset=utf-8")
        	      .setBody(expectedResponse)
        	      .setResponseCode(200));
        
        Map<String, Object> data = new HashMap<String, Object>();
        
        JavaRedcapException exception;
        
        exception = assertThrows(JavaRedcapException.class, () -> {
        	testConnection.callWithMap(data);
        });
        
		MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsString("missing"));
		assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());

		data.put(RedCapApiParams.TOKEN, "1234567890ABCDEF1234567890ABCDEF");

		exception = assertThrows(JavaRedcapException.class, () -> {
            testConnection.callWithMap(data);
        });
        
		MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsString("missing"));
		assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
		
		data.put(RedCapApiParams.CONTENT, RedCapApiContent.EVENT);

		exception = assertThrows(JavaRedcapException.class, () -> {
            testConnection.callWithMap(data);
        });
        
		MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsString("missing"));
		assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
		
		data.put(RedCapApiParams.FORMAT, RedCapApiFormat.JSON);

		try {
			testConnection.callWithMap(data);
		}
		catch (JavaRedcapException e) {
			fail("JavaRedcapException should not have been thrown: " + e.getMessage());
		}
        
        
    }

	@Test
    public void testConnectionSetErrorHandler()
    {
        ErrorHandlerInterface errorHandler = new ErrorHandler();
        ErrorHandlerInterface errorHandlerFromGet = testConnection.getErrorHandler();

        assertNotSame("Initial ErrorHandler state unexpected", errorHandler, errorHandlerFromGet);
        
        testConnection.setErrorHandler(errorHandler);
        errorHandlerFromGet = testConnection.getErrorHandler();

        assertSame("ErrorHandler set/get failure", errorHandler, errorHandlerFromGet);

        testConnection.setErrorHandler(null);
        errorHandlerFromGet = testConnection.getErrorHandler();

        assertNotNull("ErrorHandler set/get failure", errorHandlerFromGet);
    }

	@Test
    public void testSetUrl() throws JavaRedcapException
    {
		// Need to use a local connection to bypass mock web server
		RedCapApiConnection localConn = new RedCapApiConnection(TEST_URL, false, (String)null);

		assertEquals("Initial URL state unexpected", TEST_URL, localConn.getUrl());
        
        String newUrl = "https://redcap.somewhere.edu/api/";
        localConn.setUrl(newUrl);
        
        assertEquals("Url set/get failure",newUrl, localConn.getUrl());
        
        JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			localConn.setUrl(null);
		});
		
		MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsString("URL"));
		assertEquals(ErrorHandlerInterface.INVALID_URL, exception.getCode());

		exception = assertThrows(JavaRedcapException.class, () -> {
			localConn.setUrl("");
		});
		
		MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsString("URL"));
		assertEquals(ErrorHandlerInterface.INVALID_URL, exception.getCode());
    }

	@Test
    public void testSetSslVerify() throws JavaRedcapException
    {
		assertFalse("Initial SSLVerify state unexpected", testConnection.getSslVerify());
		
        testConnection.setSslVerify(true);

        assertTrue("SSLVerify set/get failure", testConnection.getSslVerify());
    }

	@Test
    public void testSetCaCertificateFile() throws JavaRedcapException
    {
		assertNull("Initial caCertificate state unexpected", testConnection.getCaCertificateFile());
        
		
        testConnection.setCaCertificateFile(testCACertificateFile.getAbsolutePath());

        assertEquals("CA certificate file set/get failure", testCACertificateFile.getAbsolutePath(), testConnection.getCaCertificateFile());

        testConnection.setCaCertificateFile(null);

        assertNull("CA certificate file set/get failure", testConnection.getCaCertificateFile());
        
        JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
        	testConnection.setCaCertificateFile("  ");        	
        });

		MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsString("not defined"));
		assertEquals(ErrorHandlerInterface.CA_CERTIFICATE_FILE_NOT_FOUND, exception.getCode());
    }

	@Test
    public void testSetTimeoutInSeconds() throws JavaRedcapException
    {
        assertEquals(
        		"Initial timeout state unexpected",
        		RedCapApiConnection.DEFAULT_TIMEOUT_IN_SECONDS,
        		testConnection.getTimeoutInSeconds()
        		);
        
        int timeout = RedCapApiConnection.DEFAULT_TIMEOUT_IN_SECONDS + 600;
        testConnection.setTimeoutInSeconds(timeout);

        assertEquals("Timeout set/get failure", timeout, testConnection.getTimeoutInSeconds());
        
        JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testConnection.setTimeoutInSeconds(0);
		});
		
		MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsString("Timeout"));
		assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());

		exception = assertThrows(JavaRedcapException.class, () -> {
			testConnection.setTimeoutInSeconds(-10);
		});
		
		MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsString("Timeout"));
		assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    }

	@Test
    public void testSetConnectionTimeoutInSeconds() throws JavaRedcapException
    {
		assertEquals(
				"Initial connection timeout state unexpected",
				RedCapApiConnection.DEFAULT_CONNECTION_TIMEOUT_IN_SECONDS,
				testConnection.getConnectionTimeoutInSeconds()
				);
        
        int timeout = RedCapApiConnection.DEFAULT_CONNECTION_TIMEOUT_IN_SECONDS + 120;
        testConnection.setConnectionTimeoutInSeconds(timeout);

        assertEquals("Connection Timeout set/get failure", timeout, testConnection.getConnectionTimeoutInSeconds());
        
        JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testConnection.setConnectionTimeoutInSeconds(0);
		});
		
		MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsString("Timeout"));
		assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());

		exception = assertThrows(JavaRedcapException.class, () -> {
			testConnection.setConnectionTimeoutInSeconds(-10);
		});
		
		MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsString("Timeout"));
		assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    }

	@Test
    public void testCaCertificateFileNotFound() throws IOException
    {
		String testFile = testCACertificateFile.getAbsolutePath();
		testCACertificateFile.delete();

		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testConnection.setCaCertificateFile(testFile);
		});

		MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsString("does not exist"));
		assertEquals(ErrorHandlerInterface.CA_CERTIFICATE_FILE_NOT_FOUND, exception.getCode());
    }

	@Test
    public void testCaCertificateFileUnreadable() throws IOException
    {
		// Skip the test on Windows
		Assume.assumeFalse(System.getProperty("os.name").toLowerCase().startsWith("win"));

		testCACertificateFile.setReadable(false);

		JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
			testConnection.setCaCertificateFile(testCACertificateFile.getAbsolutePath());
		});

		MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsString("non exist"));
		assertEquals(ErrorHandlerInterface.CA_CERTIFICATE_FILE_UNREADABLE, exception.getCode());
    }

	@Test
	public void testClone() {
		RedCapApiConnection cloneConn = (RedCapApiConnection)testConnection.clone();
		
		assertNotSame(cloneConn, testConnection);
		assertNotSame(cloneConn.errorHandler, testConnection.errorHandler);
		assertNotSame(cloneConn.serverUri, testConnection.serverUri);

		assertSame(cloneConn.httpClient, testConnection.httpClient);
	}
	
	/*    
    
    public function testCurlErrorWithNoMessage()
    {
        $stringError = 'Peer certificate cannot be authenticated with given CA certificates 6';
        
        SystemFunctions::setCurlErrorInfo($number  = 60, $message = '', $stringError);
        
        $caughtException = false;
        try {
            $apiConnection = new RedCapApiConnection($this->apiUrl);
            $apiConnection->call('data');
        } catch (PhpCapException $exception) {
            $caughtException = true;
            $this->assertEquals(
                $exception->getCode(),
                ErrorHandlerInterface::CONNECTION_ERROR,
                'Exception code check.'
            );
            $this->assertEquals($stringError, $exception->getMessage(), 'Message check.');
        }
        $this->assertTrue($caughtException, 'Caught exception.');
        SystemFunctions::setCurlErrorInfo(0, '', '');
    }
    
    public function testCurlErrorWithNoMessageOrMessageString()
    {
        SystemFunctions::setCurlErrorInfo($number = 60, $message = '', $stringError = null);
        
        $caughtException = false;
        try {
            $apiConnection = new RedCapApiConnection($this->apiUrl);
            $apiConnection->call('data');
        } catch (PhpCapException $exception) {
            $caughtException = true;
            $code = $exception->getCode();
            $this->assertEquals(
                ErrorHandlerInterface::CONNECTION_ERROR,
                $code,
                'Exception code check.'
            );
            # The error code should be contained in the error message
            $this->assertStringContainsString(strval($code), $exception->getMessage(), 'Message check.');
        }
        $this->assertTrue($caughtException, 'Caught exception.');
        SystemFunctions::setCurlErrorInfo(0, '', '');
    }
    
    public function testCallWithCurlError()
    {
        $data = array(
                'token' => '12345678901234567890123456789012',
                'content' => 'project',
                'format' => 'json',
                'returnFormat' => 'json'
        );
        
        SystemFunctions::setCurlExecResponse('OK');
        SystemFunctions::$curlErrorNumber = 3;
        SystemFunctions::$curlErrorMessage = 'The URL was not properly formatted.';
        $exceptionCaught = false;
        try {
            $result = $this->connection->callWithArray($data);
        } catch (PhpCapException $exception) {
            $exceptionCaught = true;
            $this->assertEquals(
                ErrorHandlerInterface::CONNECTION_ERROR,
                $exception->getCode(),
                'Exception code check.'
            );
            $this->assertEquals(
                SystemFunctions::$curlErrorNumber,
                $exception->getConnectionErrorNumber(),
                'Connection error number check.'
            );
            $this->assertEquals(
                SystemFunctions::$curlErrorMessage,
                $exception->getMessage(),
                'Exception message check.'
            );
        }
        $this->assertTrue($exceptionCaught);
        SystemFunctions::setCurlExecResponse('');
        SystemFunctions::$curlErrorNumber = 0;
        SystemFunctions::$curlErrorMessage = '';
    }
    
    public function testCallWithHttpCode301()
    {
        SystemFunctions::setCurlExecResponse('OK');
        SystemFunctions::$httpCode = 301;
        $exceptionCaught = false;
        try {
            $result = $this->connection->call('data');
        } catch (PhpCapException $exception) {
            $exceptionCaught = true;
            $this->assertEquals(
                ErrorHandlerInterface::INVALID_URL,
                $exception->getCode(),
                'Exception code check.'
            );
            $this->assertEquals(
                SystemFunctions::$httpCode,
                $exception->getHttpStatusCode(),
                'HTTP status code check.'
            );
        }
        $this->assertTrue($exceptionCaught);
        SystemFunctions::setCurlExecResponse('');
        SystemFunctions::$httpCode = null;
    }

    public function testCallWithHttpCode404()
    {
        SystemFunctions::setCurlExecResponse('OK');
        SystemFunctions::$httpCode = 404;
        $exceptionCaught = false;
        try {
            $result = $this->connection->call('data');
        } catch (PhpCapException $exception) {
            $exceptionCaught = true;
            $this->assertEquals(
                $exception->getCode(),
                ErrorHandlerInterface::INVALID_URL,
                'Exception code check.'
            );
            $this->assertEquals(
                $exception->getHttpStatusCode(),
                SystemFunctions::$httpCode,
                'HTTP status code check.'
            );
        }
        $this->assertTrue($exceptionCaught);
        SystemFunctions::setCurlExecResponse('');
        SystemFunctions::$httpCode = null;
    }
    
    public function testCallWithInvalidData()
    {
        $exceptionCaught = false;
        try {
            # data should be a string
            $this->connection->call(123);
        } catch (PhpCapException $exception) {
            $exceptionCaught = true;
            $this->assertEquals(
                ErrorHandlerInterface::INVALID_ARGUMENT,
                $exception->getCode(),
                'Exception code check'
            );
        }
        $this->assertTrue($exceptionCaught, 'Exception caught.');
    }
*/
}
