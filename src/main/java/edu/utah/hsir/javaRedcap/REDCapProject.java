/* --------------------------------------------------------------------------
 * Copyright (C) 2021 University of Utah Health Systems Research & Innovation
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 * Derived from work by The Trustees of Indiana University Copyright (C) 2019 
 * --------------------------------------------------------------------------
 */

package edu.utah.hsir.javaRedcap;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.utah.hsir.javaRedcap.enums.REDCapApiAction;
import edu.utah.hsir.javaRedcap.enums.REDCapApiContent;
import edu.utah.hsir.javaRedcap.enums.REDCapApiCsvDelimiter;
import edu.utah.hsir.javaRedcap.enums.REDCapApiDateFormat;
import edu.utah.hsir.javaRedcap.enums.REDCapApiDecimalCharacter;
import edu.utah.hsir.javaRedcap.enums.REDCapApiFormat;
import edu.utah.hsir.javaRedcap.enums.REDCapApiLogType;
import edu.utah.hsir.javaRedcap.enums.REDCapApiOverwriteBehavior;
import edu.utah.hsir.javaRedcap.enums.REDCapApiRawOrLabel;
import edu.utah.hsir.javaRedcap.enums.REDCapApiReturnContent;
import edu.utah.hsir.javaRedcap.enums.REDCapApiType;

/**
 * REDCap project class used to retrieve data from, and modify, REDCap projects.
 * 
 * Most methods in the class return either a String or a List of Maps of
 * String to Object pairs (List&lt;Map&lt;String, Object&gt;&gt;). A string is returned with
 * the method has been called specifying a particular data format (e.g. JSON or
 * XML). When method is called that does not specify a format a List of records
 * or rows is returned where each entry in the list is a Map of field names to
 * values.
 */
public class REDCapProject
{
	static final String JSON_RESULT_ERROR_PATTERN = "^[\\s]*\\{\\\"error\\\":[\\s]*\\\"(.*)\\\"\\}[\\s]*$";

    /** string REDCap API token for the project */
    protected String apiToken;

    /** REDCapApiConnection connection to the REDCap API at the apiURL. */
    protected REDCapApiConnectionInterface connection;

    /** Error handler for the project. */
    protected ErrorHandlerInterface errorHandler;


    /**
     * Creates a REDCapProject object for the specifed project.
     *
     * Example Usage:
     * <pre>
     * <code class="java">
     * String apiUrl = 'https://redcap.someplace.edu/api/'; # replace with your API URL
     * String apiToken = '11111111112222222222333333333344'; # replace with your API token
     * boolean sslVerify = true;
     *
     * # See the documentation for information on how to set this file up
     * String caCertificateFile = 'USERTrustRSACertificationAuthority.crt';
     *
     * REDCapProject project = new REDCapProject(apiUrl, apiToken, sslVerify, caCertificateFile);
     * </code>
     * </pre>
     *
     * @param apiUrl the URL for the API for the REDCap site that has the project.
     * @param apiToken the API token for this project.
     * @param sslVerify indicates if SSL connection to REDCap web site should be verified.
     * @param caCertificateFile the full path name of the CA (Certificate Authority)
     *     certificate file.
     * @param errorHandler the error handler used by the project.
     *    This would normally only be set if you want to override JavaRedcap's default
     *    error handler.
     * @param connection the connection used by the project.
     *    This would normally only be set if you want to override JavaRedcap's default
     *    connection. If this argument is specified, the apiUrl, sslVerify, and
     *    caCertificateFile arguments will be ignored, and the values for these
     *    set in the connection will be used.
     *
     * @throws JavaREDCapException if any of the arguments are invalid
     */
    public REDCapProject (
    		String apiUrl,
    		String apiToken,
    		boolean sslVerify,
    		String caCertificateFile,
    		ErrorHandlerInterface errorHandler,
    		REDCapApiConnectionInterface connection
    ) throws JavaREDCapException {
    	setErrorHandler(errorHandler);

        this.apiToken = REDCap.processApiTokenArgument(apiToken, 32, this.errorHandler);

        if (null != connection) {
            this.connection = connection;
        } else {
            this.connection = new REDCapApiConnection(apiUrl, sslVerify, caCertificateFile);
        }
    }

    /**
     * Creates a REDCapProject object for the specifed project.
     *
     * Example Usage:
     * <pre>
     * <code class="java">
     * String apiUrl = 'https://redcap.someplace.edu/api/'; # replace with your API URL
     * String apiToken = '11111111112222222222333333333344'; # replace with your API token
     *
     * REDCapProject project = new REDCapProject(apiUrl, apiToken);
     * </code>
     * </pre>
     *
     * @param apiUrl the URL for the API for the REDCap site that has the project.
     * @param apiToken the API token for this project.
     *
     * @throws JavaREDCapException if any of the arguments are invalid
     */
    public REDCapProject (String apiUrl, String apiToken) throws JavaREDCapException {
    	setErrorHandler(null);

        this.apiToken = REDCap.processApiTokenArgument(apiToken, 32, this.errorHandler);
        this.connection = new REDCapApiConnection(apiUrl, false, null);
    }
    

    /**
     * Exports all the numbers and names of the arms in the project.
     *
     * @return The information about each of the arms specified for the project.
     *  
     * @throws JavaREDCapException Thrown if there is a problem communicating
     * 		with REDCap.
     */
    public List<Map<String, Object>> exportArms() throws JavaREDCapException {
    	return exportArms(new HashSet<Integer>());
    }

    /**
     * Exports all the numbers and names of the arms in the project.
     *
     * @param arms Set of integers that are the numbers of the arms to export.
     *     If no arms are specified, then information for all arms will be returned.
     *
     * @return A string containing the data in JSON format.
     * 
     * @throws JavaREDCapException Thrown if there is a problem communicating
     * 		with REDCap or with one of the input parameters.
     */
    public List<Map<String, Object>> exportArms(Set<Integer> arms) throws JavaREDCapException {
    	String result = exportArms(REDCapApiFormat.JSON, arms);

    	List<Map<String, Object>> nativeResult;
		try {
			nativeResult = new ObjectMapper().readValue(result, new TypeReference<List<Map<String, Object>>>(){});
		} catch (JsonProcessingException e) {
			throw new JavaREDCapException("Exception processing REDCap response", ErrorHandlerInterface.JSON_ERROR, e);
		}

		return nativeResult;
    }

    /**
     * Exports the numbers and names of the arms in the project in the
     * specified format.
     *
     * @param format The format used to export the arm data.
     *     <ul>
     *       <li> REDCapApiFormat.CSV - string of CSV (comma-separated values)</li>
     *       <li> REDCapApiFormat.JSON - string of JSON encoded values</li>
     *       <li> REDCapApiFormat.XML - string of XML encoded data</li>
     *       <li> null - defaults to REDCapApiFormat.JSON</li>
     *     </ul>
     *
     * @return A string containing the data.
     * 
     * @throws JavaREDCapException Thrown if there is an issue communicating
     * 		with the REDCap server.  
     */
    public String exportArms(REDCapApiFormat format) throws JavaREDCapException {
    	return exportArms(format, null);
    }

