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
import static org.junit.Assert.assertNull;
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

import edu.utah.hsir.javaRedcap.enums.REDCapApiContent;
import edu.utah.hsir.javaRedcap.enums.REDCapApiFormat;
import edu.utah.hsir.javaRedcap.enums.REDCapApiParameter;
import edu.utah.hsir.javaRedcap.enums.REDCapApiRawOrLabel;
import edu.utah.hsir.javaRedcap.enums.REDCapApiType;

@SuppressWarnings("javadoc")
public class REDCapProjectTest
{
	private String apiUrl;
    private String apiToken;
    private REDCapApiConnection mockConnection;
    private REDCapProject redCapProject;

    @Before
    public void setUp() throws JavaREDCapException
    {
        apiUrl   = "https://redcap.somplace.edu/api/";
        apiToken = "12345678901234567890123456789012";
        
        

        mockConnection = Mockito.mock(REDCapApiConnection.class);
        Mockito.when(mockConnection.getUrl()).thenReturn(apiUrl);
        
        redCapProject = new REDCapProject(
            apiUrl,
            apiToken,
            false,
            null,
            null,
            mockConnection //connection
        );
        
    }
    
    @Test
    public void testCtor_withNoConnection() throws JavaREDCapException {
    	REDCapProject project = new REDCapProject(apiUrl, apiToken, false, null, null, null);
    	
    	assertNotNull(project.getConnection());
    }

