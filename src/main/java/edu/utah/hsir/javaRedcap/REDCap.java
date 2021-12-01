/* --------------------------------------------------------------------------
 * Copyright (C) 2021 University of Utah Health Systems Research & Innovation
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 * Derived from work by The Trustees of Indiana University Copyright (C) 2019 
 * --------------------------------------------------------------------------
 */

package edu.utah.hsir.javaRedcap;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.utah.hsir.javaRedcap.enums.REDCapApiContent;
import edu.utah.hsir.javaRedcap.enums.REDCapApiFormat;

/**
 * REDCap class used to represent a REDCap instance/site. This class is
 * typically only useful if your program needs to create REDCap projects
 * and/or needs to access more than one REDCap project.
 */
public class REDCap
{
	protected String superToken;
    protected ErrorHandlerInterface errorHandler;
    
    /** connection to the REDCap API at the apiURL. */
    protected REDCapApiConnectionInterface connection;

    /** function for creating project object */
    protected REDCapProjectFactory projectFactory;

    
    /**
     * Constructs an instance for communicating with the REDCap server via
     * the provided connection.  
     *
     * @param superToken The user's super token. This needs to be provided if
     *     you are going to create projects.
     * @param errorHandler The error handler that will be used. This would
     * 		normally only be set if you want to override JavaRedcap's default
     *		error handler.
     * @param connection The connection that will be used. This would normally
     * 		only be set if you want to override JavaRedcap's default
     * 		connection. If this argument is specified, the apiUrl, sslVerify,
     * 		and caCertificateFile arguments will be ignored, and the values
     * 		for these set in the connection will be used.
     *
     * @throws JavaREDCapException Thrown if there is an issue with one of the
     * 		parameters or there is a problem creating the connection. 
     */
    public REDCap (
    		String superToken,
    		REDCapApiConnectionInterface connection,
    		ErrorHandlerInterface errorHandler
    		) throws JavaREDCapException
    {
    	setErrorHandler(errorHandler);
 	   
    	this.superToken = processApiTokenArgument(superToken, 64, this.errorHandler);
        
    	if (null == connection) {
    		throw new JavaREDCapException("Connection cannot be null", ErrorHandlerInterface.INVALID_ARGUMENT);
    	}
    	
    	this.connection = connection;
       
    	projectFactory = new REDCapProjectFactory() {
    		@Override
			public REDCapProject createProject(String apiUrl, String apiToken, boolean sslVerify,
					String caCertificateFile, ErrorHandlerInterface errorHandler,
					REDCapApiConnectionInterface connection) throws JavaREDCapException {
				return new REDCapProject(apiUrl, apiToken, sslVerify, caCertificateFile, errorHandler, connection);
			}
    	};
    }