    /**
     * Exports the numbers and names of the specified arms in the specified
     * format.
     *
     * @param format The format used to export the arm data.
     *     <ul>
     *       <li> REDCapApiFormat.CSV - string of CSV (comma-separated values)</li>
     *       <li> REDCapApiFormat.JSON - string of JSON encoded values</li>
     *       <li> REDCapApiFormat.XML - string of XML encoded data</li>
     *       <li> null - defaults to REDCapApiFormat.JSON</li>
     *     </ul>
     * @param arms Set of integers that are the numbers of the arms to export.
     * 		If null or no arms are specified, then information for all arms
     *
     * @return A string containing the data.
     * 
     * @throws JavaREDCapException Thrown if there is an issue communicating
     * 		with the REDCap server.  
     */
    public String exportArms(REDCapApiFormat format, Set<Integer> arms) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.ARM, errorHandler);

        data.setFormat(format);
        data.setArms(arms, false);

        String result = connection.call(data);
        result = processExportResult(result);

        return result;
    }

    /**
     * Imports the specified arms into the project.
     *
     * @param arms The arms to import. This will be a list of Map entries.
     * 			Each entry must have two keys, 'arm_num' and 'name'. 'arm_num'
     * 			must be a positive Integer and 'name' must be a String.
     *
     * @throws JavaREDCapException if an error occurs.
     *
     * @return The number of arms imported.
     */
    public int importArms(List<Map<String, Object>> arms) throws JavaREDCapException {
    	return importArms(arms, false);
    }

    /**
     * Imports the specified arms into the project.
     *
     * @param arms The arms to import. This will be a list of Map entries.
     * 			Each entry must have two keys, 'arm_num' and 'name'. 'arm_num'
     * 			must be a positive Integer and 'name' must be a String.
     * @param override A flag indicating whether to override any arms already
     * 		defined for the project.
     * 
     * @throws JavaREDCapException if an error occurs.
     *
     * @return The number of arms imported.
     */
    public int importArms(List<Map<String, Object>> arms, boolean override) throws JavaREDCapException {
    	int result = 0;

    	for (Map<String, Object> entry : arms) {
    		if (entry.size() != 2) {
    			errorHandler.throwException(
    					"Invalid declaration of a REDCap arm, only 'arm_num' and 'name' are allowed",
    					ErrorHandlerInterface.INVALID_ARGUMENT
    					);
    		}
    		else if (!entry.keySet().contains("arm_num") ||
    				 !entry.keySet().contains("name") ) {
    			errorHandler.throwException(
    					"Invalid declaration of a REDCap arm, missing 'arm_num' or 'name' properties",
    					ErrorHandlerInterface.INVALID_ARGUMENT
    					);
    		}
    		else {
    			Object armNum = entry.get("arm_num");
    			if (null != armNum && armNum instanceof Integer) {
    				if (((Integer)armNum).intValue() < 0) {
    					errorHandler.throwException(
    							"Arm number cannot be a negative number",
    							ErrorHandlerInterface.INVALID_ARGUMENT
    							);
    				}
    			}
    			else {
					errorHandler.throwException(
							"Arm number must be an Integer, " + armNum + " provided",
							ErrorHandlerInterface.INVALID_ARGUMENT
							);
    			}
    			Object armName = entry.get("name");
    			if (null != armName && armName instanceof String) {
    				if (0 == ((String)armName).trim().length()) {
    					errorHandler.throwException(
    							"Arm name must be provided",
    							ErrorHandlerInterface.INVALID_ARGUMENT
    							);
    				}
    			}
    			else {
					errorHandler.throwException(
							"Arm name must be a String, " + armName + " provided",
							ErrorHandlerInterface.INVALID_ARGUMENT
							);
    			}
    		}
    		entry.put("name", ((String)entry.get("name")).trim());
    	}

		try {
	    	// Convert to JSON and send it on.
	    	String json = new ObjectMapper().writeValueAsString(arms);
	    	result = importArms(json, REDCapApiFormat.JSON, override);
		} catch (JsonProcessingException e) {
			throw new JavaREDCapException("Exception preparing REDCap request", ErrorHandlerInterface.JSON_ERROR, e);
		}

    	return result;
    }

    /**
     * Imports the specified arms into the project.
     *
     * @param arms The arms to import. Assumed to be in the format specified
     * 			by format.
     * @param format The format of the data.
     *     <ul>
     *       <li> 'csv' - string of CSV (comma-separated values)</li>
     *       <li> 'json' - string of JSON encoded values</li>
     *       <li> 'xml' - string of XML encoded data</li>
     *     </ul>
     *
     * @throws JavaREDCapException if an error occurs.
     *
     * @return integer the number of arms imported.
     */
    public int importArms(String arms, REDCapApiFormat format) throws JavaREDCapException {
    	return importArms(arms, format, false);
    }

    /**
     * Imports the specified arms into the project.
     *
     * @param arms The arms to import. Assumed to be in the format specified
     * 			by format.
     * @param format The format of the data.
     *     <ul>
     *       <li> 'csv' - string of CSV (comma-separated values)</li>
     *       <li> 'json' - string of JSON encoded values</li>
     *       <li> 'xml' - string of XML encoded data</li>
     *     </ul>
     * @param override
     *     <ul>
     *       <li> false - [default] don't delete existing arms; only add new
     *       arms or renames existing arms.
     *       </li>
     *       <li> true - delete all existing arms before importing.</li>
     *     </ul>
     *     
     * @throws JavaREDCapException if an error occurs.
     *
     * @return The number of arms imported.
     */
    public int importArms(String arms, REDCapApiFormat format, boolean override) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.ARM, errorHandler);
    	data.setAction(REDCapApiAction.IMPORT);

        //#---------------------------------------
        //# Process arguments
        //#---------------------------------------
        data.setFormat(format);
        data.setOverride(override);
        data.setData(processImportDataArgument(arms, "arms", format));

        String result = connection.call(data);

        processNonExportResult(result);

        return Integer.valueOf(result);
    }

    /**
     * Deletes the specified arms from the project.
     *
     * @param arms Set of arm numbers to delete.
     *
     * @return The number of arms deleted.
     *
     * @throws JavaREDCapException if an error occurs, including if the arms array is null or empty
     */
    public int deleteArms(Set<Integer> arms) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.ARM, errorHandler);
    	data.setAction(REDCapApiAction.DELETE);
    	data.setArms(arms, true);

        String result = connection.call(data);

        processNonExportResult(result);

        return Integer.valueOf(result);
    }


    /**
     * Exports information about the project events.
     *
     * @return Information about the events. Each element of the list is a
     * 		Map with the following keys:'event_name', 'arm_num',
     * 		'day_offset', 'offset_min', 'offset_max', 'unique_event_name',
     * 		'custom_event_label'
     * 
     * @throws JavaREDCapException Thrown if an error occurs.
     */
    public List<Map<String, Object>> exportEvents() throws JavaREDCapException {
    	return exportEvents(new HashSet<Integer>());
    }

    /**
     * Exports information about the specified events.
     *
     * @param arms A set of arm numbers for which events should be exported.
     *     If no arms are specified, then all events will be returned.
     *     
     * @return Information about the events. Each element of the list is a
     * 		Map with the following keys:'event_name', 'arm_num',
     * 		'day_offset', 'offset_min', 'offset_max', 'unique_event_name',
     * 		'custom_event_label'
     * 
     * @throws JavaREDCapException Thrown if an error occurs.
     */
    public List<Map<String, Object>> exportEvents(Set<Integer> arms) throws JavaREDCapException {
    	String result = exportEvents(REDCapApiFormat.JSON, arms);

    	List<Map<String, Object>> nativeResult = null;
		try {
			nativeResult = new ObjectMapper().readValue(result, new TypeReference<List<Map<String, Object>>>(){});
		} catch (JsonProcessingException e) {
			throw new JavaREDCapException("Exception procesing REDCap response", ErrorHandlerInterface.JSON_ERROR, e);
		}

		return nativeResult;
    }


    /**
     * Exports information about the specified events.
     *
     * @param format The format for the export.
     *     <ul>
     *       <li> 'csv' - string of CSV (comma-separated values)</li>
     *       <li> 'json' - string of JSON encoded values</li>
     *       <li> 'xml' - string of XML encoded data</li>
     *     </ul>
     *     
     * @return A string with the information about the specified events in the format requested.
     * 
     * @throws JavaREDCapException Thrown if an error occurs.
     */
    public String exportEvents(REDCapApiFormat format) throws JavaREDCapException {
    	return exportEvents(format, null);
    }

    /**
     * Exports information about the specified events.
     *
     * Example usage:
     * <pre>
     * <code class="java">
     * // export information about all events
     * List&lt;Map&lt;String, Object&gt;&gt; eventInfo = project.exportEvents();
     *
     * // export events in XML format for arms 1 and 2.
     * List&lt;Map&lt;String, Object&gt;&gt; eventInfo = project.exportEvents(RedCapApiformat.XML, new HashSet&lt;&gt;(Arrays.asList([1, 2])));
     * </code>
     * </pre>
     *
     * @param format The format for the export.
     *     <ul>
     *       <li> 'csv' - string of CSV (comma-separated values)</li>
     *       <li> 'json' - string of JSON encoded values</li>
     *       <li> 'xml' - string of XML encoded data</li>
     *     </ul>
     *     
     * @param arms A set of arm numbers for which events should be exported.
     *     If no arms are specified, then all events will be returned.
     *     
     * @return A string with the information about the specified events in the format requested.
     * 
     * @throws JavaREDCapException Thrown if an error occurs.
     */
    public String exportEvents(REDCapApiFormat format, Set<Integer> arms) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.EVENT, errorHandler);

        //#---------------------------------------
        //# Process arguments
        //#---------------------------------------
        data.setFormat(format);
        data.setArms(arms, false);

        //#------------------------------------------------------
        //# Get and process events
        //#------------------------------------------------------
        String events = connection.call(data);
        events = processExportResult(events);

        return events;
    }

    
    /**
     * Imports the specified events into the project.
     *
     * @param events The events to import. This will be a list of Map entries.
     * 			Each entry must have two keys, 'arm_num' and 'event_name'.
     * 			'arm_num' must be a positive Integer and 'event_name' must be
     * 			a String.
     *
     * @return The number of events imported.
     * 
     * @throws JavaREDCapException Thrown if an error occurs.
     */
    public int importEvents(List<Map<String, Object>> events) throws JavaREDCapException {
    	return importEvents(events, false);
    }

    /**
     * Imports the specified events into the project.
     *
     * @param events The events to import. This will be a list of Map entries.
     * 			Each entry must have two keys, 'arm_num' and 'event_name'.
     * 			'arm_num' must be a positive Integer and 'event_name' must be
     * 			a String.
     * @param override
     *     <ul>
     *       <li> false - [default] don't delete existing events; only add new
     *       events or renames existing events.
     *       </li>
     *       <li> true - delete all existing events before importing.</li>
     *     </ul>
     *     
     * @return The number of events imported.
     * 
     * @throws JavaREDCapException Thrown if an error occurs.
     */
    public int importEvents(List<Map<String, Object>> events, boolean override) throws JavaREDCapException {
    	int result = 0;

    	for (Map<String, Object> entry : events) {
    		if (entry.size() != 2) {
    			errorHandler.throwException(
    					"Invalid declaration of a REDCap arm, only 'arm_num' and 'event_name' are allowed",
    					ErrorHandlerInterface.INVALID_ARGUMENT
    					);
    		}
    		else if (!entry.keySet().contains("arm_num") ||
    				 !entry.keySet().contains("event_name") ) {
    			errorHandler.throwException(
    					"Invalid declaration of a REDCap arm, missing 'arm_num' or 'event_name' properties",
    					ErrorHandlerInterface.INVALID_ARGUMENT
    					);
    		}
    		else {
    			Object armNum = entry.get("arm_num");
    			if (null != armNum && armNum instanceof Integer) {
    				if (((Integer)armNum).intValue() < 0) {
    					errorHandler.throwException(
    							"Arm number cannot be a negative number",
    							ErrorHandlerInterface.INVALID_ARGUMENT
    							);
    				}
    			}
    			else {
					errorHandler.throwException(
							"Arm number must be an Integer, " + armNum + " provided",
							ErrorHandlerInterface.INVALID_ARGUMENT
							);
    			}
    			Object armName = entry.get("event_name");
    			if (null != armName && armName instanceof String) {
    				if (0 == ((String)armName).trim().length()) {
    					errorHandler.throwException(
    							"Event name must be specified",
    							ErrorHandlerInterface.INVALID_ARGUMENT
    							);
    				}
    			}
    			else {
					errorHandler.throwException(
							"Event name must be a String, " + armName + " provided",
							ErrorHandlerInterface.INVALID_ARGUMENT
							);
    			}
    		}
    		entry.put("event_name", ((String)entry.get("event_name")).trim());
    	}

    	try {
    		// Convert to JSON and send it on.
    		String json = new ObjectMapper().writeValueAsString(events);

    		result = importEvents(json, REDCapApiFormat.JSON, override);
    	}
    	catch (JsonProcessingException e) {
    		throw new JavaREDCapException("Exception preparing REDCap request", ErrorHandlerInterface.JSON_ERROR, e);
    	}

    	return result;
    }

    /**
     * Imports the specified events into the project.
     *
     * @param events The events to import as a String, formated as specified
     *  			by the format parameter.
     * @param format The format for the export.
     *     <ul>
     *       <li> 'csv' - string of CSV (comma-separated values)</li>
     *       <li> 'json' - string of JSON encoded values</li>
     *       <li> 'xml' - string of XML encoded data</li>
     *     </ul>
     *
     * @return The number of events imported.
     * 
     * @throws JavaREDCapException Thrown if an error occurs.
     */
    public int importEvents(String events, REDCapApiFormat format) throws JavaREDCapException {
    	return importEvents(events, format, false);
    }

    /**
     * Imports the specified events into the project.
     *
     * @param events The events to import as a String, formated as specified
     *  			by the format parameter.
     * @param format The format for the export.
     *     <ul>
     *       <li> 'csv' - string of CSV (comma-separated values)</li>
     *       <li> 'json' - string of JSON encoded values</li>
     *       <li> 'xml' - string of XML encoded data</li>
     *     </ul>
     * @param override
     *     <ul>
     *       <li> false - [default] don't delete existing events; only add new
     *       events or renames existing events.
     *       </li>
     *       <li> true - delete all existing events before importing.</li>
     *     </ul>
     *
     * @return The number of events imported.
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public int importEvents(String events, REDCapApiFormat format, boolean override) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.EVENT, errorHandler);
    	data.setAction(REDCapApiAction.IMPORT);

        //#---------------------------------------
        //# Process arguments
        //#---------------------------------------
        data.setFormat(format);
        data.setOverride(override);
        data.setData(processImportDataArgument(events, "events", format));

        String result = connection.call(data);

        processNonExportResult(result);

        return Integer.valueOf(result);
    }


    /**
     * Deletes the specified events from the project.
     *
     * @param events Array of event names of events to delete.
     *
     * @return The number of events deleted.
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public int deleteEvents(Set<String> events) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.EVENT, errorHandler);
    	data.setAction(REDCapApiAction.DELETE);
    	data.setEvents(events, true);

        String result = connection.call(data);

        processNonExportResult(result);

        return Integer.valueOf(result);
    }


    /**
     * Exports the field names for a project.
     *
     * @return A list of Map entires for each field in the project where the
     *     keys for the maps:
     *     <ul>
     *       <li>original_field_name</li>
     *       <li>choice_value</li>
     *       <li>export_field_name</li>
     *     </ul>
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public List<Map<String, String>> exportFieldNames() throws JavaREDCapException {
    	String result = exportFieldNames(REDCapApiFormat.JSON);

		List<Map<String, String>> nativeResult = null;
		try {
			nativeResult = new ObjectMapper().readValue(result, new TypeReference<List<Map<String, String>>>(){});
		} catch (JsonProcessingException e) {
			throw new JavaREDCapException ("Exception processing REDCap response", ErrorHandlerInterface.JSON_ERROR, e);
		}

		return nativeResult;
    }

    /**
     * Exports the field name for a project.
     *
     * @param field The name of the field for which to export field name
     *     information. If no field is specified, information for all fields
     *     is exported.
     *
     * @return A list of Map entires for each field in the project where the
     *     keys for the maps:
     *     <ul>
     *       <li>original_field_name</li>
     *       <li>choice_value</li>
     *       <li>export_field_name</li>
     *     </ul>
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public Map<String, String> exportFieldNames(String field) throws JavaREDCapException {
    	String result = exportFieldNames(REDCapApiFormat.JSON, field);

		List<Map<String, String>> nativeResult = null;
		try {
			nativeResult = new ObjectMapper().readValue(result, new TypeReference<List<Map<String, String>>>(){});
		} catch (JsonProcessingException e) {
			throw new JavaREDCapException ("Exception processing REDCap response", ErrorHandlerInterface.JSON_ERROR, e);
		}

		return nativeResult.get(0);
    }

    /**
     * Exports the fields names for a project.
     *
     * @param format The format for the export.
     *     <ul>
     *       <li> 'csv' - string of CSV (comma-separated values)</li>
     *       <li> 'json' - string of JSON encoded values</li>
     *       <li> 'xml' - string of XML encoded data</li>
     *     </ul>
     *
     * @return A string with the information about the project field names in the format requested.
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public String exportFieldNames(REDCapApiFormat format) throws JavaREDCapException {
    	return exportFieldNames(format, null);
    }

    /**
     * Exports the field names for a project.
     *
     * @param format The format for the export.
     *     <ul>
     *       <li> 'csv' - string of CSV (comma-separated values)</li>
     *       <li> 'json' - string of JSON encoded values</li>
     *       <li> 'xml' - string of XML encoded data</li>
     *     </ul>
     * @param field The name of the field for which to export field name
     * 			information. If no field is specified, information for all
     *     		fields is exported.
     *
     * @return A string with the information about the project field names in the format requested.
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public String exportFieldNames(REDCapApiFormat format, String field) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.EXPORT_FIELD_NAMES, errorHandler);

        //#---------------------------------------
        //# Process arguments
        //#---------------------------------------
        data.setFormat(format);
        data.setField(field, false);

        String result = connection.call(data);
        result = processExportResult(result);

        return result;
    }

    /**
     * Exports the specified file.
     *
     * @param recordId The record ID for the file to be exported.
     * @param field The name of the field containing the file to export.
     *
     * @return The contents of the file that was exported.
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public String exportFile(String recordId, String field) throws JavaREDCapException {
    	return exportFile(recordId, field, null, null);
    }

    /**
     * Exports the specified file.
     *
     * @param recordId The record ID for the file to be exported.
     * @param field The name of the field containing the file to export.
     * @param event The name of event for file export (for longitudinal studies).
     *
     * @return The contents of the file that was exported.
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public String exportFile(String recordId, String field, String event) throws JavaREDCapException {
    	return exportFile(recordId, field, event, null);
    }

    /**
     * Exports the specified file.
     *
     * @param recordId The record ID for the file to be exported.
     * @param field The name of the field containing the file to export.
     * @param repeatInstance The number of a repeating instance
     *
     * @return The contents of the file that was exported.
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public String exportFile(String recordId, String field, Integer repeatInstance) throws JavaREDCapException {
    	return exportFile(recordId, field, null, repeatInstance);
    }

    /**
     * Exports the specified file.
     *
     * @param recordId The record ID for the file to be exported.
     * @param field The name of the field containing the file to export.
     * @param event The name of event for file export (for longitudinal studies).
     * @param repeatInstance The number of a repeating instance
     *
     * @return The contents of the file that was exported.
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public String exportFile(String recordId, String field, String event, Integer repeatInstance) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.FILE, errorHandler);
    	data.setAction(REDCapApiAction.EXPORT);

        //#--------------------------------------------
        //# Process arguments
        //#--------------------------------------------
        data.setRecord(recordId, true);
        data.setField(field, true);
        data.setEvent(event);
        data.setRepeatInstance(repeatInstance);

        //#-------------------------------
        //# Get and process file
        //#-------------------------------
        String file = connection.call(data);
        file = processExportResult(file);

        return file;
    }

    
    /**
     * Imports the file into the field of the record with the specified event
     * and/or repeat instance, if any.
     *
     * @param filename The name of the file to import.
     * @param recordId The record ID of the record to import the file into.
     * @param field The field of the record to import the file into.
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public void importFile(String filename, String recordId, String field) throws JavaREDCapException {
    	importFile(filename, recordId, field, null, null);
    }

    /**
     * Imports the file into the field of the record with the specified event
     * and/or repeat instance, if any.
     *
     * @param filename The name of the file to import.
     * @param recordId The record ID of the record to import the file into.
     * @param field The field of the record to import the file into.
     * @param event The event of the record to import the file into
     *     (only for longitudinal studies).
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public void importFile(String filename, String recordId, String field, String event) throws JavaREDCapException {
    	importFile(filename, recordId, field, event, null);
    }

    /**
     * Imports the file into the field of the record with the specified event
     * and/or repeat instance, if any.
     *
     * @param filename The name of the file to import.
     * @param recordId The record ID of the record to import the file into.
     * @param field The field of the record to import the file into.
     * @param repeatInstance The repeat instance of the record to import
     *     the file into (only for studies that have repeating events
     *     and/or instruments).
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public void importFile(String filename, String recordId, String field, Integer repeatInstance) throws JavaREDCapException {
    	importFile(filename, recordId, field, null, repeatInstance);
    }

    /**
     * Imports the file into the field of the record with the specified event
     * and/or repeat instance, if any.
     *
     * Example usage:
     * <pre>
     * <code class="java">
     * ...
     * String file     = "../data/consent1001.txt";
     * String recordId = "1001";
     * String field    = "patient_document";
     * String event    = "enrollment_arm_1";
     * 
     * project.importFile(file, recordId, field, event, null);
     * ...
     * </code>
     * </pre>
     *
     * @param filename The name of the file to import.
     * @param recordId The record ID of the record to import the file into.
     * @param field The field of the record to import the file into.
     * @param event The event of the record to import the file into
     *     (only for longitudinal studies).
     * @param repeatInstance The repeat instance of the record to import
     *     the file into (only for studies that have repeating events
     *     and/or instruments).
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public void importFile(String filename, String recordId, String field, String event, Integer repeatInstance) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.FILE, errorHandler);
    	data.setAction(REDCapApiAction.IMPORT);

        //#----------------------------------------
        //# Process non-file arguments
        //#----------------------------------------
        data.setFile(filename);
        data.setRecord(recordId, true);
        data.setField(field, true);
        data.setEvent(event);
        data.setRepeatInstance(repeatInstance);

        //#---------------------------------------------------------------------
        //# For unknown reasons, "call" (instead of .call") needs to
        //# be used here (probably something to do with the 'file' data).
        //# REDCap's "API Playground" (also) makes no data conversion for this
        //# method.
        //#---------------------------------------------------------------------
        String result = connection.call(data);

        processNonExportResult(result);
    }


    /**
     * Deletes the specified file.
     *
     * @param recordId The record ID of the file to delete.
     * @param field The field name of the file to delete.
     * 
     * @return The response from the redcap server.
     * 
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public String deleteFile(String recordId, String field) throws JavaREDCapException {
    	return deleteFile(recordId, field, null, null);
    }

    /**
     * Deletes the specified file.
     *
     * @param recordId The record ID of the file to delete.
     * @param field The field name of the file to delete.
     * @param event The event of the file to delete (only for longitudinal studies).
     *
     * @return The response from the REDCap server.
     * 
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public String deleteFile(String recordId, String field, String event) throws JavaREDCapException {
    	return deleteFile(recordId, field, event, null);
    }

    /**
     * Deletes the specified file.
     *
     * @param recordId The record ID of the file to delete.
     * @param field The field name of the file to delete.
     * @param repeatingInstance The repeat instance of the file to delete
     *     (only for studies that have repeating events and/or instruments).
     *
     * @return The response from the REDCap server.
     * 
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public String deleteFile(String recordId, String field, Integer repeatingInstance) throws JavaREDCapException {
    	return deleteFile(recordId, field, null, repeatingInstance);
    }

    /**
     * Deletes the specified file.
     *
     * @param recordId The record ID of the file to delete.
     * @param field The field name of the file to delete.
     * @param event The event of the file to delete (only for longitudinal studies).
     * @param repeatInstance The repeat instance of the file to delete
     *     (only for studies that have repeating events and/or instruments).
     *
     * @return The respons from the REDCap server
     * 
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public String deleteFile(String recordId, String field, String event, Integer repeatInstance) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.FILE, errorHandler);
    	data.setAction(REDCapApiAction.DELETE);

        //#----------------------------------------
        //# Process arguments
        //#----------------------------------------
    	data.setRecord(recordId, true);
    	data.setField(field, true);
    	data.setEvent(event);
    	data.setRepeatInstance(repeatInstance);

        String result = connection.call(data);

        processNonExportResult(result);

        return result;
    }

    
    /**
     * Exports information about the instruments (data entry forms) for the project.
     *
     * @return A list of Map entries.
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public List<Map<String, String>> exportInstruments() throws JavaREDCapException {
    	String result = exportInstruments(REDCapApiFormat.JSON);

		List<Map<String, String>> nativeResult = null;
		try {
			nativeResult = new ObjectMapper().readValue(result, new TypeReference<List<Map<String, String>>>(){});
		} catch (JsonProcessingException e) {
			throw new JavaREDCapException ("Exception processing REDCap response", ErrorHandlerInterface.JSON_ERROR, e);
		}

		return nativeResult;
    }

    /**
     * Exports information about the instruments (data entry forms) for the project.
     *
     * @param format Format instruments are exported in:
     *     <ul>
     *       <li>'csv' - string of CSV (comma-separated values)</li>
     *       <li>'json' - string of JSON encoded data</li>
     *       <li>'xml' - string of XML encoded data</li>
     *     </ul>
     *
     * @return A string with the information about the project field names in the format requested.
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public String exportInstruments(REDCapApiFormat format) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.INSTRUMENT, errorHandler);
        data.setFormat(format);        

        String instrumentsData = connection.call(data);

        instrumentsData = processExportResult(instrumentsData);

        return instrumentsData;
    }

    /**
     * Exports a PDF version of the requested instruments (forms).
     *
     * @param file The name of the file (possibly with a path specified also)
     *     to store the PDF instruments in. Can be null.
     * @param recordId If record ID is specified, the forms retrieved will
     *     be filled with values for that record. Otherwise, they will be blank.
     * @param event A unique event name that is used when a record ID has been
     * 			specified to return only forms that are in that event (for the
     * 			specified records). If null all events will be returned (only
     * 			for longitudinal projects)
     * @param form If this is specified, only this form will be returned.
     * @param allRecords If this is set to true, all forms for all records
     * 			will be retrieved (the recordId, event, and form arguments
     *     		will be ignored).
     * @param compactDisplay Set to true to compact the field layout in the
     * 		PDF.
     *
     * @return string PDF content of requested instruments (forms).
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public String exportPdfFileOfInstruments(
        String file,
        String recordId,
        String event,
        String form,
        boolean allRecords,
        boolean compactDisplay
    ) throws JavaREDCapException {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.PDF, errorHandler);

    	if (allRecords) {
            data.setAllRecords(allRecords);
    	}
    	else {
            data.setRecord(recordId, false);
            data.setEvent(event);
            data.setInstrument(form, false);
    	}
    	data.setCompactDisplay(compactDisplay);

        String result = connection.call(data);

        if (null != file) {
            writeStringToFile(result, file);
        }

        return result;
    }

    
    /**
     * Gets the instrument to event mapping for the project.
     *
     * @return A List of Map entries have the following keys:
     *     <ul>
     *       <li>'arm_num'</li>
     *       <li>'unique_event_name'</li>
     *       <li>'form'</li>
     *     </ul>
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public List<Map<String, Object>> exportInstrumentEventMappings() throws JavaREDCapException {
    	return exportInstrumentEventMappings(null);
    }

    /**
     * Gets the instrument to event mapping for the project.
     *
     * @param arms A Set of integers that are the numbers of the arms for
     * 			which instrument/event mapping information should be exported.
     *     		If no arms are specified, then information for all arms will
     *     		be exported.
     *     
     * @return A List of Map entries have the following keys:
     *     <ul>
     *       <li>'arm_num'</li>
     *       <li>'unique_event_name'</li>
     *       <li>'form'</li>
     *     </ul>
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public List<Map<String, Object>> exportInstrumentEventMappings(Set<Integer> arms) throws JavaREDCapException {
    	String result = exportInstrumentEventMappings(REDCapApiFormat.JSON, arms);

		List<Map<String, Object>> nativeResult = null;
		try {
			nativeResult = new ObjectMapper().readValue(result, new TypeReference<List<Map<String, Object>>>(){});
		} catch (JsonProcessingException e) {
			throw new JavaREDCapException ("Exception processing REDCap response", ErrorHandlerInterface.JSON_ERROR, e);
		}

		return nativeResult;
    }

    /**
     * Gets the instrument to event mapping for the project.
     *
     * For example, the following code:
     * <pre>
     * <code class="java">
     * String mappings = project.exportInstrumentEventMappings();
     * </code>
     * </pre>
     *
     * @param format The format in which to export the records:
     *     <ul>
     *       <li> 'csv' - string of CSV (comma-separated values)</li>
     *       <li> 'json' - string of JSON encoded values</li>
     *       <li> 'xml' - string of XML encoded data</li>
     *     </ul>
     * @param arms A Set of integers that are the numbers of the arms for
     * 			for which instrument/event mapping information should be
     * 			exported. If no arms are specified, then information for
     * 			all arms will be exported.
     *
     * @return A string with the event mappings in the format requested.
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public String exportInstrumentEventMappings(REDCapApiFormat format, Set<Integer> arms) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.FORM_EVENT_MAPPING, errorHandler);

        //#------------------------------------------
        //# Process arguments
        //#------------------------------------------
        data.setFormat(format);
        data.setArms(arms, false);

        //#---------------------------------------------
        //# Get and process instrument-event mappings
        //#---------------------------------------------
        String instrumentEventMappings = connection.call(data);
        instrumentEventMappings = processExportResult(instrumentEventMappings);

        return instrumentEventMappings;
    }

    /**
     * Imports the specified instrument-event mappings into the project.
     *
     * @param mappings The mappings to import. This will a List of Map entries.
     * 			In all cases, the field names that are used in the mappings are:<br>
     *     		arm_num, unique_event_name, form
     *
     * @return The number of mappings imported.
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     *
     */
    public int importInstrumentEventMappings(List<Map<String, Object>> mappings) throws JavaREDCapException {
    	int result = 0;

    	for (Map<String, Object> entry : mappings) {
    		if (entry.size() != 3) {
    			errorHandler.throwException(
    					"Invalid declaration of a REDCap event mapping, only 'arm_num', 'unique_event_name' and 'form' are allowed",
    					ErrorHandlerInterface.INVALID_ARGUMENT
    					);
    		}
    		else if (!entry.keySet().contains("arm_num") ||
    				 !entry.keySet().contains("unique_event_name") ||
    				 !entry.keySet().contains("form") ) {
    			errorHandler.throwException(
    					"Invalid declaration of a REDCap arm, missing 'arm_num', 'unique_event_name' or 'name' properties",
    					ErrorHandlerInterface.INVALID_ARGUMENT
    					);
    		}
    		else {
    			Object armNum = entry.get("arm_num");
    			if (null != armNum && armNum instanceof Integer) {
    				if (((Integer)armNum).intValue() < 0) {
    					errorHandler.throwException(
    							"Arm number cannot be a negative number",
    							ErrorHandlerInterface.INVALID_ARGUMENT
    							);
    				}
    			}
    			else {
					errorHandler.throwException(
							"Arm number must be an Integer, " + armNum + " provided",
							ErrorHandlerInterface.INVALID_ARGUMENT
							);
    			}

    			Object armName = entry.get("unique_event_name");
    			if (null != armName && armName instanceof String) {
    				if (0 == ((String)armName).trim().length()) {
    					errorHandler.throwException(
    							"unique_event_name must be specified",
    							ErrorHandlerInterface.INVALID_ARGUMENT
    							);
    				}
    			}
    			else {
					errorHandler.throwException(
							"unique_event_name must be a String, " + armName + " provided",
							ErrorHandlerInterface.INVALID_ARGUMENT
							);
    			}

    			Object form = entry.get("form");
    			if (null != form && form instanceof String) {
    				if (0 == ((String)form).trim().length()) {
    					errorHandler.throwException(
    							"form must be specified",
    							ErrorHandlerInterface.INVALID_ARGUMENT
    							);
    				}
    			}
    			else {
					errorHandler.throwException(
							"form must be a String, " + form + " provided",
							ErrorHandlerInterface.INVALID_ARGUMENT
							);
    			}
    		}
    	}

    	try {
    		// Convert to JSON and send it on.
    		String json = new ObjectMapper().writeValueAsString(mappings);

    		result = importInstrumentEventMappings(json, REDCapApiFormat.JSON);
    	}
    	catch (JsonProcessingException e) {
    		throw new JavaREDCapException("Exception preparing REDCap request", ErrorHandlerInterface.JSON_ERROR, e);
    	}

    	return result;
    }

    /**
     * Imports the specified instrument-event mappings into the project.
     *
     * @param mappings The mappings to import in the format specified.
     * 				In all cases, the field names that are used in the
     * 				mappings are:<br>
     *     			arm_num, unique_event_name, form
     * @param format the format for the import.
     *     <ul>
     *       <li> 'csv' - string of CSV (comma-separated values)</li>
     *       <li> 'json' - string of JSON encoded values</li>
     *       <li> 'xml' - string of XML encoded data</li>
     *     </ul>
     *
     * @return The number of mappings imported.
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public int importInstrumentEventMappings(String mappings, REDCapApiFormat format) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.FORM_EVENT_MAPPING, errorHandler);

        //#---------------------------------------
        //# Process arguments
        //#---------------------------------------
    	data.setFormat(format);
    	data.setData(processImportDataArgument(mappings, "mappings", format));

        String result = connection.call(data);

        processNonExportResult(result);

        return Integer.valueOf(result);
    }

    
    /**
     * Exports metadata about the project, i.e., information about the fields in the project.
     *
     * @param fields A Set of field names for which metadata should be exported.
     * 			If not specified all fields will be exported.
     * @param forms A Set of form names. Metadata will be exported for all fields in the
     *     specified forms. If not specified, all forms will be exported.
     *
     * @return A list of Map entries of metatdata for the project, which consists of
     *         information about each field. Some examples of the information
     *         provided are: 'field_name', 'form_name', 'field_type', 'field_label'.
     *         See REDCap API documentation
     *         for more information.
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public List<Map<String, Object>> exportMetadata(Set<String> fields, Set<String> forms) throws JavaREDCapException
    {
    	String result = exportMetadata(REDCapApiFormat.JSON, fields, forms);

		List<Map<String, Object>> nativeResult = null;
		try {
			nativeResult = new ObjectMapper().readValue(result, new TypeReference<List<Map<String, Object>>>(){});
		} catch (JsonProcessingException e) {
			throw new JavaREDCapException ("Exception processing REDCap response", ErrorHandlerInterface.JSON_ERROR, e);
		}

		return nativeResult;
    }

    /**
     * Exports metadata about the project, i.e., information about the fields in the project.
     *
     * @param format The format for the export.
     *     <ul>
     *       <li> 'csv' - string of CSV (comma-separated values)</li>
     *       <li> 'json' - string of JSON encoded values</li>
     *       <li> 'xml' - string of XML encoded data</li>
     *     </ul>
     * @param fields A Set of field names for which metadata should be exported.
     * 			If not specified all fields will be exported.
     * @param forms A Set of form names. Metadata will be exported for all fields in the
     *     specified forms. If not specified, all forms will be exported.
     *
     * @return A string containing the metadata for the project in the format
     * 			requested, consisting of information about each field. Some
     * 			examples of the information provided are: 'field_name',
     * 			'form_name', 'field_type', 'field_label'.<br>
     *          See REDCap API documentation for more information on the
     *			results of this method.
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public String exportMetadata(REDCapApiFormat format, Set<String> fields, Set<String> forms) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.METADATA, errorHandler);

        //#---------------------------------------
        //# Process format
        //#---------------------------------------
        data.setFormat(format);
        data.setFields(fields);
        data.setForms(forms);

        //#-------------------------------------------
        //# Get and process metadata
        //#-------------------------------------------
        String metadata = connection.call(data);
        metadata = processExportResult(metadata);

        return metadata;
    }

    /**
     * Imports the specified metadata (field information) into the project.
     *
     * @param metadata A List of Map entries for the metadata to import.
     *
     * @return The number of fields imported.
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public int importMetadata(List<Map<String, Object>> metadata) throws JavaREDCapException {
    	int result = 0;

    	try {
    		// Convert to JSON and send it on.
    		String json = new ObjectMapper().writeValueAsString(metadata);

    		result = importMetadata(json, REDCapApiFormat.JSON);
    	}
    	catch (JsonProcessingException e) {
    		throw new JavaREDCapException("Exception preparing REDCap request", ErrorHandlerInterface.JSON_ERROR, e);
    	}

    	return result;
    }

    /**
     * Imports the specified metadata (field information) into the project.
     *
     * @param metadata A string with the metadata to import in the format specified.
     * @param format The format for the export.
     *     <ul>
     *       <li> 'csv' - string of CSV (comma-separated values)</li>
     *       <li> 'json' - string of JSON encoded values</li>
     *       <li> 'xml' - string of XML encoded data</li>
     *     </ul>
     *
     * @return The number of fields imported.
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public int importMetadata(String metadata, REDCapApiFormat format) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.METADATA, errorHandler);

        //#---------------------------------------
        //# Process arguments
        //#---------------------------------------
        data.setFormat(format);

        data.setData(processImportDataArgument(metadata, "metadata", format));

        String result = connection.call(data);

        processNonExportResult(result);

        return Integer.valueOf(result);
    }


    /**
     * Exports information about the project, e.g., project ID, project title, creation time.
     *
     * @return A Map of project information. See REDCap API documentation
     *         for a list of the fields in the results of this method.
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public Map<String, Object> exportProjectInfo() throws JavaREDCapException {
    	String result = exportProjectInfo(REDCapApiFormat.JSON);

		Map<String, Object> nativeResult = null;
		try {
			nativeResult = new ObjectMapper().readValue(result, new TypeReference<Map<String, Object>>(){});
		} catch (JsonProcessingException e) {
			throw new JavaREDCapException ("Exception processing REDCap response", ErrorHandlerInterface.JSON_ERROR, e);
		}

		return nativeResult;
    }

    /**
     * Exports information about the project, e.g., project ID, project title, creation time.
     *
     * @param format The format for the export.
     *     <ul>
     *       <li> 'csv' - string of CSV (comma-separated values)</li>
     *       <li> 'json' - string of JSON encoded values</li>
     *       <li> 'xml' - string of XML encoded data</li>
     *     </ul>
     *
     * @return A string containing the information about the project in the format requested.
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public String exportProjectInfo(REDCapApiFormat format) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest (apiToken, REDCapApiContent.PROJECT, errorHandler);

        //#---------------------------------------
        //# Process format
        //#---------------------------------------
        data.setFormat(format);

        //#---------------------------------------
        //# Get and process project information
        //#---------------------------------------
        String projectInfo = connection.call(data);
        projectInfo = processExportResult(projectInfo);

        return projectInfo;
    }

    /**
     * Imports the specified project information into the project.
     * The valid fields that can be imported are:
     *<br>
     * project_title, project_language, purpose, purpose_other, project_notes,
     * custom_record_label, secondary_unique_field, is_longitudinal,
     * surveys_enabled, scheduling_enabled, record_autonumbering_enabled,
     * randomization_enabled, project_irb_number, project_grant_number,
     * project_pi_firstname, project_pi_lastname, display_today_now_button
     * <p>
     * You do not need to specify all of these fields when doing an import,
     * only the ones that you actually want to change. For example:
     *
     * @param projectInfo The project information to import as a Map.
     *
     * @return The number of project info values specified that were valid,
     *     whether or not each valid value actually caused an update (i.e.,
     *     was different from the existing value before the method call).
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public int importProjectInfo(Map<String, Object> projectInfo) throws JavaREDCapException {
    	int result = 0;

    	try {
    		// Convert to JSON and send it on.
    		String json = new ObjectMapper().writeValueAsString(projectInfo);

    		result = importProjectInfo(json, REDCapApiFormat.JSON);
    	}
    	catch (JsonProcessingException e) {
    		throw new JavaREDCapException("Exception preparing REDCap request", ErrorHandlerInterface.JSON_ERROR, e);
    	}

    	return result;
    }

    /**
     * Imports the specified project information into the project.
     * The valid fields that can be imported are:
     *<br>
     * project_title, project_language, purpose, purpose_other, project_notes,
     * custom_record_label, secondary_unique_field, is_longitudinal,
     * surveys_enabled, scheduling_enabled, record_autonumbering_enabled,
     * randomization_enabled, project_irb_number, project_grant_number,
     * project_pi_firstname, project_pi_lastname, display_today_now_button
     *<p>
     * You do not need to specify all of these fields when doing an import,
     * only the ones that you actually want to change. For example:
     * <pre>
     * <code class="java">
     * ...
     * # Set the project to be longitudinal and enable surveys
     * projectInfo = ['is_longitudinal' : 1, 'surveys_enabled' : 1];
     * project.importProjectInfo(projectInfo);
     * ...
     * </code>
     * </pre>
     *
     * @param projectInfo The project information to import in the format specified.
     * @param format The format for the export.
     *     <ul>
     *       <li> 'csv' - string of CSV (comma-separated values)</li>
     *       <li> 'json' - string of JSON encoded values</li>
     *       <li> 'xml' - string of XML encoded data</li>
     *     </ul>
     *
     * @return The number of project info values specified that were valid,
     *     whether or not each valid value actually caused an update (i.e.,
     *     was different from the existing value before the method call).
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public int importProjectInfo(String projectInfo, REDCapApiFormat format) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.PROJECT_SETTINGS, errorHandler);

        //#---------------------------------------
        //# Process arguments
        //#---------------------------------------
        data.setFormat(format);
        data.setData(processImportDataArgument(projectInfo, "projectInfo", format));

        String result = connection.call(data);

        processNonExportResult(result);

        return Integer.valueOf(result);
    }

    /**
     * Exports the specified information of project in XML format.
     *
     * @param returnMetadataOnly If set to true, only the metadata for the
     * 		project is returned. If set to false, the metadata and data for
     * 		the project is returned.
     * @param recordIds A Set of record id's that are to be retrieved. If null
     * 		or empty all records are returned.
     * @param fields A Set of field names to export. If null or empty all fields
     * 		are returned.
     * @param events A Set of event names for which fields should be exported.
     * 		If null or empty all events are returned.
     * @param filterLogic Logic used to restrict the records retrieved, e.g.,
     *     "[last_name] = 'Smith'".
     * @param exportSurveyFields Specifies whether survey fields should be exported.
     *     <ul>
     *       <li> true - export the following survey fields:
     *         <ul>
     *           <li> survey identifier field ('redcap_survey_identifier') </li>
     *           <li> survey timestamp fields (instrument+'_timestamp') </li>
     *         </ul>
     *       </li>
     *       <li> false - survey fields are not exported.</li>
     *     </ul>
     * @param exportDataAccessGroups Specifies whether the data access group field
     *      ('redcap_data_access_group') should be exported.
     *     <ul>
     *       <li> true - export the data access group field if there is at least one data access group, and
     *                   the user calling the method (as identified by the API token) is not
     *                   in a data access group.</li>
     *       <li> false - don't export the data access group field.</li>
     *     </ul>
     * @param exportFiles If this is set to true, files will be exported in the XML.
     *     If set to false, files will not be exported.
     *     
     * @return The specified information for the project in XML format.
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public String exportProjectXml(
        boolean returnMetadataOnly,
        Set<String>recordIds,
        Set<String>fields,
        Set<String>events,
        String filterLogic,
        boolean exportSurveyFields,
        boolean exportDataAccessGroups,
        boolean exportFiles
    ) throws JavaREDCapException {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.PROJECT_XML, errorHandler);

        //#---------------------------------------------
        //# Process the arguments
        //#---------------------------------------------
    	data.setReturnMetadataOnly(returnMetadataOnly);
        data.setRecords(recordIds);
        data.setFields(fields);
        data.setEvents(events, false);
        data.setFilterLogic(filterLogic);

        data.setExportSurveryFields(exportSurveyFields);
        data.setExportDataAccessGroups(exportDataAccessGroups);
        data.setExportFiles(exportFiles);

        //#---------------------------------------
        //# Get the Project XML and process it
        //#---------------------------------------
        String projectXml = connection.call(data);
        projectXml = processExportResult(projectXml);

        return projectXml;
    }

    /**
     * This method returns the next potential record ID for a project, but it does NOT
     * actually create a new record. The record ID returned will generally be the current maximum
     * record ID number incremented by one (but see the REDCap documentation for the case
     * where Data Access Groups are being used).
     * This method is intended for use with projects that have record-autonumbering enabled.
     *
     * @return The next record name.
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public String generateNextRecordName() throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.GENERATE_NEXT_RECORD_NAME, errorHandler);

        String nextRecordName = connection.call(data);
        nextRecordName = processExportResult(nextRecordName);

        return nextRecordName;
    }

    

    /**
     * Exports the specified records.
     *
     * Note: date ranges do not work for records that were imported at
     * the time the project was created.
     *
     * @param type The type of records exported:
     *     <ul>
     *       <li>'flat' - [default] exports one record per row.</li>
     *       <li>'eav'  - exports one data point per row:, so,
     *         for non-longitudinal studies, each record will have the following
     *         fields: record_id, field_name, value. For longitudinal studies, each record
     *         will have the fields: record_id, field_name, value, redcap_event_name.
     *       </li>
     *     </ul>
     * @param recordIds A set of record id's that are to be retrieved. If null
     * 		or empty all records will be retrieved.
     * @param fields A set of field names to exported. If null or empty all
     * 		fields will be returned. 
     * @param forms A set of form names for which fields should be exported.
     * 		If null or empty fields from all forms will be returned.
     * @param events A set of event names for which fields should be exported.
     * 		If null or empty data from all events will be returned.
     * @param filterLogic Logic used to restrict the records retrieved, e.g.,
     *      "[last_name] = 'Smith'".
     * @param rawOrLabel Indicates what should be exported for options of
     * 		multiple choice fields:
     *     <ul>
     *       <li> 'raw' - [default] export the raw coded values</li>
     *       <li> 'label' - export the labels</li>
     *     </ul>
     * @param rawOrLabelHeaders When exporting with 'csv' format 'flat' type,
     * 		indicates what format should be used for the CSV headers:
     *         <ul>
     *           <li> 'raw' - [default] export the variable/field names</li>
     *           <li> 'label' - export the field labels</li>
     *         </ul>
     * @param exportCheckboxLabel Specifies the format for checkbox fields for the case where
     *         format = 'csv', rawOrLabel = true, and type = 'flat'. For other cases this
     *         parameter is effectively ignored.
     *     <ul>
     *       <li> true - checked checkboxes will have a value equal to the checkbox option's label
     *           (e.g., 'Choice 1'), and unchecked checkboxes will have a blank value.
     *       </li>
     *       <li> false - checked checkboxes will have a value of 'Checked', and
     *            unchecked checkboxes will have a value of 'Unchecked'.
     *       </li>
     *     </ul>
     * @param exportSurveyFields Specifies whether survey fields should be exported.
     *     <ul>
     *       <li> true - export the following survey fields:
     *         <ul>
     *           <li> survey identifier field ('redcap_survey_identifier') </li>
     *           <li> survey timestamp fields (instrument+'_timestamp') </li>
     *         </ul>
     *       </li>
     *       <li> false - [default] survey fields are not exported.</li>
     *     </ul>
     * @param exportDataAccessGroups Specifies whether the data access group field
     *      ('redcap_data_access_group') should be exported.
     *     <ul>
     *       <li> true - export the data access group field if there is at least one data access group, and
     *                   the user calling the method (as identified by the API token) is not
     *                   in a data access group.</li>
     *       <li> false - don't export the data access group field.</li>
     *     </ul>
     * @param dateRangeBegin Specifies to return only those records have
     * 		been created or modified after the date entered. Date needs to be
     *      in YYYY_MM-DD HH:MM:SS, e.g., '2020-01-31 00:00:00'.
     * @param dateRangeEnd Specifies to return only those records have been
     * 		created or modified before the date entered. Date needs to be
     *      in YYYY_MM-DD HH:MM:SS, e.g., '2020-01-31 00:00:00'.
     * @param csvDelimiter Specifies what delimiter is used to separate values
     * 		in a CSV file (for CSV format only). Options are:
     *     <ul>
     *       <li> ',' - comma, this is the default </li>
     *       <li> 'tab' - tab </li>
     *       <li> ';' - semi-colon</li>
     *       <li> '|' - pipe</li>
     *       <li> '^' - caret</li>
     *     </ul>
     * @param decimalCharacter Specifies what decimal format to apply to
     * 		numeric values being returned. Options are:
     *     <ul>
     *       <li> '.' - dot/full stop </li>
     *       <li> ',' - comma </li>
     *       <li> null - numbers will be exported using the fields' native decimal format</li>
     *     </ul>
     *
     * @return A string with the data in the format specified. The format
     *     of the records depends on the 'type'parameter (see above). For other
     *     formats, a string is returned that contains the records in the
     *     specified format.
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public List<Map<String, Object>> exportRecords(
    		REDCapApiType type,
    		Set<String> recordIds,
    		Set<String> fields,
    		Set<String> forms,
    		Set<String> events,
    		String filterLogic,
    		REDCapApiRawOrLabel rawOrLabel, // = 'raw',
    		REDCapApiRawOrLabel rawOrLabelHeaders, // = 'raw',
    		boolean exportCheckboxLabel,
    		boolean exportSurveyFields,
    		boolean exportDataAccessGroups,
    		String dateRangeBegin,
    		String dateRangeEnd,
    		REDCapApiCsvDelimiter csvDelimiter, // = ',',
    		REDCapApiDecimalCharacter decimalCharacter
    		) throws JavaREDCapException
    {
    	String result = exportRecords(
    			REDCapApiFormat.JSON,
    			type,
    			recordIds,
    			fields,
    			forms,
    			events,
    			filterLogic,
    			rawOrLabel,
    			rawOrLabelHeaders,
    			exportCheckboxLabel,
    			exportSurveyFields,
    			exportDataAccessGroups,
    			dateRangeBegin,
    			dateRangeEnd,
                csvDelimiter,
                decimalCharacter
            );

    	List<Map<String, Object>> nativeResult = null;
		try {
			nativeResult = new ObjectMapper().readValue(result, new TypeReference<List<Map<String, Object>>>(){});
		} catch (JsonProcessingException e) {
			throw new JavaREDCapException ("Exception processing REDCap response", ErrorHandlerInterface.JSON_ERROR, e);
		}

		return nativeResult;
    }

    /**
     * Exports the specified records.
     *
     * Note: date ranges do not work for records that were imported at
     * the time the project was created.
     *
     * @param format The format in which to export the records:
     *     <ul>
     *       <li> 'csv' - string of CSV (comma-separated values)</li>
     *       <li> 'json' - string of JSON encoded values</li>
     *       <li> 'xml' - string of XML encoded data</li>
     *       <li> 'odm' - string with CDISC ODM XML format, specifically ODM version 1.3.1</li>
     *     </ul>
     * @param type The type of records exported:
     *     <ul>
     *       <li>'flat' - [default] exports one record per row.</li>
     *       <li>'eav'  - exports one data point per row:, so,
     *         for non-longitudinal studies, each record will have the following
     *         fields: record_id, field_name, value. For longitudinal studies, each record
     *         will have the fields: record_id, field_name, value, redcap_event_name.
     *       </li>
     *     </ul>
     * @param recordIds A set of record id's that are to be retrieved. If null
     * 		or empty all records will be retrieved.
     * @param fields A set of field names to exported. If null or empty all
     * 		fields will be returned. 
     * @param forms A set of form names for which fields should be exported.
     * 		If null or empty fields from all forms will be returned.
     * @param events A set of event names for which fields should be exported.
     * 		If null or empty data from all events will be returned.
     * @param filterLogic Logic used to restrict the records retrieved, e.g.,
     *      "[last_name] = 'Smith'".
     * @param rawOrLabel Indicates what should be exported for options of
     * 		multiple choice fields:
     *     <ul>
     *       <li> 'raw' - [default] export the raw coded values</li>
     *       <li> 'label' - export the labels</li>
     *     </ul>
     * @param rawOrLabelHeaders When exporting with 'csv' format 'flat' type,
     * 		indicates what format should be used for the CSV headers:
     *         <ul>
     *           <li> 'raw' - [default] export the variable/field names</li>
     *           <li> 'label' - export the field labels</li>
     *         </ul>
     * @param exportCheckboxLabel Specifies the format for checkbox fields for the case where
     *         format = 'csv', rawOrLabel = true, and type = 'flat'. For other cases this
     *         parameter is effectively ignored.
     *     <ul>
     *       <li> true - checked checkboxes will have a value equal to the checkbox option's label
     *           (e.g., 'Choice 1'), and unchecked checkboxes will have a blank value.
     *       </li>
     *       <li> false - checked checkboxes will have a value of 'Checked', and
     *            unchecked checkboxes will have a value of 'Unchecked'.
     *       </li>
     *     </ul>
     * @param exportSurveyFields Specifies whether survey fields should be exported.
     *     <ul>
     *       <li> true - export the following survey fields:
     *         <ul>
     *           <li> survey identifier field ('redcap_survey_identifier') </li>
     *           <li> survey timestamp fields (instrument+'_timestamp') </li>
     *         </ul>
     *       </li>
     *       <li> false - [default] survey fields are not exported.</li>
     *     </ul>
     * @param exportDataAccessGroups Specifies whether the data access group field
     *      ('redcap_data_access_group') should be exported.
     *     <ul>
     *       <li> true - export the data access group field if there is at least one data access group, and
     *                   the user calling the method (as identified by the API token) is not
     *                   in a data access group.</li>
     *       <li> false - don't export the data access group field.</li>
     *     </ul>
     * @param dateRangeBegin Specifies to return only those records have
     * 		been created or modified after the date entered. Date needs to be
     *      in YYYY_MM-DD HH:MM:SS, e.g., '2020-01-31 00:00:00'.
     * @param dateRangeEnd Specifies to return only those records have been
     * 		created or modified before the date entered. Date needs to be
     *      in YYYY_MM-DD HH:MM:SS, e.g., '2020-01-31 00:00:00'.
     * @param csvDelimiter Specifies what delimiter is used to separate values
     * 		in a CSV file (for CSV format only). Options are:
     *     <ul>
     *       <li> ',' - comma, this is the default </li>
     *       <li> 'tab' - tab </li>
     *       <li> ';' - semi-colon</li>
     *       <li> '|' - pipe</li>
     *       <li> '^' - caret</li>
     *     </ul>
     * @param decimalCharacter Specifies what decimal format to apply to
     * 		numeric values being returned. Options are:
     *     <ul>
     *       <li> '.' - dot/full stop </li>
     *       <li> ',' - comma </li>
     *       <li> null - numbers will be exported using the fields' native decimal format</li>
     *     </ul>
     *
     * @return A string with the data in the format specified. The format
     *     of the records depends on the 'type'parameter (see above). For other
     *     formats, a string is returned that contains the records in the
     *     specified format.
     *     
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public String exportRecords(
        REDCapApiFormat format,
        REDCapApiType type,
        Set<String> recordIds,
        Set<String> fields,
        Set<String> forms,
        Set<String> events,
        String filterLogic,
        REDCapApiRawOrLabel rawOrLabel, // = 'raw',
        REDCapApiRawOrLabel rawOrLabelHeaders, // = 'raw',
        boolean exportCheckboxLabel,
        boolean exportSurveyFields,
        boolean exportDataAccessGroups,
        String dateRangeBegin,
        String dateRangeEnd,
        REDCapApiCsvDelimiter csvDelimiter, // = ',',
        REDCapApiDecimalCharacter decimalCharacter
    ) throws JavaREDCapException {
    	if (null == csvDelimiter) {csvDelimiter = REDCapApiCsvDelimiter.COMMA; }

    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.RECORD, errorHandler);

    	data.addFormat(REDCapApiFormat.ODM);

    	//#---------------------------------------
        //# Process the arguments
        //#---------------------------------------
        data.setFormat(format);
        data.setType(type);
        data.setRecords(recordIds);
        data.setFields(fields);
        data.setForms(forms);
        data.setEvents(events, false);

        data.setRawOrLabel(rawOrLabel);
        data.setRawOrLabelHeaders(rawOrLabelHeaders);
        data.setExportCheckboxLabel(exportCheckboxLabel);
        data.setExportSurveryFields(exportSurveyFields);
        data.setExportDataAccessGroups(exportDataAccessGroups);

        data.setFilterLogic(filterLogic);

        data.setDateRangeBegin(dateRangeBegin);
        data.setDateRangeEnd(dateRangeEnd);

        if (REDCapApiFormat.CSV.equals(format)) {
        	data.setCsvDelimiter(csvDelimiter);
        };

        data.setDecimalCharacter(decimalCharacter);

        //#---------------------------------------
        //# Get the records and process them
        //#---------------------------------------
        String records = connection.call(data);
        records = processExportResult(records);

        return records;
    }


    /**
     * Imports the specified records into the project.
     *
     * @param records A string containing the data in the format specified.
     * @param format One of the following formats can be specified
     *            <ul>
     *              <li> 'csv' - string of CSV (comma-separated values)</li>
     *              <li> 'json' - string of JSON encoded values</li>
     *              <li> 'xml' - string of XML encoded data</li>
     *              <li> 'odm' - CDISC ODM XML format, specifically ODM version 1.3.1</li>
     *            </ul>
     * @param type
     *            <ul>
     *              <li> 'flat' - [default] each data element is a record</li>
     *              <li> 'eav' - each data element is one value</li>
     *            </ul>
     * @param overwriteBehavior
     *            <ul>
     *              <li>normal - [default] blank/empty values will be ignored</li>
     *              <li>overwrite - blank/empty values are valid and will overwrite data</li>
     *            </ul>
     * @param dateFormat date format which can be one of the following:
     *            <ul>
     *              <li>'YMD' - [default] Y-M-D format (e.g., 2016-12-31)</li>
     *              <li>'MDY' - M/D/Y format (e.g., 12/31/2016)</li>
     *              <li>'DMY' - D/M/Y format (e.g., 31/12/2016)</li>
     *           </ul>
     * @param returnContent specifies what should be returned:
     *           <ul>
     *             <li>'count' - [default] the number of records imported</li>
     *             <li>'ids' - an array of the record IDs imported is returned</li>
     *             <li>'auto_ids' - an array of comma-separated record ID pairs, with
     *                 the new ID created and the corresponding ID that
     *                 was sent, for the records that were imported.
     *                 This can only be used if $forceAutoNumber is set to true.</li>
     *           </ul>
     * @param forceAutoNumber enables automatic assignment of record IDs of imported
     *         records by REDCap.
     *         If this is set to true, and auto-numbering for records is enabled for the project,
     *         auto-numbering of imported records will be enabled.
     *
     * @param csvDelimiter specifies which delimiter separates the values in the CSV
     *         data file (for CSV format only).
     *         <ul>
     *           <li>RedCapApiCsvDelimiter.COMMA - comma [default] </li>
     *           <li>RedCapApiCsvDelimiter.TAB </li>
     *           <li>RedCapApiCsvDelimiter.SEMICOLON - semi-colon </li>
     *           <li>RedCapApiCsvDelimiter.PIPE - pipe </li>
     *           <li>RedCapApiCsvDelimiter.CARET - caret </li>
     *         </ul>
     *
     * @return mixed if 'count' was specified for 'returnContent', then an integer will
     *         be returned that is the number of records imported.
     *         If 'ids' was specified, then an array of record IDs that were imported will
     *         be returned. If 'auto_ids' was specified, an array that maps newly created IDs
     *         to sent IDs will be returned.
     *         
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public String importRecords(
    		String records,
    		REDCapApiFormat format,
    		REDCapApiType type,
    		REDCapApiOverwriteBehavior overwriteBehavior, // = 'normal',
    		REDCapApiDateFormat dateFormat, // = 'YMD',
    		REDCapApiReturnContent returnContent, // = 'count',
    		boolean forceAutoNumber, // = false,
    		REDCapApiCsvDelimiter csvDelimiter // = ','
    		) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.RECORD, errorHandler);

    	data.addFormat(REDCapApiFormat.ODM);

        //#---------------------------------------
        //# Process format
        //#---------------------------------------
        data.setFormat(format);
        if (REDCapApiFormat.CSV.equals(format)) {
        	data.setCsvDelimiter(csvDelimiter);
        }
        data.setType(type);

        data.setOverwriteBehavior(overwriteBehavior);
        data.setForceAutoNumber(forceAutoNumber);
        data.setReturnContent(returnContent, forceAutoNumber);
        data.setDateFormat(dateFormat);

        data.setData(processImportDataArgument(records, "records", format));

        String result = connection.call(data);

        processNonExportResult(result);


        /*
         * TODO:
        #--------------------------------------------------------------------------
        # Process result, which should either be a count of the records imported,
        # or a list of the record IDs that were imported
        #
        # The result should be a string in JSON for all formats.
        # Need to convert the result to a PHP data structure.
        #--------------------------------------------------------------------------
        $phpResult = json_decode($result, true); // true => return as array instead of object

        $jsonError = json_last_error();

        switch ($jsonError) {
            case JSON_ERROR_NONE:
                $result = $phpResult;
                # If this is a count, then just return the count, and not an
                # array that has a count index with the count
                if (isset($result) && is_array($result) && array_key_exists('count', $result)) {
                    $result = $result['count'];
                }
                break;
            default:
                # Hopefully the REDCap API will always return valid JSON, and this
                # will never happen.
                $message =  'JSON error ('.$jsonError.') "'.json_last_error_msg().
                    '" while processing import return value: "'.
                $result.'".';
                $this->errorHandler->throwException($message, ErrorHandlerInterface::JSON_ERROR);
                break; // @codeCoverageIgnore
        }
         */

        return result;
    }

    
    /**
     * Deletes the specified records from the project.
     *
     * @param recordIds A set for record IDs to delete
     * @param arm If an arm is specified, only records that have
     *     one of the specified record IDs that are in that arm will
     *     be deleted.
     *
     * @return integer the number of records deleted. Note that as of
     *     REDCap version 7.0.15 (at least) the number of records
     *     deleted will not be correct for the case where an arm
     *     is specified and some of the record IDs specified are
     *     not in that arm.
     *
     * @throws JavaREDCapException Thrown if an error occurs.
     */
    public int deleteRecords(Set<String> recordIds, Integer arm) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.RECORD, errorHandler);
    	data.setAction(REDCapApiAction.DELETE);

    	data.setRecords(recordIds);
    	data.setArm(arm);

        String result = connection.call(data);

        processNonExportResult(result);

        return Integer.valueOf(result);
    }


    /**
     * Exports the repeating instruments and events.
     *
     * @return A List for Map entries.<br>
     * 		For classic (non-longitudinal) studies, the 'form name' and
     * 		'custom form label' will be returned for each repeating form.<br>
     * 		Longitudinal studies additionally return the 'event name'. For
     * 		repeating events in longitudinal studies, a blank value will be
     * 		returned for the form_name. In all cases, a blank value will be
     * 		returned for the 'custom form label' if it is not defined.
     * 
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public List<Map<String, Object>> exportRepeatingInstrumentsAndEvents() throws JavaREDCapException
    {
    	String result = exportRepeatingInstrumentsAndEvents(REDCapApiFormat.JSON);

		List<Map<String, Object>> nativeResult = null;
		try {
			nativeResult = new ObjectMapper().readValue(result, new TypeReference<List<Map<String, Object>>>(){});
		} catch (JsonProcessingException e) {
			throw new JavaREDCapException ("Exception processing REDCap response", ErrorHandlerInterface.JSON_ERROR, e);
		}

		return nativeResult;
    }

    /**
     * Exports the repeating instruments and events.
     *
     * @param format The format in which to export the records:
     *     <ul>
     *       <li>REDCapApiFormat.CSV - string of CSV (comma-separated values)</li>
     *       <li>REDCapApiFormat.JSON - string of JSON encoded values</li>
     *       <li>REDCapApiFormat.XML - string of XML encoded data</li>
     *       <li>REDCapApiFormat.ODM - string with CDISC ODM XML format, specifically ODM version 1.3.1</li>
     *     </ul>
     *
     * @return A string with the data in the specified format.<br>
     * 		For classic (non-longitudinal) studies, the 'form name' and
     *		'custom form label' will be returned for each repeating form.<br>
     *		Longitudinal studies additionally return the 'event name'. For
     *		repeating events in longitudinal studies, a blank value will be
     *		returned for the form_name. In all cases, a blank value will be
     *		returned for the 'custom form label' if it is not defined.
     *
     * @throws JavaREDCapException Thrown if an error occurs. 
     */
    public String exportRepeatingInstrumentsAndEvents(REDCapApiFormat format) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.REPEATING_FORMS_EVENTS, errorHandler);
    	data.addFormat(REDCapApiFormat.ODM);

        //#---------------------------------------
        //# Process the arguments
        //#---------------------------------------
    	data.setFormat(format);

        String result = connection.call(data);

        processExportResult(result);

        return result;
    }


    /**
     * Imports the repeating instruments and events.
     *
     * @param formsEvents A List of Map entries for the data to be imported.
     *
     * @return The number of repeated instruments or repeated events imported.
     * 
     * @throws JavaREDCapException Thrown if an error occurs. 
     */
    public int importRepeatingInstrumentsAndEvents(List<Map<String, Object>> formsEvents) throws JavaREDCapException
    {
    	int result = 0;

    	try {
    		// Convert to JSON and send it on.
    		String json = new ObjectMapper().writeValueAsString(formsEvents);

    		result = importRepeatingInstrumentsAndEvents(json, REDCapApiFormat.JSON);
    	}
    	catch (JsonProcessingException e) {
    		throw new JavaREDCapException("Exception preparing REDCap request", ErrorHandlerInterface.JSON_ERROR, e);
    	}

    	return result;
    }

    /**
     * Imports the repeating instruments and events.
     *
     * @param formsEvents A string containing the data in the format specified.
     * @param format The format in which to export the records:
     *     <ul>
     *       <li>REDCapApiFormat.CSV - string of CSV (comma-separated values)</li>
     *       <li>REDCapApiFormat.JSON - string of JSON encoded values</li>
     *       <li>REDCapApiFormat.XML - string of XML encoded data</li>
     *     </ul>
     *
     * @return The number of repeated instruments or repeated events imported.
     * 
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public int importRepeatingInstrumentsAndEvents(String formsEvents, REDCapApiFormat format) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.REPEATING_FORMS_EVENTS, errorHandler);

        //#---------------------------------------
        //# Process the arguments
        //#---------------------------------------
    	data.setFormat(format);
    	data.setData(processImportDataArgument(
            formsEvents,
            "repeating instruments/events",
            format
        ) );

        //#---------------------------------------
        //# Process the data
        //#---------------------------------------
        String result = connection.call(data);

        processNonExportResult(result);

        return Integer.valueOf(result);
    }


    /**
     * Gets the REDCap version number of the REDCap instance being used by the project.
     *
     * @return string the REDCap version number of the REDCap instance being used by the project.
     * 
     * @throws JavaREDCapException Thrown if an error occurs. 
     */
    public String exportRedcapVersion() throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.VERSION, errorHandler);

        String redcapVersion = connection.call(data);
        redcapVersion = processExportResult(redcapVersion);

        return redcapVersion;
    }

    /**
     * Exports the records produced by the specified report.
     *
     * @param reportId An number of the report to use.
     * @param format Output data format.
     *     <ul>
     *       <li>REDCapApiFormat.CSV - string of CSV (comma-separated values)</li>
     *       <li>REDCapApiFormat.JSON - string of JSON encoded values</li>
     *       <li>REDCapApiFormat.XML - string of XML encoded data</li>
     *     </ul>
     * @param rawOrLabel Indicates what should be exported for options of multiple choice fields:
     *     <ul>
     *       <li>REDCapApiRawOrLabel.RAW - [default] export the raw coded values</li>
     *       <li>REDCapApiRawOrLabel.LABEL - export the labels</li>
     *     </ul>
     * @param rawOrLabelHeaders When exporting with 'csv' format 'flat' type, indicates what format
     *         should be used for the CSV headers:
     *         <ul>
     *           <li>REDCapApiRawOrLabel.RAW - [default] export the variable/field names</li>
     *           <li>REDCapApiRawOrLabel.LABEL - export the field labels</li>
     *         </ul>
     * @param exportCheckboxLabel Specifies the format for checkbox fields for the case where
     *         format = 'csv', rawOrLabel = true, and type = 'flat'. For other cases this
     *         parameter is effectively ignored.
     *     <ul>
     *       <li> true - checked checkboxes will have a value equal to the checkbox option's label
     *           (e.g., 'Choice 1'), and unchecked checkboxes will have a blank value.
     *       </li>
     *       <li> false - [default] checked checkboxes will have a value of 'Checked', and
     *            unchecked checkboxes will have a value of 'Unchecked'.
     *       </li>
     *     </ul>
     * @param csvDelimiter Specifies what delimiter is used to separate
     *     values in a CSV file (for CSV format only). Options are:
     *     <ul>
     *       <li>RedCapApiCsvDelimiter.COMMA - comma, this is the default </li>
     *       <li>RedCapApiCsvDelimiter.TAB - tab </li>
     *       <li>RedCapApiCsvDelimiter.SEMICOLON - semi-colon</li>
     *       <li>RedCapApiCsvDelimiter.PIPE - pipe</li>
     *       <li>RedCapApiCsvDelimiter.CARET - caret</li>
     *     </ul>
     * @param decimalCharacter Specifies what decimal format to apply to
     *		numeric values being returned. Options are:
     *     <ul>
     *       <li>REDCapApiDecimalCharacter.PERIOD - dot/full stop </li>
     *       <li>REDCapApiDecimalCharacter.COMMA - comma </li>
     *       <li>null - numbers will be exported using the fields' native decimal format</li>
     *     </ul>
     *
     * @return A string containing the report in the specified format.
     * 
     * @throws JavaREDCapException Thrown if an error occurs. 
     */
    public String exportReports(
    		Integer reportId,
    		REDCapApiFormat format,
    		REDCapApiRawOrLabel rawOrLabel, // = 'raw',
    		REDCapApiRawOrLabel rawOrLabelHeaders, // = 'raw',
    		boolean exportCheckboxLabel, // = false,
    		REDCapApiCsvDelimiter csvDelimiter, // = ',',
    		REDCapApiDecimalCharacter decimalCharacter
    		) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.REPORT, errorHandler);

        //#------------------------------------------------
        //# Process arguments
        //#------------------------------------------------
    	data.setReportId(reportId);

        data.setFormat(format);
        data.setRawOrLabel(rawOrLabel);
        data.setRawOrLabelHeaders(rawOrLabelHeaders);
        data.setExportCheckboxLabel(exportCheckboxLabel);

        if (REDCapApiFormat.CSV.equals(format)) {
        	data.setCsvDelimiter(csvDelimiter);
        }
        data.setDecimalCharacter(decimalCharacter);

        //#---------------------------------------------------
        //# Get and process records
        //#---------------------------------------------------
        String records = connection.call(data);
        records = processExportResult(records);

        return records;
    }


    /**
     * Exports the survey link for the specified inputs.
     *
     * @param recordId The record ID for the link.
     * @param form The form for the link.
     * @param event Event for link (for longitudinal studies only).
     * @param repeatInstance Repeat instance number for repeatable form,
     * 		the instance of the form to return a link for.
     *
     * @return A survey link.
     * 
     * @throws JavaREDCapException Thrown if an error occurs. 
     */
    public String exportSurveyLink(String recordId, String form, String event, Integer repeatInstance) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.SURVEY_LINK, errorHandler);

        //#----------------------------------------------
        //# Process arguments
        //#----------------------------------------------
    	data.setRecord(recordId, true);
    	data.setInstrument(form,  true);
    	data.setEvent(event);
    	data.setRepeatInstance(repeatInstance);

        String surveyLink = connection.call(data);
        surveyLink = processExportResult(surveyLink);

        return surveyLink;
    }

    /**
     * Exports the list of survey participants for the specified form and, for
     * longitudinal studies, event.
     *
     * @param form The form for which the participants should be exported.
     * @param event The event name for which survey participants should be
     *     exported if a longitudial project.
     *
     * @return A List of Map entries.
     * 
     * @throws JavaREDCapException Thrown if an error occurs. 
     */
    public List<Map<String, Object>> exportSurveyParticipants(String form, String event) throws JavaREDCapException
    {
    	String result = exportSurveyParticipants(form, REDCapApiFormat.JSON, event);

		List<Map<String, Object>> nativeResult = null;
		try {
			nativeResult = new ObjectMapper().readValue(result, new TypeReference<List<Map<String, Object>>>(){});
		} catch (JsonProcessingException e) {
			throw new JavaREDCapException ("Exception processing REDCap response", ErrorHandlerInterface.JSON_ERROR, e);
		}

		return nativeResult;
    }

    /**
     * Exports the list of survey participants for the specified form and, for
     * longitudinal studies, event.
     *
     * @param form The form for which the participants should be exported.
     * @param format Output data format.
     *     <ul>
     *       <li>REDCapApiFormat.CSV - string of CSV (comma-separated values)</li>
     *       <li>REDCapApiFormat.JSON - string of JSON encoded values</li>
     *       <li>REDCapApiFormat.XML - string of XML encoded data</li>
     *     </ul>
     * @param event The event name for which survey participants should be
     *     exported if a longitudial project.
     *
     * @return A string containing the exported data in the specified format.
     * 
     * @throws JavaREDCapException Thrown if an error occurs.
     */
    public String exportSurveyParticipants(String form, REDCapApiFormat format, String event) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.PARTICIPANT_LIST, errorHandler);

        //#----------------------------------------------
        //# Process arguments
        //#----------------------------------------------
        data.setFormat(format);
        data.setInstrument(form, true);
        data.setEvent(event);

        String surveyParticipants = connection.call(data);
        surveyParticipants = processExportResult(surveyParticipants);

        return surveyParticipants;
    }

    /**
     * Exports the survey queue link for the specified record ID.
     *
     * @param recordId the record ID of the survey queue link that should be returned.
     *
     * @return string survey queue link.
     * 
     * @throws JavaREDCapException Thrown if an error occurs.
     */
    public String exportSurveyQueueLink(String recordId) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.SURVEY_QUEUE_LINK, errorHandler);

        //#----------------------------------------------
        //# Process arguments
        //#----------------------------------------------
        data.setRecord(recordId, true);

        String surveyQueueLink = connection.call(data);
        surveyQueueLink = processExportResult(surveyQueueLink);

        return surveyQueueLink;
    }

    /**
     * Exports the code for returning to a survey that was not completed.
     *
     * @param recordId The record ID for the survey to return to.
     * @param form The form name of the survey to return to.
     * @param event The unique event name (for longitudinal studies) for the
     * 		survey to return to.
     * @param repeatInstance The repeat instance (if any) for the survey to return to.
     * 
     * @return The survey return code.
     * 
     * @throws JavaREDCapException Thrown if an error occurs. 
     */
    public String exportSurveyReturnCode(String recordId, String form, String event, Integer repeatInstance) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.SURVEY_RETURN_CODE, errorHandler);

        //#----------------------------------------------
        //# Process arguments
        //#----------------------------------------------
    	data.setRecord(recordId, true);
        data.setInstrument(form, true);
        data.setEvent(event);
        data.setRepeatInstance(repeatInstance);

        String surveyReturnCode = connection.call(data);
        surveyReturnCode = processExportResult(surveyReturnCode);

        return surveyReturnCode;
    }

    
    /**
     * Exports the users of the project.
     *
     * @return A List of Map entries for each user.
     * 
     * @throws JavaREDCapException Thrown if an error occurs 
     */
    public List<Map<String, Object>> exportUsers() throws JavaREDCapException
    {
    	String result = exportUsers(REDCapApiFormat.JSON);

		List<Map<String, Object>> nativeResult = null;
		try {
			nativeResult = new ObjectMapper().readValue(result, new TypeReference<List<Map<String, Object>>>(){});
		} catch (JsonProcessingException e) {
			throw new JavaREDCapException ("Exception processing REDCap response", ErrorHandlerInterface.JSON_ERROR, e);
		}

		return nativeResult;
    }

    /**
     * Exports the users of the project.
     *
     * @param format Output data format.
     *     <ul>
     *       <li>REDCapApiFormat.CSV - string of CSV (comma-separated values)</li>
     *       <li>REDCapApiFormat.JSON - string of JSON encoded values</li>
     *       <li>REDCapApiFormat.XML - string of XML encoded data</li>
     *     </ul>
     *
     * @return A string containing the user information in the requested format.
     * 
     * @throws JavaREDCapException Thrown if an error occurs.
     */
    public String exportUsers(REDCapApiFormat format) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.USER, errorHandler);

    	data.setFormat(format);

        //#---------------------------------------------------
        //# Get and process users
        //#---------------------------------------------------
        String users = connection.call(data);
        users = processExportResult(users);

        return users;
    }

    /**
     * Imports the specified users into the project. This method can also be
     * used to update user privileges by importing a users that already exist
     * in the project and specifying new privileges for that user in the user
     * data that is imported.
     *<p>
     * The available field names for user import are:
     * <pre>
     * <code class="java">
     * username, expiration, data_access_group, design,
     * user_rights, data_access_groups, data_export, reports, stats_and_charts,
     * manage_survey_participants, calendar, data_import_tool, data_comparison_tool,
     * logging, file_repository, data_quality_create, data_quality_execute,
     * api_export, api_import, mobile_app, mobile_app_download_data,
     * record_create, record_rename, record_delete,
     * lock_records_customization, lock_records, lock_records_all_forms,
     * forms
     * </code>
     * </pre>
     *
     * Privileges for fields above can be set as follows:
     * <ul>
     *   <li><b>Data Export:</b> 0=No Access, 2=De-Identified, 1=Full Data Set</li>
     *   <li><b>Form Rights:</b> 0=No Access, 2=Read Only,
     *       1=View records/responses and edit records (survey responses are read-only),
     *       3=Edit survey responses</li>
     *   <li><b>Other field values:</b> 0=No Access, 1=Access.</li>
     * </ul>
     *
     * See the REDCap API documentation for more information, or the results
     * of the exportUsers method to see what the data looks like for the current users.
     *
     * @param users A List of Map entries containing the field names and values.
     *
     * @return The number of users added or updated.
     * 
     * @throws JavaREDCapException Thrown if an error occurs. 
     */
    public int importUsers(List<Map<String, Object>> users) throws JavaREDCapException
    {
    	int result = 0;

    	try {
    		// Convert to JSON and send it on.
    		String json = new ObjectMapper().writeValueAsString(users);

    		result = importUsers(json, REDCapApiFormat.JSON);
    	}
    	catch (JsonProcessingException e) {
    		throw new JavaREDCapException("Expection preparing REDCap request", ErrorHandlerInterface.JSON_ERROR, e);
    	}

    	return result;
    }

    /**
     * Imports the specified users into the project. This method can also be
     * used to update user privileges by importing a users that already exist
     * in the project and specifying new privileges for that user in the user
     * data that is imported.
     *<p>
     * The available field names for user import are:
     * <pre>
     * <code class="java">
     * username, expiration, data_access_group, design,
     * user_rights, data_access_groups, data_export, reports, stats_and_charts,
     * manage_survey_participants, calendar, data_import_tool, data_comparison_tool,
     * logging, file_repository, data_quality_create, data_quality_execute,
     * api_export, api_import, mobile_app, mobile_app_download_data,
     * record_create, record_rename, record_delete,
     * lock_records_customization, lock_records, lock_records_all_forms,
     * forms
     * </code>
     * </pre>
     *
     * Privileges for fields above can be set as follows:
     * <ul>
     *   <li><b>Data Export:</b> 0=No Access, 2=De-Identified, 1=Full Data Set</li>
     *   <li><b>Form Rights:</b> 0=No Access, 2=Read Only,
     *       1=View records/responses and edit records (survey responses are read-only),
     *       3=Edit survey responses</li>
     *   <li><b>Other field values:</b> 0=No Access, 1=Access.</li>
     * </ul>
     *
     * See the REDCap API documentation for more information, or the results
     * of the exportUsers method to see what the data looks like for the current users.
     *
     * @param users A string containing the user data in the format specified.
     * @param format The format of the input data.
     *     <ul>
     *       <li>REDCapApiFormat.CSV - string of CSV (comma-separated values)</li>
     *       <li>REDCapApiFormat.JSON - string of JSON encoded values</li>
     *       <li>REDCapApiFormat.XML - string of XML encoded data</li>
     *     </ul>
     *
     * @return The number of users added or updated.
     * 
     * @throws JavaREDCapException Thrown if an error occurs. 
     */
    public int importUsers(String users, REDCapApiFormat format) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.USER, errorHandler);

        //#----------------------------------------------------
        //# Process arguments
        //#----------------------------------------------------
        data.setFormat(format);
        data.setData(processImportDataArgument(users, "users", format));

        //#---------------------------------------------------
        //# Get and process users
        //#---------------------------------------------------
        String result = connection.call(data);
        processNonExportResult(result);

        return Integer.valueOf(result);
    }

    /**
     * Exports the Data Access Groups for a project.
     *
     * @return A list of Map entries for each DAG with the map having the
     * 		following keys:
     *     <ul>
     *       <li>'data_access_group_name'</li>
     *      <li>'unique_group_name'</li>
     *     </ul>
     *     
     * @throws JavaREDCapException Thrown if an error occurs. 
     */
    public List<Map<String, String>> exportDags() throws JavaREDCapException
    {
    	String result = exportDags(REDCapApiFormat.JSON);

		List<Map<String, String>> nativeResult = null;
		try {
			nativeResult = new ObjectMapper().readValue(result, new TypeReference<List<Map<String, String>>>(){});
		} catch (JsonProcessingException e) {
			throw new JavaREDCapException ("Exception processing REDCap response", ErrorHandlerInterface.JSON_ERROR, e);
		}

		return nativeResult;
    }

    /**
     * Exports the Data Access Groups for a project.
     *
     * @param format The format used to export the data.
     *     <ul>
     *       <li>REDCapApiFormat.CSV - string of CSV (comma-separated values)</li>
     *       <li>REDCapApiFormat.JSON - string of JSON encoded values</li>
     *       <li>REDCapApiFormat.XML - string of XML encoded data</li>
     *     </ul>
     *
     * @return A string containing the DAG information in the requested format.
     * 
     * @throws JavaREDCapException Thrown if an error occurs.
     */
    public String exportDags(REDCapApiFormat format) throws JavaREDCapException
    {
	   REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.DAG, errorHandler);

       data.setFormat(format);

       String dags = connection.call(data);
       dags = processExportResult(dags);

       return dags;
   }

    /**
     * Imports the specified dags into the project. Allows import of new DAGs
	 * or update of the data_access_group_name of any existing DAGs. DAGs can
	 * be renamed by changing the data_access_group_name. A DAG can be created
	 * by providing group name value with unique group name set to blank.
	 *
	 * @param dags A list of Map entries of the DAGs to import. The field names
	 * 		(keys) are: data_access_group_name, unique_group_name
	 *
	 * @return The number of DAGs imported.
	 *     
	 * @throws JavaREDCapException if an error occurs.
	 */
    public int importDags(List<Map<String, String>> dags) throws JavaREDCapException
    {
    	int result = 0;

    	try {
    		// Convert to JSON and send it on.
    		String json = new ObjectMapper().writeValueAsString(dags);

    		result = importDags(json, REDCapApiFormat.JSON);
    	}
    	catch (JsonProcessingException e) {
    		throw new JavaREDCapException("Exception preparing REDCap request", ErrorHandlerInterface.JSON_ERROR, e);
    	}

    	return result;
    }

    /**
    * Imports the specified DAGs into the project. Allows import of new DAGs
    * or update of the data_access_group_name of any existing DAGs. DAGs can
    * be renamed by changing the data_access_group_name. A DAG can be created
    * by providing group name value with unique group name set to blank.
    *
    * @param dags A list of Map entries of the DAGs to import. The field names
    * 		(keys) are: data_access_group_name, unique_group_name
    * @param format The format for the import.
    *     <ul>
    *       <li>REDCapApiFormat.CSV - string of CSV (comma-separated values)</li>
    *       <li>REDCapApiFormat.JSON - string of JSON encoded values</li>
    *       <li>REDCapApiFormat.XML - string of XML encoded data</li>
    *     </ul>
    *
    * @return The number of DAGs imported.
    *     
    * @throws JavaREDCapException if an error occurs.
    */
    public int importDags(String dags, REDCapApiFormat format) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.DAG, errorHandler);
    	data.setAction(REDCapApiAction.IMPORT);

    	//#---------------------------------------
    	//# Process arguments
    	//#---------------------------------------
    	data.setFormat(format);
    	data.setData(processImportDataArgument(dags, "dag", format));

    	String result = connection.call(data);

    	processNonExportResult(result);

    	return Integer.valueOf(result);
    }

    /**
    * Deletes the specified DAGs from the project.
    *
    * @param dags A set of the unique_group_names to delete.
    *
    * @return The number of DAGs imported.
    * 
    * @throws JavaREDCapException if an error occurs.
    */
    public int deleteDags(Set<String> dags) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.DAG, errorHandler);
    	data.setAction(REDCapApiAction.DELETE);
    	data.setDags(dags, true);

    	String result = connection.call(data);
    	processNonExportResult(result);

    	return Integer.valueOf(result);
    }

    /**
     * Exports the User-DataAccessGroup assignments for a project.
     *
     * @return A List of Map entries for each mapping with the following keys:
     *     <ul>
     *       <li>'username'</li>
     *       <li>'redcap_data_access_group'</li>
     *     </ul>
     *
     * @throws JavaREDCapException Thrown if an error occurs.
     */
    public List<Map<String, String>> exportUserDagAssignment() throws JavaREDCapException
    {
    	String result = exportUserDagAssignment(REDCapApiFormat.JSON);

		List<Map<String, String>> nativeResult = null;
		try {
			nativeResult = new ObjectMapper().readValue(result, new TypeReference<List<Map<String, String>>>(){});
		} catch (JsonProcessingException e) {
			throw new JavaREDCapException ("Exception processing REDCap response", ErrorHandlerInterface.JSON_ERROR, e);
		}

		return nativeResult;
    }

    /**
    * Exports the User-DataAccessGroup assignments for a project.
    *
    * @param format The format used to export the data.
    *     <ul>
    *       <li>REDCapApiFormat.CSV - string of CSV (comma-separated values)</li>
    *       <li>REDCapApiFormat.JSON - string of JSON encoded values</li>
    *       <li>REDCapApiFormat.XML - string of XML encoded data</li>
    *     </ul>
    *
    * @return A string with the mappings in the format requested.
    * 
    * @throws JavaREDCapException Thrown if an error occurs
    */
    public String exportUserDagAssignment(REDCapApiFormat format) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.USER_DAG_MAPPING, errorHandler);
    	data.setFormat(format);

    	String dagAssignments = connection.call(data);
    	dagAssignments = processExportResult(dagAssignments);

    	return dagAssignments;
    }

    /**
     * Imports User-DAG assignments, allowing you to assign users to any
     * data access group.o the project. If you wish to modify an existing
     * mapping, you must provide its unique username and group name.
     * If the 'redcap_data_access_group' column is not provided, user
     * will not be assigned to any group. There should be only one record
     * per username.
     *
     * @param dagAssignments The User-DAG assignments to import.<br>
     * The field names (keys) used in both cases are: username, recap_data_access_group
     *
     * @return The number of DAGs imported.
     *
     * @throws JavaREDCapException Thrown if an error occurs.
     */ 
    public int importUserDagAssignment(List<Map<String, String>> dagAssignments) throws JavaREDCapException
    {
    	int result = 0;

    	try {
    		// Convert to JSON and send it on.
    		String json = new ObjectMapper().writeValueAsString(dagAssignments);

    		result = importUserDagAssignment(json, REDCapApiFormat.JSON);
    	}
    	catch (JsonProcessingException e) {
    		throw new JavaREDCapException("Exception preparing REDCap request", ErrorHandlerInterface.JSON_ERROR, e);
    	}

    	return result;
    }

    /**
     * Imports User-DAG assignments, allowing you to assign users to any
     * data access group.o the project. If you wish to modify an existing
     * mapping, you must provide its unique username and group name.
     * If the 'redcap_data_access_group' column is not provided, user
     * will not be assigned to any group. There should be only one record
     * per username.
     *
     * @param dagAssignments The User-DAG assignments to import.<br>
     * The field names (keys) used in both cases are: username, recap_data_access_group
     * @param format The format for the export.
     *     <ul>
     *       <li>REDCapApiFormat.CSV - string of CSV (comma-separated values)</li>
     *       <li>REDCapApiFormat.JSON - string of JSON encoded values</li>
     *       <li>REDCapApiFormat.XML - string of XML encoded data</li>
     *     </ul>
     *
     * @return The number of DAGs imported.
     *
     * @throws JavaREDCapException Thrown if an error occurs.
     */
    public int importUserDagAssignment(String dagAssignments, REDCapApiFormat format) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.USER_DAG_MAPPING, errorHandler);
    	data.setAction(REDCapApiAction.IMPORT);

    	//#---------------------------------------
    	//# Process arguments
    	//#---------------------------------------
    	data.setFormat(format);
    	data.setData(processImportDataArgument(dagAssignments, "userDagMapping", format));

    	String result = connection.call(data);

    	processNonExportResult(result);

    	return Integer.valueOf(result);
    }

    /**
     * Exports the logging (audit trail) of all changes made to this project,
     * including data exports, data changes, project metadata changes,
     * modification of user rights, etc.
     *
     * @param format the format for the export.
     *     <ul>
     *       <li>REDCapApiFormat.CSV - string of CSV (comma-separated values)</li>
     *       <li>REDCapApiFormat.JSON - string of JSON encoded values</li>
     *       <li>REDCapApiFormat.XML' - string of XML encoded data</li>
     *     </ul>
     * @param logType type of logging to return. Defaults to NULL
     *     to return all types.
     * @param username returns only the events belong to specific
     *     username. If not specified, it will assume all users.
     * @param recordId the record ID for the file to be exported.
     * @param dag returns only the events belong to specific DAG
     *     (referring to group_id), provide a dag. If not specified, it will
     *     assume all dags.
     * @param beginTime specifies to return only those records
     *     *after* a given date/time, provide a timestamp in the format
     *     YYYY-MM-DD HH:MM (e.g., '2017-01-01 17:00' for January 1, 2017
     *     at 5:00 PM server time). If not specified, it will assume no
     *     begin time.
     * @param endTime returns only records that have been logged
     *     *before* a given date/time, provide a timestamp in the format
     *     YYYY-MM-DD HH:MM (e.g., '2017-01-01 17:00' for January 1, 2017
     *     at 5:00 PM server time). If not specified, it will use the current
     *     server time.
     *
     * @return array information, filtered by event (logtype), listing all
     *     changes made to this project. Each element of the array is an
     *     associative array with the following keys:
     *     'timestamp', 'username', 'action', 'details'
     *     
     * @throws JavaREDCapException Thrown if there is an issue with a
     * 		parameter or a problem communicating with REDCap.
     */
    public String exportLogging(
    		REDCapApiFormat format,
    		REDCapApiLogType logType,
    		String username,
    		String recordId,
    		String dag,
    		String beginTime,
    		String endTime
    		) throws JavaREDCapException
    {
    	REDCapApiRequest data = new REDCapApiRequest(apiToken, REDCapApiContent.LOG, errorHandler);

    	//#---------------------------------------
    	//# Process arguments
    	//#---------------------------------------
    	data.setFormat(format);
    	data.setLogType(logType);
    	data.setUser(username);
    	data.setRecord(recordId, false);
    	data.setDag(dag);
    	data.setBeginTime(beginTime);
    	data.setEndTime(endTime);

    	String logs = connection.call(data);
    	logs = processExportResult(logs);

    	return logs;
	}

    /**
     * Gets the JavaRedcap version number.
     * 
     * @return The version of JavaRedcap 
     */
    public String getJavaRedcapVersion()
    {
        return Version.RELEASE_NUMBER;
    }

    /**
     * Gets a list of record ID batches.
     *
     * These can be used for batch processing of records exports to lessen
     * memory requirements, for example:
     * <pre>
     * <code class="java">
     * ...
     * # Get all the record IDs of the project in 10 batches
     * List&lt;Set&lt;String&gt;&gt;&gt; recordIdBatches = project.getRecordIdBatches(10, null, null);
     * for (Set&lt;String&gt; recordIdBatch : recordIdBatches) {
     *     List&lt;Map&lt;String, Object&gt;&gt; records = project.exportRecords(REDCapApiType.FLAT, recordIdBatch, ...);
     *     ...
     * }
     * ...
     * </code>
     * </pre>
     *
     * @param batchSize The batch size in number of record IDs.<br>
     *     The last batch may have less record IDs. For example, if you had 500
     *     record IDs and specified a batch size of 200, the first 2 batches would have
     *     200 record IDs, and the last batch would have 100.
     * @param filterLogic Logic used to restrict the records retrieved, e.g.,
     *     "[last_name] = 'Smith'". This could be used for batch processing a subset
     *     of the records.
     * @param recordIdFieldName The name of the record ID field. Specifying this is not
     *     necessary, but will speed things up, because it will eliminate the need for
     *     this method to call the REDCap API to retrieve the value.
     *     
     * @return A list of Sets record IDs, where each record ID set is
     * 		considered to be a batch. Each batch can be used as the value
     *     for the records IDs parameter for an export records method.
     *     
     * @throws JavaREDCapException Thrown if there is an issue with a
     * 		parameter or a problem communicating with REDCap.
     */
    public List<Set<String>> getRecordIdBatches(Integer batchSize, String filterLogic, String recordIdFieldName) throws JavaREDCapException
    {
    	List<Set<String>> recordIdBatches = new LinkedList<Set<String>>();

        //#-----------------------------------
        //# Check arguments
        //#-----------------------------------
    	if (null == batchSize) {
    		errorHandler.throwException(
    				"The number of batches was not specified.",
    				ErrorHandlerInterface.INVALID_ARGUMENT
    				);
        } else if (batchSize < 1) {
        	errorHandler.throwException(
        			"The batch size argument is less than 1. It needs to be at least 1.",
        			ErrorHandlerInterface.INVALID_ARGUMENT
        			);
        }

        if (null == recordIdFieldName) {
            recordIdFieldName = getRecordIdFieldName();
        }

        List<Map<String, Object>> records = exportRecords(
        		REDCapApiType.FLAT,
        		null,
        		new HashSet<>(Arrays.asList(recordIdFieldName)),
        		null,
        		null,
        		filterLogic,
        		null,
        		null,
        		false,
        		false,
        		false,
        		null,
        		null,
        		null,
        		null
        		);

//        $records = $this->exportRecordsAp(
//            ['fields' => [$recordIdFieldName], 'filterLogic' => $filterLogic]
//        );

        Set<String> recordIdBatch = null;

        for (Map<String, Object> fields : records) {
        	if (null == recordIdBatch) {
        		recordIdBatch = new HashSet<String>();
        		recordIdBatches.add(recordIdBatch);
        	}

        	recordIdBatch.add((String)fields.get(recordIdFieldName));

        	if (recordIdBatch.size() == batchSize) {
        		recordIdBatch = null;
        	}
        }

        return recordIdBatches;
    }

    

    /**
     * Gets the record ID field name for the project.
     *
     * @return string the field name of the record ID field of the project.
     * 
     * @throws JavaREDCapException Thrown if there is an issue communicating
     * 		with REDCap.
     */
    public String getRecordIdFieldName() throws JavaREDCapException
    {
    	List<Map<String, Object>> metadata = exportMetadata(null, null);

    	Map<String, Object> firstField = metadata.get(0);
    	String recordIdFieldName = (String)firstField.get("field_name");

        return recordIdFieldName;
    }

    /**
     * Gets the API token for the project.
     *
     * @return The API token for the project.
     */
    public String getApiToken()
    {
        return apiToken;
    }

    
    /**
     * Returns the underlying REDCap API connection being used by the project.
     * This can be used to make calls to the REDCap API, possibly to access
     * functionality not supported by JavaRedcap.
     *
     * @return The underlying REDCap API connection being used by the project.
     */
    public REDCapApiConnectionInterface getConnection()
    {
        return connection;
    }

    /**
     * Sets the connection used for calling the REDCap API.
     *
     * @param connection The connection to use for calls to the REDCap API.
     * 
     * @throws JavaREDCapException Thrown if the connection provided is null.
     */
    public void setConnection(REDCapApiConnectionInterface connection) throws JavaREDCapException
    {
        if (null == connection) {
        	errorHandler.throwException(
        			"The connection argument cannot be null.",
        			ErrorHandlerInterface.INVALID_ARGUMENT
        			);
        }

        this.connection = connection;
    }

    /**
     * Gets the error handler.
     *
     * @return The error handler being used.
     */
    public ErrorHandlerInterface getErrorHandler()
    {
        return errorHandler;
    }

    /**
     * Sets the error handler to be used by the project.
     *
     * @param errorHandler The error handler to use. If null the default
     * 		{@link ErrorHandler} will be used.
     */
    public void setErrorHandler(ErrorHandlerInterface errorHandler)
    {
        if (null == errorHandler) {
            // Set errorHandler to default
        	errorHandler = new ErrorHandler();
        }
        
        this.errorHandler = errorHandler;
    }

    /**
     * Checks the result returned from the REDCap API for non-export methods.
     * JavaRedcap is set to return errors from REDCap using JSON, so the
     * result string is checked to see if there is a JSON format error, and
     * if so, an exception is thrown using the error message returned from
     * the REDCap API.
     *
     * @param result A result returned from the REDCap API, which should be
     * 		for a non-export method.
     * 
     * @throws JavaREDCapException Thrown if an error occurs.
     */
    protected void checkForRedcapError(String result) throws JavaREDCapException
    {
    	Pattern pattern = Pattern.compile(REDCapProject.JSON_RESULT_ERROR_PATTERN);
    	Matcher matcher = pattern.matcher(result);

    	if (matcher.matches()) {
            // note: $matches[0] is the complete string that matched
            //       $matches[1] is just the error message part
            String message = matcher.group(1);
            message = message.replace('\"', '"');
            message = message.replace("\n", System.lineSeparator());

            errorHandler.throwException(message, ErrorHandlerInterface.REDCAP_API_ERROR);
        }
    }
    
    /**
     * Checks the result returned from the REDCap API for non-export methods.
     * JavaRedcap is set to return errors from REDCap using JSON, so the
     * result string is checked to see if there is a JSON format error, and
     * if so, an exception is thrown using the error message returned from
     * the REDCap API.
     *
     * @param result A result returned from the REDCap API, which should be
     * 		for a non-export method.
     * 
     * @throws JavaREDCapException Thrown if an error occurs
     */
    protected void processNonExportResult(String result) throws JavaREDCapException
    {
    	checkForRedcapError(result);
    }

    /**
     * Processes an export result from the REDCap API.
     *
     * @param result The response from REDCap
     *
     * @return The response from the REDCap server 
     * 
     * @throws JavaREDCapException Thrown if the response contains a error from REDCap.
     */
    protected String processExportResult(String result) throws JavaREDCapException
    {
    	checkForRedcapError(result);

        return result;
    }

    protected String processImportDataArgument(String data, String dataName, REDCapApiFormat format) throws JavaREDCapException
    {
        if (null == data) {
        	errorHandler.throwException(
        			"No value specified for required argument '"+dataName+"'.",
        			ErrorHandlerInterface.INVALID_ARGUMENT
        			);
        }

        return data;
    }