    @Test
    public void testCtor_withNullApiToken()
    {
    	JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
            new REDCapProject(apiUrl, null, false, null, null, null);
    	});

    	assertEquals("Null API token exception code check failed", ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
    }
    
    @Test
    public void testCtor_withBlankApiToken()
    {
    	JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
            new REDCapProject(apiUrl, " ", false, null, null, null);
    	});

    	assertEquals("Blank API token exception code check failed", ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("blank"));
    }

    @Test
    public void testCtor_withApiTokenWithInvalidCharacter()
    {
    	JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
            new REDCapProject(apiUrl, "1234567890123456789012345678901G", false, null, null, null);
    	});

    	assertEquals("API token with invalid character exception code check failed", ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("format"));
    }
    
    @Test
    public void testCtor_withApiTokenWithIncorrectLength()
    {
    	JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
            new REDCapProject(apiUrl, "1234567890123456789012345678901", false, null, null, null);
    	});

    	assertEquals("API token with incorrect length exception code check failed", ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("format"));
    }
    
    @Test
    public void testCtor_withSimpleCtor() throws JavaREDCapException
    {
    	REDCapProject project = new REDCapProject(apiUrl, apiToken);
    	
    	assertNotNull(project.getErrorHandler());
    	assertNotNull(project.getConnection());
    	
    	assertEquals(apiToken, project.getApiToken());
    	assertEquals(apiUrl, project.getConnection().getUrl());
    }

    @Test
    public void testCtor_withConnection() throws JavaREDCapException {
    	REDCapProject project = new REDCapProject(apiToken, mockConnection);
    	
    	assertNotNull(project.getErrorHandler());
    	assertNotNull(project.getConnection());

    	assertEquals(apiToken, project.getApiToken());
    	assertEquals(mockConnection, project.getConnection());
    }

    @Test
    public void testSetErrorHandler() throws JavaREDCapException
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
    public void testSetConnection() throws JavaREDCapException
    {
        REDCapApiConnectionInterface connection = redCapProject.getConnection();
        assertNotNull("Connection not null check failed", connection);
        
        assertTrue("Connection class check failed", connection instanceof REDCapApiConnectionInterface);
        
        String url = connection.getUrl();
        
        REDCapApiConnectionInterface localConnection = new REDCapApiConnection(url, false, null, null);
        
        redCapProject.setConnection(localConnection);
        
        //# Test that connection retrieved is the same one that was set
        REDCapApiConnectionInterface retrievedConnection = redCapProject.getConnection();
        assertSame("Connection get check failed", localConnection, retrievedConnection);
        
        JavaREDCapException exception = assertThrows(JavaREDCapException.class, () -> {
        	redCapProject.setConnection(null);
        });
        
        assertEquals(ErrorHandlerInterface.INVALID_ARGUMENT, exception.getCode());
    	MatcherAssert.assertThat(exception.getMessage(), CoreMatchers.containsStringIgnoringCase("null"));
    }
    
    @Test
    public void testGetApiToken() throws JavaREDCapException {
    	REDCapProject project = new REDCapProject(apiUrl, apiToken, false, null, null, null);
    	
    	assertEquals(apiToken, project.getApiToken());
    }
    
    @Test
    public void testGetJavaRedapVersion() throws JavaREDCapException {
    	REDCapProject project = new REDCapProject(apiUrl, apiToken, false, null, null, null);
    	
    	assertEquals(Version.RELEASE_NUMBER, project.getJavaRedcapVersion());
    }
    
    @Test
    public void testExportArms_noParams() throws JavaREDCapException {
    	String expectedResponse = "[{\"arm_num\":1,\"name\":\"Drug A\"},{\"arm_num\":2,\"name\":\"Drug B\"}]";	

    	Mockito.when(mockConnection.call(any())).thenReturn(expectedResponse);
		ArgumentCaptor<REDCapApiRequest> argument = ArgumentCaptor.forClass(REDCapApiRequest.class);
    	
        List<Map<String, Object>> arms = redCapProject.exportArms();
        
		Mockito.verify(mockConnection, times(1)).call(argument.capture());
		
        // Validate  the call parameters
		REDCapApiRequest req = argument.getValue();
		
		assertEquals(apiToken, req.paramMap.get(REDCapApiParameter.TOKEN));
		assertEquals(REDCapApiContent.ARM, req.paramMap.get(REDCapApiParameter.CONTENT));
		assertEquals(REDCapApiFormat.JSON, req.paramMap.get(REDCapApiParameter.FORMAT));
		assertEquals(REDCapApiFormat.JSON, req.paramMap.get(REDCapApiParameter.RETURN_FORMAT));
		assertFalse(req.paramMap.keySet().contains(REDCapApiParameter.ARMS));

		// There should only be four parameters
		assertEquals(4, req.paramMap.keySet().size());

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
    public void testExportArms_withSet() throws JavaREDCapException {
    	String expectedResponse = "[{\"arm_num\":2,\"name\":\"Drug B\"}]";
    	Set<Integer> testSet = Set.of(Integer.valueOf(2));

    	Mockito.when(mockConnection.call(any())).thenReturn(expectedResponse);
		ArgumentCaptor<REDCapApiRequest> argument = ArgumentCaptor.forClass(REDCapApiRequest.class);

        List<Map<String, Object>> arms = redCapProject.exportArms(testSet);
        
		Mockito.verify(mockConnection, times(1)).call(argument.capture());
		
        // Validate  the call parameters
		REDCapApiRequest req = argument.getValue();
		
		assertEquals(apiToken, req.paramMap.get(REDCapApiParameter.TOKEN));
		assertEquals(REDCapApiContent.ARM, req.paramMap.get(REDCapApiParameter.CONTENT));
		assertEquals(REDCapApiFormat.JSON, req.paramMap.get(REDCapApiParameter.FORMAT));
		assertEquals(REDCapApiFormat.JSON, req.paramMap.get(REDCapApiParameter.RETURN_FORMAT));
		assertEquals(testSet, req.paramMap.get(REDCapApiParameter.ARMS));

		// There should only be five parameters
		assertEquals(5, req.paramMap.keySet().size());

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
    
    @Test
    public void testExportRecords_defaults() throws JavaREDCapException {
    	String expectedResponse = "[{\"recordid\":1,\"name\":\"Fred Flintstone\"},{\"recordid\":2,\"name\":\"Wilma Flintstone\"}]";

    	Mockito.when(mockConnection.call(any())).thenReturn(expectedResponse);
    	ArgumentCaptor<REDCapApiRequest> argument = ArgumentCaptor.forClass(REDCapApiRequest.class);

    	String resp =
    			redCapProject.exportRecords(
    					null,		// REDCapApiFormat format
    					null,		// REDCapApiType type
    					null,		// Set<String> recordIds
    					null,		// Set<String> fields
    					null,		// Set<String> forms
    					null,		// Set<String> events
    					null, 		// String filterLogic
    					null,		// REDCapApiRawOrLabel rawOrLabel
    					null,		// REDCapApiRawOrLabel rawOrLabelHeaders
    					false,		// boolean exportCheckboxLabel
    					false,		// boolean exportSurveyFields
    					false,		// boolean exportDataAccessGroups
    					null,		// String dateRangeBegin
    					null,		// String dateRangeEnd
    					null,		// REDCapApiCsvDelimiter csvDelimiter,
    					null		// REDCapApiDecimalCharacter decimalCharacter
    					);

		Mockito.verify(mockConnection, times(1)).call(argument.capture());
		
	    // Validate  the call parameters
		REDCapApiRequest req = argument.getValue();

		// implied parameters - automatically added
		assertEquals(apiToken, req.paramMap.get(REDCapApiParameter.TOKEN));
		assertEquals(REDCapApiFormat.JSON, req.paramMap.get(REDCapApiParameter.RETURN_FORMAT));
		assertEquals(REDCapApiContent.RECORD, req.paramMap.get(REDCapApiParameter.CONTENT));

		// explicit parameters - from method call
		assertEquals(REDCapApiFormat.JSON, req.paramMap.get(REDCapApiParameter.FORMAT));
		assertEquals(REDCapApiType.FLAT, req.paramMap.get(REDCapApiParameter.TYPE));
		assertNull(req.paramMap.get(REDCapApiParameter.RECORDS));
		assertNull(req.paramMap.get(REDCapApiParameter.FORMS));
		assertNull(req.paramMap.get(REDCapApiParameter.EVENTS));
		assertNull(req.paramMap.get(REDCapApiParameter.FILTER_LOGIC));
		assertEquals(REDCapApiRawOrLabel.RAW, req.paramMap.get(REDCapApiParameter.RAW_OR_LABEL));
		assertEquals(REDCapApiRawOrLabel.RAW, req.paramMap.get(REDCapApiParameter.RAW_OR_LABEL_HEADERS));
		assertFalse((boolean)req.paramMap.get(REDCapApiParameter.EXPORT_CHECKBOX_LABEL));
		assertFalse((boolean)req.paramMap.get(REDCapApiParameter.EXPORT_SURVEY_FIELDS));
		assertFalse((boolean)req.paramMap.get(REDCapApiParameter.EXPORT_DATA_ACCESS_GROUPS));
		assertNull(req.paramMap.get(REDCapApiParameter.DATE_RANGE_BEGIN));
		assertNull(req.paramMap.get(REDCapApiParameter.DATE_RANGE_END));
		assertNull(req.paramMap.get(REDCapApiParameter.CSV_DELIMITER));
		assertNull(req.paramMap.get(REDCapApiParameter.DECIMAL_CHARACTER));


		assertEquals(expectedResponse, resp);
/*		
	
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
*/	    
    }
}
