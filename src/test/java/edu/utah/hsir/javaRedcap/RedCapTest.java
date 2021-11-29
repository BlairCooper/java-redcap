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

import edu.utah.hsir.javaRedcap.enums.RedCapApiFormat;

public class RedCapTest
{
	private String testSuperToken = "1234567890123456789012345678901234567890123456789012345678901234";
	private String testToken = "12345678901234567890123456789012";
    private RedCap redCap;
    private RedCapApiConnection mockConnection;
    private RedCapProject mockProject;
    private RedCapProjectFactory testFactory;

    @Before
    public void setUp() throws JavaRedcapException
    {
        mockConnection = Mockito.mock(RedCapApiConnection.class);
        Mockito.when(mockConnection.getUrl()).thenReturn("http://host.somedomain.com/redcap/api/");
        
    	mockProject = Mockito.mock(RedCapProject.class);
    	Mockito.when(mockProject.getConnection()).thenReturn(mockConnection);

    	testFactory = new RedCapProjectFactory() {
			@Override
			public RedCapProject createProject(String apiUrl, String apiToken, boolean sslVerify,
					String caCertificateFile, ErrorHandlerInterface errorHandler,
					RedCapApiConnectionInterface connection) throws JavaRedcapException {
                return mockProject;
			}
    	};

    	redCap = new RedCap(testSuperToken, mockConnection, null);
        redCap.setProjectFactory(testFactory);
    }

    @Test
    public void testCtor_withConnection() throws JavaRedcapException
    {
        RedCap redCap = new RedCap(testSuperToken, mockConnection, null);
        
        assertNotNull("RedCap object should not be null", redCap);
        
        ErrorHandlerInterface errorHandler = redCap.getErrorHandler();
        assertNotNull(errorHandler);
        assertTrue(errorHandler instanceof ErrorHandler);
        
        RedCapApiConnectionInterface connection = redCap.getConnection();
        assertNotNull(connection);
        assertSame(mockConnection, connection);
    }