/*    
    protected String processImportDataArgument(Map<String, Object> data, String dataName, REDCapApiFormat format) throws JavaRedcapException
    {
    	if (null == data || data.isEmpty()) {
        	errorHandler.throwException(
        			"No value specified for required argument '"+dataName+"'.",
        			ErrorHandlerInterface.INVALID_ARGUMENT
        			);
        }

        String json = new ObjectMapper().writeValueAsString(data);

         // TODO: handle json errors

            $jsonError = json_last_error();

            switch ($jsonError) {
                case JSON_ERROR_NONE:
                    break;
                default:
                    $message =  'JSON error ('.$jsonError.') "'. json_last_error_msg().
                    '"'." while processing argument '".$dataName."'.";
                    $this->errorHandler->throwException($message, ErrorHandlerInterface::JSON_ERROR);
                    break; // @codeCoverageIgnore
            }

        return json;
    }
*/

    
/*    
    protected function processAllRecordsArgument($allRecords)
    {
        if (!isset($allRecords)) {
            ;  // That's OK
        } elseif (!is_bool($allRecords)) {
            $message = 'The allRecords argument has type "'.gettype($allRecords).
            '", but it should be a boolean (true/false).';
            $this->errorHandler->throwException($message, ErrorHandlerInterface::INVALID_ARGUMENT);
        } elseif ($allRecords !== true) {
            $allRecords = null; // need to reset to null, because ANY (non-null) value
                                // will cause the REDCap API to return all records
        }

        return $allRecords;
    }
*/
/*    
    protected String processApiTokenArgument(String apiToken)
    {
    	if (null == apiToken || apiToken.trim().length() == 0) {
    		errorHandler.throwException(
    				"The REDCap API token specified for the project was null or blank.",
    				ErrorHandlerInterface.INVALID_ARGUMENT
    				);
        } elseif (!ctype_xdigit($apiToken)) {   // ctype_xdigit - check token for hexidecimal
            $message = 'The REDCap API token has an invalid format.'
                .' It should only contain numbers and the letters A, B, C, D, E and F.';
            $code = ErrorHandlerInterface::INVALID_ARGUMENT;
            $this->errorHandler->throwException($message, $code);
        } elseif (strlen($apiToken) != 32) { # Note: super tokens are not valid for project methods
            $message = 'The REDCap API token has an invalid format.'
                .' It has a length of '.strlen($apiToken).' characters, but should have a length of'
                .' 32.';
            $code = ErrorHandlerInterface::INVALID_ARGUMENT;
            $this->errorHandler->throwException($message, $code);
        } // @codeCoverageIgnore
        return $apiToken;
    }
*/
/*    
    protected function processArmArgument($arm)
    {
        if (!isset($arm)) {
            ;  // That's OK
        } elseif (is_string($arm)) {
            if (! preg_match('/^[0-9]+$/', $arm)) {
                $this->errorHandler->throwException(
                    'Arm number "' . $arm . '" is non-numeric string.',
                    ErrorHandlerInterface::INVALID_ARGUMENT
                );
            } // @codeCoverageIgnore
        } elseif (is_int($arm)) {
            if ($arm < 0) {
                $this->errorHandler->throwException(
                    'Arm number "' . $arm . '" is a negative integer.',
                    ErrorHandlerInterface::INVALID_ARGUMENT
                );
            } // @codeCoverageIgnore
        } else {
            $message = 'The arm argument has type "'.gettype($arm)
                .'"; it should be an integer or a (numeric) string.';
            $code = ErrorHandlerInterface::INVALID_ARGUMENT;
            $this->errorHandler->throwException($message, $code);
        } // @codeCoverageIgnore

        return $arm;
    }
*/
/*
    protected Set<Integer> processArmsArgument(Set<Integer> arms) {
    	return processArmsArgument(arms, false);
    }

    protected Set<Integer> processArmsArgument(Set<Integer> arms, boolean required)
    {
        if (null == arms) {
        	if (required) {
	        	errorHandler.throwException(
	        			"The arms argument was not set.",
	                    ErrorHandlerInterface.INVALID_ARGUMENT
	                );
            }
            arms = new HashSet<Integer>();
        } else {
            if (required && arms.length() < 1) {
            	errorHandler.throwException(
            			"No arms were specified in the arms argument; at least one must be specified.",
            			ErrorHandlerInterface.INVALID_ARGUMENT
            			);
            }
        }

        for (Integer arm : arms) {
        	if (arm < 0) {
        		errorHandler.throwException(
        				"Arm number '" + arm + "' is a negative integer.",
                        ErrorHandlerInterface.INVALID_ARGUMENT
                    );
        	}
        }

        return arms;
    }
*/    
/*    
    protected String processDateFormatArgument(String dateFormat)
    {
        if (null == dataFormat) {
            dateFormat = 'YMD';
        } else {
        	dateFormat = dataFormat.toUpperCase();

            String legalDateFormats[] = ['MDY', 'DMY', 'YMD'];

            if (!Arrays.asList(legalDataFormats).contains(dataFormat)) {
            	errorHandler.throwException(
            			"Invalid date format '" + dateFormat + "' specified. " +
    					"The date format should be one of the following: " + Arrays.toString(legalDataFormats),
    					ErrorHandlerInterface.INVALID_ARGUMENT
    					);
            }
        }

        return dateFormat;
    }
*/    
/*    
    protected function processEventArgument($event)
    {
        if (!isset($event)) {
            ; // This might be OK
        } elseif (gettype($event) !== 'string') {
            $message = 'Event has type "'.gettype($event).'", but should be a string.';
            $this->errorHandler->throwException($message, ErrorHandlerInterface::INVALID_ARGUMENT);
        } // @codeCoverageIgnore
        return $event;
    }
*/
/*
    protected function processEventsArgument(Set<String>events) {
    	return processEventsArgument(events, false);
    }

    protected Set<String> processEventsArgument(Set<String> events, boolean required)
    {
        if (null == events) {
            if (required) {
            	errorHandler.throwException(
            			"The events argument was not set.",
            			ErrorHandlerInterface.INVALID_ARGUMENT
            			);
            }

            // If not set, but not required, use an empty set
            events = new Set<String>();
        } else {
            if (required && events.size() < 1) {
            	errorHandler.throwException(
            			"No events were specified in the events argument; at least one must be specified.",
            			ErrorHandlerInterface.INVALID_ARGUMENT
            			);
            } else {
                for (String event : events) {
                	if (0 == event.trim().length()) {
                		errorHandler.throwException(
                				"Empty event specified.",
                				ErrorHandlerInterface.INVALID_ARGUMENT
                				);
                    }
                }
            }
        }

        return $events;
    }
*/    
/*
    protected function processExportCheckboxLabelArgument($exportCheckboxLabel)
    {
        if ($exportCheckboxLabel == null) {
            $exportCheckboxLabel = false;
        } else {
            if (gettype($exportCheckboxLabel) !== 'boolean') {
                $this->errorHandler->throwException(
                    'Invalid type for exportCheckboxLabel. It should be a boolean (true or false),'
                    .' but has type: '.gettype($exportCheckboxLabel).'.',
                    ErrorHandlerInterface::INVALID_ARGUMENT
                );
            } // @codeCoverageIgnore
        }
        return $exportCheckboxLabel;
    }
*/
/*
    protected function processExportDataAccessGroupsArgument($exportDataAccessGroups)
    {
        if ($exportDataAccessGroups == null) {
            $exportDataAccessGroups = false;
        } else {
            if (gettype($exportDataAccessGroups) !== 'boolean') {
                $message = 'Invalid type for exportDataAccessGroups.'
                    .' It should be a boolean (true or false),'
                    .' but has type: '.gettype($exportDataAccessGroups).'.';
                $code = ErrorHandlerInterface::INVALID_ARGUMENT;
                $this->errorHandler->throwException($message, $code);
            } // @codeCoverageIgnore
        }
        return $exportDataAccessGroups;
    }
*/
/*    
    protected function processExportFilesArgument($exportFiles)
    {
        if ($exportFiles== null) {
            $exportFiles= false;
        } else {
            if (gettype($exportFiles) !== 'boolean') {
                $message = 'Invalid type for exportFiles. It should be a boolean (true or false),'
                    .' but has type: '.gettype($exportFiles).'.';
                $code = ErrorHandlerInterface::INVALID_ARGUMENT;
                $this->errorHandler->throwException($message, $code);
            } // @codeCoverageIgnore
        }
        return $exportFiles;
    }
*/    
/*    
    protected function processExportSurveyFieldsArgument($exportSurveyFields)
    {
        if ($exportSurveyFields == null) {
            $exportSurveyFields = false;
        } else {
            if (gettype($exportSurveyFields) !== 'boolean') {
                $message =  'Invalid type for exportSurveyFields.'
                    .' It should be a boolean (true or false),'
                    .' but has type: '.gettype($exportSurveyFields).'.';
                $code = ErrorHandlerInterface::INVALID_ARGUMENT;
                $this->errorHandler->throwException($message, $code);
            } // @codeCoverageIgnore
        }
        return $exportSurveyFields;
    }
*/
/*    
    protected String processFieldArgument(String field) {
    	return processFieldArgument(field, true);
    }

    protected String processFieldArgument(String field, boolean required)
    {
    	if (null == field) {
            if (required) {
            	errorHandler.throwException(
            			"No field was specified but is required.",
            			ErrorHandlerInterface.INVALID_ARGUMENT
            			);
            }
            // else OK
        }

        return field;
    }
*/    
/*    
    protected function processFieldsArgument($fields)
    {
        if (!isset($fields)) {
            $fields = array();
        } else {
            if (!is_array($fields)) {
                $message = 'Argument "fields" has the wrong type; it should be an array.';
                $code    = ErrorHandlerInterface::INVALID_ARGUMENT;
                $this->errorHandler->throwException($message, $code);
            } else { // @codeCoverageIgnore
                foreach ($fields as $field) {
                    $type = gettype($field);
                    if (strcmp($type, 'string') !== 0) {
                        $message = 'A field with type "'.$type.'" was found in the fields array.'.
                            ' Fields should be strings.';
                        $code = ErrorHandlerInterface::INVALID_ARGUMENT;
                        $this->errorHandler->throwException($message, $code);
                    } // @codeCoverageIgnore
                }
            }
        }

        return $fields;
    }
*/
/*    
    protected function processFileArgument($file)
    {
        if (isset($file)) {
            if (gettype($file) !== 'string') {
                $message = "Argument 'file' has type '".gettype($file)."', but should be a string.";
                $code    = ErrorHandlerInterface::INVALID_ARGUMENT;
                $this->errorHandler->throwException($message, $code);
            } // @codeCoverageIgnore
        }
        return $file;
    }

    protected function processFilenameArgument($filename)
    {
        if (!isset($filename)) {
            $message = 'No filename specified.';
            $code    = ErrorHandlerInterface::INVALID_ARGUMENT;
            $this->errorHandler->throwException($message, $code);
        } elseif (gettype($filename) !== 'string') {
            $message = "Argument 'filename' has type '".gettype($filename)."', but should be a string.";
            $code    = ErrorHandlerInterface::INVALID_ARGUMENT;
            $this->errorHandler->throwException($message, $code);
        } elseif (!file_exists($filename)) {
            $message = 'The input file "'.$filename.'" could not be found.';
            $code    = ErrorHandlerInterface::INPUT_FILE_NOT_FOUND;
            $this->errorHandler->throwException($message, $code);
        } elseif (!is_readable($filename)) {
            $message = 'The input file "'.$filename.'" was unreadable.';
            $code    = ErrorHandlerInterface::INPUT_FILE_UNREADABLE;
            $this->errorHandler->throwException($message, $code);
        } // @codeCoverageIgnore

        $basename = pathinfo($filename, PATHINFO_BASENAME);
        $curlFile = curl_file_create($filename, 'text/plain', $basename);

        return $curlFile;
    }
*/    
/*    
    protected function processFilterLogicArgument($filterLogic)
    {
        if ($filterLogic == null) {
            $filterLogic = '';
        } else {
            if (gettype($filterLogic) !== 'string') {
                $message = 'Invalid type for filterLogic. It should be a string, but has type "'
                    .gettype($filterLogic).'".';
                $code = ErrorHandlerInterface::INVALID_ARGUMENT;
                $this->errorHandler->throwException($message, $code);
            } // @codeCoverageIgnore
        }
        return $filterLogic;
    }
  */  