    /**
     * 
     * Constructs an instance for communicating with the REDCap server via
     * a REDCapApiConnection instance.  
     *
     * @param apiUrl the URL for the API for your REDCap site.
     * @param superToken The user's super token. This needs to be provided if
     *		you are going to create projects.
     * @param sslVerify indicates if SSL connection to REDCap web site should
     * 		be verified.
     * @param caCertificateFile the full path name of the CA (Certificate Authority)
     *     certificate file.
     * @param errorHandler The error handler that will be used. This would
     * 		normally only be set if you want to override JavaRedcap's default
     *		error handler.
     *
     * @throws JavaREDCapException Thrown if there is an issue with one of the
     * 		parameters or there is a problem creating the connection. 
     */
    public REDCap (
    	String apiUrl,
    	String superToken,
    	boolean sslVerify,
        String caCertificateFile,
        ErrorHandlerInterface errorHandler
        ) throws JavaREDCapException
    {
    	this(superToken, new REDCapApiConnection(apiUrl, sslVerify, caCertificateFile, null), errorHandler);
    }
    
 
    /**
     * Creates a REDCap project with the specified data.
     * A super token must have been specified for this method to be used.
     *
     * The data fields that can be set are as follows:
     * <ul>
     *   <li>
     *     <b>project_title</b> - the title of the project.
     *   </li>
     *   <li>
     *     <b>purpose</b> - the purpose of the project:
     *     <ul>
     *       <li>0 - Practice/Just for fun</li>
     *       <li>1 - Other</li>
     *       <li>2 - Research</li>
     *       <li>3 - Quality Improvement</li>
     *       <li>4 - Operational Support</li>
     *     </ul>
     *   </li>
     *   <li>
     *     <b>purpose_other</b> - text describing purpose if purpose above is specified as 1.
     *   </li>
     *   <li>
     *     <b>project_notes</b> - notes about the project.
     *   </li>
     *   <li>
     *     <b>is_longitudinal</b> - indicates if the project is longitudinal (0 = False [default],
     *     1 = True).
     *   </li>
     *   <li>
     *     <b>surveys_enabled</b> - indicates if surveys are enabled (0 = False [default], 1 = True).
     *   </li>
     *   <li>
     *     <b>record_autonumbering_enabled</b> - indicates id record auto numbering is enabled
     *     (0 = False [default], 1 = True).
     *   </li>
     * </ul>
     *
     * @param projectData The data used for project creation.
     * @param odm String in CDISC ODM XML format that contains metadata and
     * 		optionally data to be imported into the created project.
     *
     * @return REDCapProject the project that was created.
     * 
     * @throws JavaREDCapException Thrown if there is a problem creating the project.
     */
    public REDCapProject createProject (List<Map<String, Object>> projectData, String odm) throws JavaREDCapException {
    	REDCapProject result = null;
    	
    	try {
    		// Convert to JSON and send it on.
    		String json = new ObjectMapper().writeValueAsString(projectData);

    		result = createProject(json, REDCapApiFormat.JSON, odm);
    	}
    	catch (JsonProcessingException e) {
    		throw new JavaREDCapException("Exception preparing REDCap request", ErrorHandlerInterface.JSON_ERROR, e);
    	}

    	return result;
    }
    /**
     * Creates a REDCap project with the specified data.
     * A super token must have been specified for this method to be used.
     *
     * The data fields that can be set are as follows:
     * <ul>
     *   <li>
     *     <b>project_title</b> - the title of the project.
     *   </li>
     *   <li>
     *     <b>purpose</b> - the purpose of the project:
     *     <ul>
     *       <li>0 - Practice/Just for fun</li>
     *       <li>1 - Other</li>
     *       <li>2 - Research</li>
     *       <li>3 - Quality Improvement</li>
     *       <li>4 - Operational Support</li>
     *     </ul>
     *   </li>
     *   <li>
     *     <b>purpose_other</b> - text descibing purpose if purpose above is specified as 1.
     *   </li>
     *   <li>
     *     <b>project_notes</b> - notes about the project.
     *   </li>
     *   <li>
     *     <b>is_longitudinal</b> - indicates if the project is longitudinal (0 = False [default],
     *     1 = True).
     *   </li>
     *   <li>
     *     <b>surveys_enabled</b> - indicates if surveys are enabled (0 = False [default], 1 = True).
     *   </li>
     *   <li>
     *     <b>record_autonumbering_enabled</b> - indicates id record autonumbering is enabled
     *     (0 = False [default], 1 = True).
     *   </li>
     * </ul>
     *
     * @param projectData The data used for project creation in the specified format.
     * @param format The format used to export the arm data.
     *     <ul>
     *       <li> 'csv' - string of CSV (comma-separated values)</li>
     *       <li> 'json' - string of JSON encoded values</li>
     *       <li> 'xml' - string of XML encoded data</li>
     *     </ul>
     * @param odm A string in CDISC ODM XML format that contains metadata and
     * 		optionally data to be imported into the created project.
     *
     * @return REDCapProject the project that was created.
     * 
     * @throws JavaREDCapException Thrown if there is a problem creating the project.
     */
    public REDCapProject createProject (String projectData, REDCapApiFormat format, String odm) throws JavaREDCapException {
        // Note: might want to clone error handler, in case state variables
        // have been added that should differ for different uses, e.g.,
        // a user message that is displayed where you have multiple project
        // objects
    	REDCapApiRequest data = new REDCapApiRequest(superToken, REDCapApiContent.PROJECT, errorHandler);
       
        /*
         * Process the arguments
         */
    	data.setFormat(format);
    	data.setData(processImportDataArgument(projectData, "projectData", format));
		data.setOdm(odm);
        
        /*
         * Create the project
         */
        String apiToken = connection.call(data);
        
        checkForRedcapError(apiToken);
        
        return getProject(apiToken);
    }


    /**
     * Gets the REDCap version number of the REDCap instance being used.
     * A super token must have been specified for this method to be used.
     *
     * @return string the REDCap version number of the REDCap instance being
     * 		used by the project.
     * 
     * @throws JavaREDCapException Thrown if there is a problem communicating
     * 		with the REDCap server.
     */
    public String  exportRedcapVersion() throws JavaREDCapException {

        String redcapVersion = connection.call(new REDCapApiRequest(superToken, REDCapApiContent.VERSION, errorHandler));

        return redcapVersion;
    }


