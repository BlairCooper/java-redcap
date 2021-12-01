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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import edu.utah.hsir.javaRedcap.enums.REDCapApiFormat;

@SuppressWarnings("javadoc")
public class REDCapTest
{
	private String testSuperToken = "1234567890123456789012345678901234567890123456789012345678901234";
	private String testToken = "12345678901234567890123456789012";
    private REDCap redCap;
    private REDCapApiConnection mockConnection;
    private REDCapProject mockProject;
    private REDCapProjectFactory testFactory;

    @Before
    public void setUp() throws JavaREDCapException
    {
        mockConnection = Mockito.mock(REDCapApiConnection.class);
        Mockito.when(mockConnection.getUrl()).thenReturn("http://host.somedomain.com/redcap/api/");
        
    	mockProject = Mockito.mock(REDCapProject.class);
    	Mockito.when(mockProject.getConnection()).thenReturn(mockConnection);

    	testFactory = new REDCapProjectFactory() {
			@Override
			public REDCapProject createProject(String apiUrl, String apiToken, boolean sslVerify,
					String caCertificateFile, ErrorHandlerInterface errorHandler,
					REDCapApiConnectionInterface connection) throws JavaREDCapException {
                return mockProject;
			}
    	};

    	redCap = new REDCap(testSuperToken, mockConnection, null);
        redCap.setProjectFactory(testFactory);
    }

    @Test
    public void testCtor_withConnection() throws JavaREDCapException
    {
        REDCap redCap = new REDCap(testSuperToken, mockConnection, null);
        
        assertNotNull("REDCap object should not be null", redCap);
        
        ErrorHandlerInterface errorHandler = redCap.getErrorHandler();
        assertNotNull(errorHandler);
        assertTrue(errorHandler instanceof ErrorHandler);
        
        REDCapApiConnectionInterface connection = redCap.getConnection();
        assertNotNull(connection);
        assertSame(mockConnection, connection);
    }