/*    
    protected function processForceAutoNumberArgument($forceAutoNumber)
    {
        if ($forceAutoNumber == null) {
            $forceAutoNumber = false;
        } else {
            if (gettype($forceAutoNumber) !== 'boolean') {
                $message = 'Invalid type for forceAutoNumber.'
                    .' It should be a boolean (true or false),'
                    .' but has type: '.gettype($forceAutoNumber).'.';
                $code    = ErrorHandlerInterface::INVALID_ARGUMENT;
                $this->errorHandler->throwException($message, $code);
            } // @codeCoverageIgnore
        }
        return $forceAutoNumber;
    }
    */
/*    
    protected function processFormArgument($form, $required = false)
    {
        if (!isset($form)) {
            if ($required === true) {
                $message = 'The form argument was not set.';
                $code    = ErrorHandlerInterface::INVALID_ARGUMENT;
                $this->errorHandler->throwException($message, $code);
            } // @codeCoverageIgnore
            $form = '';
        } elseif (!is_string($form)) {
            $message = 'The form argument has invalid type "'.gettype($form)
                .'"; it should be a string.';
            $code = ErrorHandlerInterface::INVALID_ARGUMENT;
            $this->errorHandler->throwException($message, $code);
        } // @codeCoverageIgnore

        return $form;
    }
  */
