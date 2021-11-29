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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import edu.utah.hsir.javaRedcap.enums.RedCapApiContent;
import edu.utah.hsir.javaRedcap.enums.RedCapApiFormat;

public class RedCapProjectTest
{
	private String apiUrl;
    private String apiToken;
    private RedCapApiConnection mockConnection;
    private RedCapProject redCapProject;

    @Before
    public void setUp() throws JavaRedcapException
    {
        apiUrl   = "https://redcap.somplace.edu/api/";
        apiToken = "12345678901234567890123456789012";
        
        

        mockConnection = Mockito.mock(RedCapApiConnection.class);
        Mockito.when(mockConnection.getUrl()).thenReturn(apiUrl);
        
        redCapProject = new RedCapProject(
            apiUrl,
            apiToken,
            false,
            null,
            null,
            mockConnection //connection
        );
        
    }
    
    @Test
    public void testCreateProjectWithNoConnection() throws JavaRedcapException {
    	RedCapProject project = new RedCapProject(apiUrl, apiToken, false, null, null, null);
    	
    	assertNotNull(project.getConnection());
    }

    @Test
    public void testCreateProjectWithNullApiToken()
    {
    	JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
            new RedCapProject(apiUrl, null, false, null, null, null);
    	});

    	assertEquals("Null API token exception code check failed", ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
    }
    
    @Test
    public void testCreateProjectWithBlankApiToken()
    {
    	JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
            new RedCapProject(apiUrl, " ", false, null, null, null);
    	});

    	assertEquals("Blank API token exception code check failed", ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("blank"));
    }

    @Test
    public void testCreateProjectwithApiTokenWithInvalidCharacter()
    {
    	JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
            new RedCapProject(apiUrl, "1234567890123456789012345678901G", false, null, null, null);
    	});

    	assertEquals("API token with invalid character exception code check failed", ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("format"));
    }
    
    @Test
    public void testCreateProjectWithApiTokenWithIncorrectLength()
    {
    	JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
            new RedCapProject(apiUrl, "1234567890123456789012345678901", false, null, null, null);
    	});

    	assertEquals("API token with incorrect length exception code check failed", ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("format"));
    }

    @Test
    public void testSetErrorHandler() throws JavaRedcapException
    {
    	ErrorHandlerInterface errorHandler = redCapProject.getErrorHandler();
        assertNotNull("Error handler not null check failed", errorHandler);
        
        assertTrue("Error handler class check failed", errorHandler instanceof ErrorHandlerInterface);
        
        ErrorHandlerInterface localErrorHandler = new ErrorHandler();
        
        redCapProject.setErrorHandler(localErrorHandler);
        
        //# Test that error handler retrieved is the same one that was set
        ErrorHandlerInterface retrievedErrorHandler = redCapProject.getErrorHandler();
        assertSame("Error handler get check failed", localErrorHandler, retrievedErrorHandler);
    }
    
    @Test
    public void testSetConnection() throws JavaRedcapException
    {
        RedCapApiConnectionInterface connection = redCapProject.getConnection();
        assertNotNull("Connection not null check failed", connection);
        
        assertTrue("Connection class check failed", connection instanceof RedCapApiConnectionInterface);
        
        String url = connection.getUrl();
        
        RedCapApiConnectionInterface localConnection = new RedCapApiConnection(url, false, null, null);
        
        redCapProject.setConnection(localConnection);
        
        //# Test that connection retrieved is the same one that was set
        RedCapApiConnectionInterface retrievedConnection = redCapProject.getConnection();
        assertSame("Connection get check failed", localConnection, retrievedConnection);
        
        JavaRedcapException exception = assertThrows(JavaRedcapException.class, () -> {
        	redCapProject.setConnection(null);
        });
        
        assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
    }
    
    @Test
    public void testGetApiToken() throws JavaRedcapException {
    	RedCapProject project = new RedCapProject(apiUrl, apiToken, false, null, null, null);
    	
    	assertEquals(apiToken, project.getApiToken());
    }
    
    @Test
    public void testGetJavaRedapVersion() throws JavaRedcapException {
    	RedCapProject project = new RedCapProject(apiUrl, apiToken, false, null, null, null);
    	
    	assertEquals(Version.RELEASE_NUMBER, project.getJavaRedcapVersion());
    }
    
    @Test
    public void testExportArms_noParams() throws JavaRedcapException {
    	String expectedResponse = "[{\"arm_num\":1,\"name\":\"Drug A\"},{\"arm_num\":2,\"name\":\"Drug B\"}]";	

    	Mockito.when(mockConnection.call(any())).thenReturn(expectedResponse);
		ArgumentCaptor<RedCapApiParams> argument = ArgumentCaptor.forClass(RedCapApiParams.class);
    	
        List<Map<String, Object>> arms = redCapProject.exportArms();
        
		Mockito.verify(mockConnection, times(1)).call(argument.capture());
		
        // Validate  the call parameters
		RedCapApiParams params = argument.getValue();
		
		assertEquals(apiToken, params.get(RedCapApiParams.TOKEN));
		assertEquals(RedCapApiContent.ARM, params.get(RedCapApiParams.CONTENT));
		assertEquals(RedCapApiFormat.JSON, params.get(RedCapApiParams.FORMAT));
		assertEquals(RedCapApiFormat.JSON, params.get(RedCapApiParams.RETURN_FORMAT));
		assertFalse(params.keySet().contains(RedCapApiParams.ARMS));

		// There should only be four parameters
		assertEquals(4, params.keySet().size());

		// Validate the response
        assertNotNull(arms);
        assertEquals(2, arms.size());
        
        for (Map<String, Object> entry : arms) {
        	assertEquals(2, entry.keySet().size());
        	Integer armNum = (Integer) entry.get("arm_num");
        	String armName = (String) entry.get("name");
        	
        	assertNotNull(armNum);
        	assertNotNull(armName);
        	
        	switch(armNum) {
        		case 1:
        			assertEquals("Drug A", entry.get("name"));
        			break;
        		case 2:
        			assertEquals("Drug B", entry.get("name"));
        			break;
    			default:
    				fail("Unexpected arm name");
    				break;
        	}
        }
    }
    
    @Test
    public void testExportArms_withSet() throws JavaRedcapException {
    	String expectedResponse = "[{\"arm_num\":2,\"name\":\"Drug B\"}]";
    	Set<Integer> testSet = Set.of(Integer.valueOf(2));

    	Mockito.when(mockConnection.call(any())).thenReturn(expectedResponse);
		ArgumentCaptor<RedCapApiParams> argument = ArgumentCaptor.forClass(RedCapApiParams.class);

        List<Map<String, Object>> arms = redCapProject.exportArms(testSet);
        
		Mockito.verify(mockConnection, times(1)).call(argument.capture());
		
        // Validate  the call parameters
		RedCapApiParams params = argument.getValue();
		
		assertEquals(apiToken, params.get(RedCapApiParams.TOKEN));
		assertEquals(RedCapApiContent.ARM, params.get(RedCapApiParams.CONTENT));
		assertEquals(RedCapApiFormat.JSON, params.get(RedCapApiParams.FORMAT));
		assertEquals(RedCapApiFormat.JSON, params.get(RedCapApiParams.RETURN_FORMAT));
		assertEquals(testSet, params.get(RedCapApiParams.ARMS));

		// There should only be five parameters
		assertEquals(5, params.keySet().size());

		// Validate the response
        assertNotNull(arms);
        assertEquals(1, arms.size());
        
        for (Map<String, Object> entry : arms) {
        	assertEquals(2, entry.keySet().size());
        	Integer armNum = (Integer) entry.get("arm_num");
        	String armName = (String) entry.get("name");
        	
        	assertNotNull(armNum);
        	assertNotNull(armName);
        	
        	switch(armNum) {
        		case 2:
        			assertEquals("Drug B", entry.get("name"));
        			break;
    			default:
    				fail("Unexpected arm name");
    				break;
        	}
        }
    }
}