    @Test
    public void testCtor_withNullConnection() throws JavaREDCapException
    {
    	JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
            new REDCap(testSuperToken, null, null);
    	});
        
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
    }

    @Test
    public void testCtor_withErrorHandler() throws JavaREDCapException
    {
    	ErrorHandler errorHandler = new ErrorHandler();
    	
        REDCap redCap = new REDCap(testSuperToken, mockConnection, errorHandler);

        ErrorHandlerInterface retrievedErrorHandler = redCap.getErrorHandler();
        assertNotNull(retrievedErrorHandler);
        assertSame(errorHandler, retrievedErrorHandler);
    }

    @Test
    public void testCtor_withoutConnection() throws JavaREDCapException
    {
        REDCap redCap = new REDCap(mockConnection.getUrl(), testSuperToken, false, null, null);
        
        assertNotNull("REDCap object should not be null", redCap);
        
        ErrorHandlerInterface errorHandler = redCap.getErrorHandler();
        assertNotNull(errorHandler);
        assertTrue(errorHandler instanceof ErrorHandler);
        
        REDCapApiConnectionInterface connection = redCap.getConnection();
        assertNotNull(connection);
        assertNotSame(mockConnection, connection);
    }

    @Test
    public void testCheckForRedcapError_emptyResponse() throws JavaREDCapException {
		redCap.checkForRedcapError("");
    }

    @Test
    public void testCheckForRedcapError_recordResponse() throws JavaREDCapException {
		redCap.checkForRedcapError("[{\"recordid\": \"1\"}]");
    }

    @Test
    public void testCheckForRedcapError_errorResponse() throws JavaREDCapException {
    	JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
    		redCap.checkForRedcapError("{\"error\": \"REDCap API error.\"}");
    	});
    	
    	assertEquals(ErrorHandlerInterface.REDCAP_API_ERROR, exception.getCode());
    	assertEquals("REDCap API error.", exception.getMessage());
    }

    @Test
    public void testSetConnection() throws JavaREDCapException
    {
    	REDCapApiConnectionInterface connection = redCap.getConnection();
        assertNotNull("Connection should not be null", connection);
        assertTrue("Connection class check failed", connection instanceof REDCapApiConnection);
        
        REDCapApiConnection localConnection = new REDCapApiConnection(connection.getUrl(), false, null);
        
        redCap.setConnection(localConnection);
        
        // Test that connection retrieved is the same one that was set
        REDCapApiConnectionInterface retrievedConnection = redCap.getConnection();
        assertNotNull(retrievedConnection);
        assertSame("Connection get check failed", localConnection, retrievedConnection);
        
        JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
        	redCap.setConnection(null);
        });
        
        assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
        MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("connection"));
    }
    
    @Test
    public void testSetErrorHandler()
    {
    	ErrorHandlerInterface errorHandler = redCap.getErrorHandler();
        assertNotNull("Error handler not null check failed", errorHandler);
        
        assertTrue("Error handler class check failed", errorHandler instanceof ErrorHandler);
        
        ErrorHandler localErrorHandler = new ErrorHandler();
        
        redCap.setErrorHandler(localErrorHandler);
        
        // Test that error handler retrieved is the same one that was set
        ErrorHandlerInterface retrievedErrorHandler = redCap.getErrorHandler();
        assertSame("Error handler get check failed", localErrorHandler, retrievedErrorHandler);
    }

    @Test
    public void testGetProjectFactory()
    {
    	REDCapProjectFactory projectFactory = redCap.getProjectFactory();
        assertNotNull(projectFactory);
    }

    @Test
    public void testSetProjectFactory()
    {
    	REDCapProjectFactory projectFactory = redCap.getProjectFactory();
    	assertNotNull(projectFactory);
    	
    	REDCapProjectFactory localFactory = new REDCapProjectFactory() {
			@Override
			public REDCapProject createProject(String apiUrl, String apiToken, boolean sslVerify,
					String caCertificateFile, ErrorHandlerInterface errorHandler,
					REDCapApiConnectionInterface connection) throws JavaREDCapException {
				return null;
			}
    	};
    	
    	redCap.setProjectFactory(localFactory);
    	
    	REDCapProjectFactory retrievedFactory = redCap.getProjectFactory();
    	
    	assertNotSame(projectFactory, retrievedFactory);
    	assertSame(localFactory, retrievedFactory);
    	
		redCap.setProjectFactory(null);
		
		// should still be using the last factory
		retrievedFactory = redCap.getProjectFactory();
		assertNotNull(retrievedFactory);
		assertSame(localFactory, retrievedFactory);
    }

    @Test
    public void testCtor_superTokenWithInvalidLength()
    {
    	JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
    		new REDCap("1234567890", mockConnection, null);
    	});

    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
        MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("token"));
    }

    @Test
    public void testCtor_superTokenWithInvalidCharacters()
    {
    	JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
    		new REDCap("ABCDEFG890123456789012345678901212345678901234567890123456789012", mockConnection, null);
    	});

    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
        MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("token"));
    }

    @Test
    public void testCreateProject_withJsonAndOdm() throws JavaREDCapException
    {
        Mockito.when(mockConnection.call(any())).thenReturn("12345678901234567890123456789012");

        String projectData = "[{\"project_title\": \"Test project.\", \"purpose\": \"0\"}]";
        
        REDCapProject project = redCap.createProject(projectData, REDCapApiFormat.JSON, "");
        
        assertSame(mockProject, project);
    }

    @Test
    public void testCreateProject_withMap() throws JavaREDCapException
    {
        Mockito.when(mockConnection.call(any())).thenReturn("12345678901234567890123456789012");

        List<Map<String, Object>> projectData = Arrays.asList(
        		Map.of(
        				"project_title", "Test project",
        				"purpose", "0"
        				)
        		);
        REDCapProject project = redCap.createProject(projectData, "");
        
        assertSame(mockProject, project);
    }

    @Test
    public void testCreateProject_withRedCapApiError() throws JavaREDCapException
    {
        Mockito.when(mockConnection.call(any())).thenReturn("{\"error\": \"REDCap API error.\"}");

        String projectData = "[{\"project_title\": \"Test project.\", \"purpose\": \"0\"}]";

        JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
        	redCap.createProject(projectData, REDCapApiFormat.JSON, "");
        });
        
        assertEquals(ErrorHandlerInterface.REDCAP_API_ERROR, exception.getCode());
        assertEquals("REDCap API error.", exception.getMessage());
    }

    @Test
    public void testCreateProject_withNullData() throws JavaREDCapException
    {
        JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
        	redCap.createProject(null, REDCapApiFormat.JSON, "");
        });
        
        assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
        MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("no value"));
    }

    @Test
    public void testGetProject() throws JavaREDCapException
    {
    	String localToken = new StringBuilder(testToken).reverse().toString();
    	Mockito.when(mockProject.getApiToken()).thenReturn(localToken);
    	
        REDCapProject project = redCap.getProject(localToken);
        
        assertNotNull(project);
        assertEquals(localToken, project.getApiToken());

        REDCapApiConnectionInterface conn = redCap.getConnection();
        assertEquals(conn.getUrl(), project.getConnection().getUrl());
        assertEquals(conn.getSslVerify(), project.getConnection().getSslVerify());
        assertEquals(conn.getCaCertificateFile(), project.getConnection().getCaCertificateFile());
    }

    @Test
    public void testGetProject_WithNullApiToken()
    {
        JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
            redCap.getProject(null);
        });
        
        assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
        MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("token"));
    }
    
    @Test
    public void testExportRedcapVersion() throws JavaREDCapException {
    	String testVersion = "11.4.4";

    	Mockito.when(mockConnection.call(any())).thenReturn(testVersion);
    	
    	String redcapVersion = redCap.exportRedcapVersion();
    	
    	assertNotNull(redcapVersion);
    	assertEquals(testVersion, redcapVersion);
    }
}