/*    
    protected function processFormatArgument(&$format, $legalFormats)
    {
        if (!isset($format)) {
            $format = 'php';
        }

        if (gettype($format) !== 'string') {
            $message = 'The format specified has type "'.gettype($format)
                .'", but it should be a string.';
            $code = ErrorHandlerInterface::INVALID_ARGUMENT;
            $this->errorHandler->throwException($message, $code);
        } // @codeCoverageIgnore

        $format = strtolower(trim($format));

        if (!in_array($format, $legalFormats)) {
            $message = 'Invalid format "'.$format.'" specified.'
                .' The format should be one of the following: "'.
                implode('", "', $legalFormats).'".';
            $code = ErrorHandlerInterface::INVALID_ARGUMENT;
            $this->errorHandler->throwException($message, $code);
        } // @codeCoverageIgnore

        $dataFormat = '';
        if (strcmp($format, 'php') === 0) {
            $dataFormat = 'json';
        } else {
            $dataFormat = $format;
        }

        return $dataFormat;
    }
*/    
    /*
    protected function processFormsArgument($forms)
    {
        if (!isset($forms)) {
            $forms = array();
        } else {
            if (!is_array($forms)) {
                $message = 'The forms argument has invalid type "'.gettype($forms)
                    .'"; it should be an array.';
                $code = ErrorHandlerInterface::INVALID_ARGUMENT;
                $this->errorHandler->throwException($message, $code);
            } else { // @codeCoverageIgnore
                foreach ($forms as $form) {
                    $type = gettype($form);
                    if (strcmp($type, 'string') !== 0) {
                        $message = 'A form with type "'.$type.'" was found in the forms array.'.
                            ' Forms should be strings.';
                        $this->errorHandler->throwException($message, ErrorHandlerInterface::INVALID_ARGUMENT);
                    } // @codeCoverageIgnore
                }
            }
        }

        return $forms;
    }
*/
/*    
    protected boolean processOverrideArgument(boolean override)
    {
    	return override ? 1 : 0;
    }
*/    
    /*
    protected function processOverwriteBehaviorArgument($overwriteBehavior)
    {
        if (!isset($overwriteBehavior)) {
            $overwriteBehavior = 'normal';
        } elseif ($overwriteBehavior !== 'normal' && $overwriteBehavior !== 'overwrite') {
            $message = 'Invalid value "'.$overwriteBehavior.'" specified for overwriteBehavior.'.
                " Valid values are 'normal' and 'overwrite'.";
            $code = ErrorHandlerInterface::INVALID_ARGUMENT;
            $this->errorHandler->throwException($message, $code);
        } // @codeCoverageIgnore

        return $overwriteBehavior;
    }
    */
    /*
    protected function processRawOrLabelArgument($rawOrLabel)
    {
        if (!isset($rawOrLabel)) {
            $rawOrLabel = 'raw';
        } else {
            if ($rawOrLabel !== 'raw' && $rawOrLabel !== 'label') {
                $message =   'Invalid value "'.$rawOrLabel.'" specified for rawOrLabel.'
                    ." Valid values are 'raw' and 'label'.";
                    $code = ErrorHandlerInterface::INVALID_ARGUMENT;
                $this->errorHandler->throwException($message, $code);
            } // @codeCoverageIgnore
        }
        return $rawOrLabel;
    }
*/
/*    
    protected function processRawOrLabelHeadersArgument($rawOrLabelHeaders)
    {
        if (!isset($rawOrLabelHeaders)) {
            $rawOrLabelHeaders = 'raw';
        } else {
            if ($rawOrLabelHeaders !== 'raw' && $rawOrLabelHeaders !== 'label') {
                $message = 'Invalid value "'.$rawOrLabelHeaders.'" specified for rawOrLabelHeaders.'
                    ." Valid values are 'raw' and 'label'.";
                $code = ErrorHandlerInterface::INVALID_ARGUMENT;
                $this->errorHandler->throwException($message, $code);
            } // @codeCoverageIgnore
        }
        return $rawOrLabelHeaders;
    }
*/    
/*    
    protected function processRecordIdArgument($recordId, $required = true)
    {
        if (!isset($recordId)) {
            if ($required) {
                $message = 'No record ID specified.';
                $code    = ErrorHandlerInterface::INVALID_ARGUMENT;
                $this->errorHandler->throwException($message, $code);
            } // @codeCoverageIgnore
        } elseif (!is_string($recordId) && !is_int($recordId)) {
            $message = 'The record ID has type "'.gettype($recordId)
                .'", but it should be a string or integer.';
                $code = ErrorHandlerInterface::INVALID_ARGUMENT;
            $this->errorHandler->throwException($message, $code);
        }  // @codeCoverageIgnore

        return $recordId;
    }
*/    
/*
    protected function processRecordIdsArgument($recordIds)
    {
        if (!isset($recordIds)) {
            $recordIds = array();
        } else {
            if (!is_array($recordIds)) {
                $message = 'The record IDs argument has type "'.gettype($recordIds)
                    .'"; it should be an array.';
                $code = ErrorHandlerInterface::INVALID_ARGUMENT;
                $this->errorHandler->throwException($message, $code);
            } else { // @codeCoverageIgnore
                foreach ($recordIds as $recordId) {
                    $type = gettype($recordId);
                    if (strcmp($type, 'integer') !== 0 && strcmp($type, 'string') !== 0) {
                        $message = 'A record ID with type "'.$type.'" was found.'
                            .' Record IDs should be integers or strings.';
                            $code = ErrorHandlerInterface::INVALID_ARGUMENT;
                        $this->errorHandler->throwException($message, $code);
                    } // @codeCoverageIgnore
                }
            }
        }
        return $recordIds;
    }
*/    