    /**
     * Gets the REDCap project for the specified API token.
     *
     * @param apiToken The API token for the project to use.
     *
     * @return A REDCapProject instance created by the currently configured
     * REDCapProjectFactory.
     * 
     * @throws JavaREDCapException Thrown if there is an issue creating the
     * 		project or with the parameters provided. 
     */
    public REDCapProject getProject(String apiToken) throws JavaREDCapException
    {
        apiToken = REDCap.processApiTokenArgument(apiToken, 32, errorHandler);

        REDCapProject project = projectFactory.createProject(
        		connection.getUrl(),
        		apiToken,
        		connection.getSslVerify(),
        		connection.getCaCertificateFile(),
        		(ErrorHandler)errorHandler.clone(),
        		(REDCapApiConnection)connection.clone()
        		);
        
        return project;
    }
    
    /**
     * Gets the function used to create projects.
     *
     * @return The function used by this class to create projects.
     */
    public REDCapProjectFactory getProjectFactory()
    {
        return projectFactory;
    }
    
    /**
     * Sets the function used to create projects in this class.
     * 
     * This method would normally only be used if you have extended the
     * REDCapProject class and want REDCap to return projects using your
     * extended class.
     *
     * @param projectFactory The factory instance to use in creating new
     * 		projects.
     */
    public void setProjectFactory(REDCapProjectFactory projectFactory)
    {
    	if (null != projectFactory) {
    		this.projectFactory = projectFactory;
    	}
    }
    
    /**
     * Gets the error handler being used.
     *
     * @return The error handler being used.
     */
    public ErrorHandlerInterface getErrorHandler()
    {
        return errorHandler;
    }
    
    /**
     * Set the error handler that is used.
     *
     * @param errorHandler The error handler to use.
     */
    public void setErrorHandler(ErrorHandlerInterface errorHandler)
    {
    	if (null == errorHandler) {
    		this.errorHandler = new ErrorHandler();
    	}
    	else {
    		this.errorHandler = errorHandler;
        }
    }
    
    /**
     * Gets the connection being used.
     *
     * @return The connection being used.
     */
    public REDCapApiConnectionInterface getConnection()
    {
        return connection;
    }
    
    /**
     * Sets the connection that is used.
     *
     * @param connection The connection to use for requests to REDCap.
     * 
     * @throws JavaREDCapException Thrown if the connection provided is null. 
     */
    public void setConnection(REDCapApiConnectionInterface connection) throws JavaREDCapException
    {
    	if (null == connection) {
    		errorHandler.throwException(
    				"The connection argument is not valid",
    				ErrorHandlerInterface.INVALID_ARGUMENT);
        }

    	this.connection = connection;
    }

    /**
     * Validates the tokne provided.
     * 
     * @param apiToken An API token to validate.
     * @param length The expected length of the token. Must be either 32 (a
     * 		regular token) or 64 (a super token).
     * @param errorHandler The error handler to use in the event there's a
     * 		problem with the token.
     * 
     * @return The validated token.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the token provided.
     */
    public static String processApiTokenArgument(String apiToken, int length, ErrorHandlerInterface errorHandler) throws JavaREDCapException
    {
    	if (null == apiToken || 0 == apiToken.trim().length()) {
    		errorHandler.throwException(
    				"The REDCap API token specified for the project was null or blank.",
    				ErrorHandlerInterface.INVALID_ARGUMENT
    				);
        } else if (!apiToken.matches("^[A-F0-9]+$")) {   // check token for hexadecimal
        	errorHandler.throwException(
        			"The REDCap API token has an invalid format. " +
        			"It should only contain numbers and the letters A, B, C, D, E and F.",
        			ErrorHandlerInterface.INVALID_ARGUMENT
        			);
        } else if (length != 32 && length != 64) {
        	errorHandler.throwException(
        			"Invalid length specifed. REDCap API tokens are either 32 or 64 characters long.",
        			ErrorHandlerInterface.INVALID_ARGUMENT
        			);
        } else if (apiToken.trim().length() != length) { // # Note: super tokens are not valid for project methods
        	errorHandler.throwException(
        			"The REDCap API token has an invalid format. " + 
        			"It has a length of " + apiToken.trim().length() + " characters, but should have a length of " + length + ".",
        			ErrorHandlerInterface.INVALID_ARGUMENT
        			);
        }

    	return apiToken.trim();
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
}