    @Test
    public void testCtor_withNullConnection() throws JavaRedcapException
    {
    	JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
            new RedCap(testSuperToken, null, null);
    	});
        
    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
    }

    @Test
    public void testCtor_withErrorHandler() throws JavaRedcapException
    {
    	ErrorHandler errorHandler = new ErrorHandler();
    	
        RedCap redCap = new RedCap(testSuperToken, mockConnection, errorHandler);

        ErrorHandlerInterface retrievedErrorHandler = redCap.getErrorHandler();
        assertNotNull(retrievedErrorHandler);
        assertSame(errorHandler, retrievedErrorHandler);
    }

    @Test
    public void testCtor_withoutConnection() throws JavaRedcapException
    {
        RedCap redCap = new RedCap(mockConnection.getUrl(), testSuperToken, false, null, null);
        
        assertNotNull("RedCap object should not be null", redCap);
        
        ErrorHandlerInterface errorHandler = redCap.getErrorHandler();
        assertNotNull(errorHandler);
        assertTrue(errorHandler instanceof ErrorHandler);
        
        RedCapApiConnectionInterface connection = redCap.getConnection();
        assertNotNull(connection);
        assertNotSame(mockConnection, connection);
    }

    @Test
    public void testCheckForRedcapError_emptyResponse() throws JavaRedcapException {
		redCap.checkForRedcapError("");
    }

    @Test
    public void testCheckForRedcapError_recordResponse() throws JavaRedcapException {
		redCap.checkForRedcapError("[{\"recordid\": \"1\"}]");
    }

    @Test
    public void testCheckForRedcapError_errorResponse() throws JavaRedcapException {
    	JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
    		redCap.checkForRedcapError("{\"error\": \"REDCap API error.\"}");
    	});
    	
    	assertEquals(ErrorHandlerInterface.REDCAP_API_ERROR, exception.getCode());
    	assertEquals("REDCap API error.", exception.getMessage());
    }

    @Test
    public void testSetConnection() throws JavaRedcapException
    {
    	RedCapApiConnectionInterface connection = redCap.getConnection();
        assertNotNull("Connection should not be null", connection);
        assertTrue("Connection class check failed", connection instanceof RedCapApiConnection);
        
        RedCapApiConnection localConnection = new RedCapApiConnection(connection.getUrl(), false, null);
        
        redCap.setConnection(localConnection);
        
        // Test that connection retrieved is the same one that was set
        RedCapApiConnectionInterface retrievedConnection = redCap.getConnection();
        assertNotNull(retrievedConnection);
        assertSame("Connection get check failed", localConnection, retrievedConnection);
        
        JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
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
    	RedCapProjectFactory projectFactory = redCap.getProjectFactory();
        assertNotNull(projectFactory);
    }

    @Test
    public void testSetProjectFactory()
    {
    	RedCapProjectFactory projectFactory = redCap.getProjectFactory();
    	assertNotNull(projectFactory);
    	
    	RedCapProjectFactory localFactory = new RedCapProjectFactory() {
			@Override
			public RedCapProject createProject(String apiUrl, String apiToken, boolean sslVerify,
					String caCertificateFile, ErrorHandlerInterface errorHandler,
					RedCapApiConnectionInterface connection) throws JavaRedcapException {
				return null;
			}
    	};
    	
    	redCap.setProjectFactory(localFactory);
    	
    	RedCapProjectFactory retrievedFactory = redCap.getProjectFactory();
    	
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
    	JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
    		new RedCap("1234567890", mockConnection, null);
    	});

    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
        MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("token"));
    }

    @Test
    public void testCtor_superTokenWithInvalidCharacters()
    {
    	JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
    		new RedCap("ABCDEFG890123456789012345678901212345678901234567890123456789012", mockConnection, null);
    	});

    	assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
        MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("token"));
    }

    @Test
    public void testCreateProject_withJsonAndOdm() throws JavaRedcapException
    {
        Mockito.when(mockConnection.call(any())).thenReturn("12345678901234567890123456789012");

        String projectData = "[{\"project_title\": \"Test project.\", \"purpose\": \"0\"}]";
        
        RedCapProject project = redCap.createProject(projectData, RedCapApiFormat.JSON, "");
        
        assertSame(mockProject, project);
    }

    @Test
    public void testCreateProject_withMap() throws JavaRedcapException
    {
        Mockito.when(mockConnection.call(any())).thenReturn("12345678901234567890123456789012");

        List<Map<String, Object>> projectData = Arrays.asList(
        		Map.of(
        				"project_title", "Test project",
        				"purpose", "0"
        				)
        		);
        RedCapProject project = redCap.createProject(projectData, "");
        
        assertSame(mockProject, project);
    }

    @Test
    public void testCreateProject_withRedCapApiError() throws JavaRedcapException
    {
        Mockito.when(mockConnection.call(any())).thenReturn("{\"error\": \"REDCap API error.\"}");

        String projectData = "[{\"project_title\": \"Test project.\", \"purpose\": \"0\"}]";

        JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
        	redCap.createProject(projectData, RedCapApiFormat.JSON, "");
        });
        
        assertEquals(ErrorHandlerInterface.REDCAP_API_ERROR, exception.getCode());
        assertEquals("REDCap API error.", exception.getMessage());
    }

    @Test
    public void testCreateProject_withNullData() throws JavaRedcapException
    {
        JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
        	redCap.createProject(null, RedCapApiFormat.JSON, "");
        });
        
        assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
        MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("no value"));
    }

    @Test
    public void testGetProject() throws JavaRedcapException
    {
    	String localToken = new StringBuilder(testToken).reverse().toString();
    	Mockito.when(mockProject.getApiToken()).thenReturn(localToken);
    	
        RedCapProject project = redCap.getProject(localToken);
        
        assertNotNull(project);
        assertEquals(localToken, project.getApiToken());

        RedCapApiConnectionInterface conn = redCap.getConnection();
        assertEquals(conn.getUrl(), project.getConnection().getUrl());
        assertEquals(conn.getSslVerify(), project.getConnection().getSslVerify());
        assertEquals(conn.getCaCertificateFile(), project.getConnection().getCaCertificateFile());
    }

    @Test
    public void testGetProject_WithNullApiToken()
    {
        JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
            redCap.getProject(null);
        });
        
        assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
        MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("token"));
    }
    
    @Test
    public void testExportRedcapVersion() throws JavaRedcapException {
    	String testVersion = "11.4.4";

    	Mockito.when(mockConnection.call(any())).thenReturn(testVersion);
    	
    	String redcapVersion = redCap.exportRedcapVersion();
    	
    	assertNotNull(redcapVersion);
    	assertEquals(testVersion, redcapVersion);
    }
}