/*
    protected function processRepeatInstanceArgument($repeatInstance)
    {
        if (!isset($repeatInstance)) {
            ; // Might be OK
        } elseif (!is_int($repeatInstance)) {
            $message = 'The repeat instance has type "'.gettype($repeatInstance)
                .'", but it should be an integer.';
            $code    = ErrorHandlerInterface::INVALID_ARGUMENT;
            $this->errorHandler->throwException($message, $code);
        } // @codeCoverageIgnore

        return $repeatInstance;
    }
*/    
/*
    protected function processReportIdArgument($reportId)
    {
        if (!isset($reportId)) {
            $message = 'No report ID specified for export.';
            $code    = ErrorHandlerInterface::INVALID_ARGUMENT;
            $this->errorHandler->throwException($message, $code);
        } // @codeCoverageIgnore

        if (is_string($reportId)) {
            if (!preg_match('/^[0-9]+$/', $reportId)) {
                $message = 'Report ID "'.$reportId.'" is non-numeric string.';
                $code    = ErrorHandlerInterface::INVALID_ARGUMENT;
                $this->errorHandler->throwException($message, $code);
            } // @codeCoverageIgnore
        } elseif (is_int($reportId)) {
            if ($reportId < 0) {
                $message = 'Report ID "'.$reportId.'" is a negative integer.';
                $code    = ErrorHandlerInterface::INVALID_ARGUMENT;
                $this->errorHandler->throwException($message, $code);
            } // @codeCoverageIgnore
        } else {
            $message = 'The report ID has type "'.gettype($reportId)
                .'", but it should be an integer or a (numeric) string.';
                $code    = ErrorHandlerInterface::INVALID_ARGUMENT;
            $this->errorHandler->throwException($message, $code);
        } // @codeCoverageIgnore

        return $reportId;
    }
*/    
/*
    protected function processReturnContentArgument($returnContent, $forceAutoNumber)
    {
        if (!isset($returnContent)) {
            $returnContent = 'count';
        } elseif ($returnContent === 'auto_ids') {
            if ($forceAutoNumber !== true) {
                $message = "'auto_ids' specified for returnContent,"
                    ." but forceAutoNumber was not set to true;"
                    ." 'auto_ids' can only be used when forceAutoNumber is set to true.";
                $code = ErrorHandlerInterface::INVALID_ARGUMENT;
                $this->errorHandler->throwException($message, $code);
            } // @codeCoverageIgnore
        } elseif ($returnContent !== 'count' && $returnContent !== 'ids') {
            $message = "Invalid value '".$returnContent."' specified for returnContent.".
                    " Valid values are 'count', 'ids' and 'auto_ids'.";
            $code = ErrorHandlerInterface::INVALID_ARGUMENT;
            $this->errorHandler->throwException($message, $code);
        } // @codeCoverageIgnore

        return $returnContent;
    }
*/
/*    
    protected function processReturnMetadataOnlyArgument($returnMetadataOnly)
    {
        if ($returnMetadataOnly== null) {
            $returnMetadataOnly= false;
        } else {
            if (gettype($returnMetadataOnly) !== 'boolean') {
                $message = 'Invalid type for returnMetadataOnly.'
                    .' It should be a boolean (true or false),'
                    .' but has type: '.gettype($returnMetadataOnly).'.';
                $code    = ErrorHandlerInterface::INVALID_ARGUMENT;
                $this->errorHandler->throwException($message, $code);
            } // @codeCoverageIgnore
        }
        return $returnMetadataOnly;
    }
*/    

/*    
    protected function processTypeArgument($type)
    {
        if (!isset($type)) {
            $type = 'flat';
        }
        $type = strtolower(trim($type));

        if (strcmp($type, 'flat') !== 0 && strcmp($type, 'eav') !== 0) {
            $message = "Invalid type '".$type."' specified. Type should be either 'flat' or 'eav'";
            $code    = ErrorHandlerInterface::INVALID_ARGUMENT;
            $this->errorHandler->throwException($message, $code);
        } // @codeCoverageIgnore
        return $type;
    }
*/
/*    
    protected function processCsvDelimiterArgument($csvDelimiter, $format)
    {
        $legalCsvDelimiters = array(',',';','tab','|','^');
        if ($format == 'csv') {
            if (empty($csvDelimiter)) {
                $csvDelimiter = ',';
            }
            if (gettype($csvDelimiter) !== 'string') {
                $message = 'The csv delimiter specified has type "'.gettype($csvDelimiter)
                    .'", but it should be a string.';
                $code = ErrorHandlerInterface::INVALID_ARGUMENT;
                $this->errorHandler->throwException($message, $code);
            } // @codeCoverageIgnore

            $csvDelimiter = strtolower(trim($csvDelimiter));

            if (!in_array($csvDelimiter, $legalCsvDelimiters)) {
                $message = 'Invalid csv delimiter "'.$csvDelimiter.'" specified.'
                    .' Valid csv delimiter options are: "'.
                    implode('", "', $legalCsvDelimiters).'".';
                $code = ErrorHandlerInterface::INVALID_ARGUMENT;
                $this->errorHandler->throwException($message, $code);
            } // @codeCoverageIgnore
        }
        return $csvDelimiter;
    }
*/
/*    
    protected function processDateRangeArgument($date)
    {
        if (isset($date)) {
            if (trim($date) === '') {
                $date = null;
            } else {
                $legalFormat = 'Y-m-d H:i:s';
                $err = false;

                if (gettype($date) === 'string') {
                    $dt = \DateTime::createFromFormat($legalFormat, $date);

                    if (!($dt && $dt->format($legalFormat) == $date)) {
                        $err = true;
                    }
                } else {
                    $err = true;
                }

                if ($err) {
                    $errMsg = 'Invalid date format. ';
                    $errMsg .= "The date format for export dates is YYYY-MM-DD HH:MM:SS, ";
                    $errMsg .= 'e.g., 2020-01-31 00:00:00.';
                    $code = ErrorHandlerInterface::INVALID_ARGUMENT;
                    $this->errorHandler->throwException($errMsg, $code);
                } // @codeCoverageIgnore
            }
        }
        return $date;
    }
*/    
/*
    protected function processDecimalCharacterArgument($decimalCharacter)
    {
        $legalDecimalCharacters = array(',','.');
        if ($decimalCharacter) {
            if (!in_array($decimalCharacter, $legalDecimalCharacters)) {
                $message = 'Invalid decimal character of "'.$decimalCharacter.'" specified.'
                    .' Valid decimal character options are: "'.
                    implode('", "', $legalDecimalCharacters).'".';
                $code = ErrorHandlerInterface::INVALID_ARGUMENT;
                $this->errorHandler->throwException($message, $code);
            } // @codeCoverageIgnore
        }
        return $decimalCharacter;
    }
*/
/*    
    protected function processCompactDisplayArgument($compactDisplay)
    {
        if (!isset($compactDisplay) || $compactDisplay === null) {
            ;  // That's OK
        } elseif (!is_bool($compactDisplay)) {
            $message = 'The compact display argument has type "'.gettype($compactDisplay).
            '", but it should be a boolean (true/false).';
            $this->errorHandler->throwException($message, ErrorHandlerInterface::INVALID_ARGUMENT);
        }
        return $compactDisplay;
    }
*/  
/*    
    protected function processDagsArgument($dags, $required = true)
    {
        if (!isset($dags)) {
            if ($required === true) {
                $this->errorHandler->throwException(
                    'The dags argument was not set.',
                    ErrorHandlerInterface::INVALID_ARGUMENT
                );
            }
            // @codeCoverageIgnoreStart
            $dags = array();
            // @codeCoverageIgnoreEnd
        } else {
            if (!is_array($dags)) {
                $this->errorHandler->throwException(
                    'The dags argument has invalid type "'.gettype($dags).'"; it should be an array.',
                    ErrorHandlerInterface::INVALID_ARGUMENT
                );
            } elseif ($required === true && count($dags) < 1) {
                $this->errorHandler->throwException(
                    'No dags were specified in the dags argument; at least one must be specified.',
                    ErrorHandlerInterface::INVALID_ARGUMENT
                );
            }
        }

        foreach ($dags as $dag) {
            $type = gettype($dag);
            if (strcmp($type, 'string') !== 0) {
                $message = 'A dag with type "'.$type.'" was found in the dags array.'.
                    ' Dags should be strings.';
                $code = ErrorHandlerInterface::INVALID_ARGUMENT;
                $this->errorHandler->throwException($message, $code);
            }
        }

        return $dags;
    }
*/    
/*
    protected function processLogTypeArgument($logType)
    {
        $legalLogTypes = array(
            'export',
            'manage',
            'user',
            'record',
            'record_add',
            'record_edit',
            'record_delete',
            'lock_record',
            'page_view'
        );
        if ($logType) {
            if (!in_array($logType, $legalLogTypes)) {
                $message = 'Invalid log type of "'.$logType.'" specified.'
                    .' Valid log types are: "'.
                    implode('", "', $legalLogTypes).'".';
                $code = ErrorHandlerInterface::INVALID_ARGUMENT;
                $this->errorHandler->throwException($message, $code);
            }
        }
        return $logType;
    }
*/
/*    
    protected function processDagArgument($dag)
    {
        if ($dag) {
            if (!is_string($dag)) {
                $message = 'The dag argument has invalid type "'.gettype($dag)
                    .'"; it should be a string.';
                $code = ErrorHandlerInterface::INVALID_ARGUMENT;
                $this->errorHandler->throwException($message, $code);
            }
        }
        return $dag;
    }
*/    
/*
    protected function processUserArgument($username)
    {
        if ($username) {
            if (!is_string($username)) {
                $message = 'The user argument has invalid type "'.gettype($username)
                    .'"; it should be a string.';
                $code = ErrorHandlerInterface::INVALID_ARGUMENT;
                $this->errorHandler->throwException($message, $code);
            }
        }
        return $username;
    }
*/    

    private void writeStringToFile(String output, String filename) throws JavaREDCapException 
    {
    	try {
	    	Writer fw = new FileWriter(filename);

	    	try (Writer writer = new BufferedWriter(fw) ) {
	    	    writer.write(output);
	    	}
	    	catch (NumberFormatException nfe) {
	    	}
    	}
    	catch (IOException ioe) {
    		throw new JavaREDCapException("Output file not available.", ErrorHandlerInterface.OUTPUT_FILE_ERROR, ioe);
    	}
    }
}
