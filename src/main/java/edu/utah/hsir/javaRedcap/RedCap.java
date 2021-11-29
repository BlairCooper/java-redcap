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

import edu.utah.hsir.javaRedcap.enums.RedCapApiContent;
import edu.utah.hsir.javaRedcap.enums.RedCapApiFormat;

/**
 * REDCap class used to represent a REDCap instance/site. This class is
 * typically only useful if your program needs to create REDCap projects
 * and/or needs to access more than one REDCap project.
 */
public class RedCap
{
	protected String superToken;
    protected ErrorHandlerInterface errorHandler;
    
    /** connection to the REDCap API at the apiURL. */
    protected RedCapApiConnectionInterface connection;

    /** function for creating project object */
    protected RedCapProjectFactory projectFactory;
    
    /**
    *
    * @param string $superToken the user's super token. This needs to be provided if
    *     you are going to create projects.
    * @param ErrorHandlerInterface $errorHandler the error handler that will be
    *    used. This would normally only be set if you want to override the PHPCap's default
    *    error handler.
    * @param RedCapApiConnectionInterface $connection the connection that will be used.
    *    This would normally only be set if you want to override the PHPCap's default
    *    connection. If this argument is specified, the $apiUrl, $sslVerify, and
    *    $caCertificateFile arguments will be ignored, and the values for these
    *    set in the connection will be used.
    *
    * @throws JavaRedcapException 
    */
    public RedCap (
    		String superToken,
    		RedCapApiConnectionInterface connection,
    		ErrorHandlerInterface errorHandler
    		) throws JavaRedcapException
    {
    	setErrorHandler(errorHandler);
 	   
    	this.superToken = processApiTokenArgument(superToken, 64, this.errorHandler);
        
    	if (null == connection) {
    		throw new JavaRedcapException("Connection cannot be null", ErrorHandlerInterface.INVALID_ARGUMENT);
    	}
    	
    	this.connection = connection;
       
    	projectFactory = new RedCapProjectFactory() {
    		@Override
			public RedCapProject createProject(String apiUrl, String apiToken, boolean sslVerify,
					String caCertificateFile, ErrorHandlerInterface errorHandler,
					RedCapApiConnectionInterface connection) throws JavaRedcapException {
				return new RedCapProject(apiUrl, apiToken, sslVerify, caCertificateFile, errorHandler, connection);
			}
    	};
    }

    /**
     *
     * @param string $apiUrl the URL for the API for your REDCap site.
     * @param string $superToken the user's super token. This needs to be provided if
     *     you are going to create projects.
     * @param boolean $sslVerify indicates if SSL connection to REDCap web site should be verified.
     * @param string $caCertificateFile the full path name of the CA (Certificate Authority)
     *     certificate file.
     * @param ErrorHandlerInterface $errorHandler the error handler that will be
     *    used. This would normally only be set if you want to override the PHPCap's default
     *    error handler.
     *
     * @throws JavaRedcapException 
     */
    public RedCap (
    	String apiUrl,
    	String superToken,
    	boolean sslVerify,
        String caCertificateFile,
        ErrorHandlerInterface errorHandler
        ) throws JavaRedcapException
    {
    	this(superToken, new RedCapApiConnection(apiUrl, sslVerify, caCertificateFile, null), errorHandler);
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
     * @param projectData The data used for project creation.
     * @param odm String in CDISC ODM XML format that contains metadata and
     * 		optionally data to be imported into the created project.
     *
     * @return RedCapProject the project that was created.
     * 
     * @throws JavaRedcapException 
     */
    public RedCapProject createProject (List<Map<String, Object>> projectData, String odm) throws JavaRedcapException {
    	RedCapProject result = null;
    	
    	try {
    		// Convert to JSON and send it on.
    		String json = new ObjectMapper().writeValueAsString(projectData);

    		result = createProject(json, RedCapApiFormat.JSON, odm);
    	}
    	catch (JsonProcessingException e) {
    		throw new JavaRedcapException("Exception preparing REDCap request", ErrorHandlerInterface.JSON_ERROR, e);
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
     * @return RedCapProject the project that was created.
     * 
     * @throws JavaRedcapException 
     */
    public RedCapProject createProject (String projectData, RedCapApiFormat format, String odm) throws JavaRedcapException {
        // Note: might want to clone error handler, in case state variables
        // have been added that should differ for different uses, e.g.,
        // a user message that is displayed where you have multiple project
        // objects
    	RedCapApiParams data = new RedCapApiParams(superToken, RedCapApiContent.PROJECT, errorHandler);
       
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
     * @return string the REDCap version number of the REDCap instance being used by the project.
     * @throws JavaRedcapException 
     */
    public String  exportRedcapVersion() throws JavaRedcapException {

        String redcapVersion = connection.call(new RedCapApiParams(superToken, RedCapApiContent.VERSION, errorHandler));

        return redcapVersion;
    }


    /**
     * Gets the REDCap project for the specified API token.
     *
     * @param string $apiToken the API token for the project to get.
     *
     * @return \IU\PHPCap\RedCapProject the project for the specified API token.
     * 
     * @throws JavaRedcapException 
     */
    public RedCapProject getProject(String apiToken) throws JavaRedcapException
    {
        apiToken = RedCap.processApiTokenArgument(apiToken, 32, errorHandler);

        RedCapProject project = projectFactory.createProject(
        		connection.getUrl(),
        		apiToken,
        		connection.getSslVerify(),
        		connection.getCaCertificateFile(),
        		(ErrorHandler)errorHandler.clone(),
        		(RedCapApiConnection)connection.clone()
        		);
        
        return project;
    }
    
    /**
     * Gets the function used to create projects.
     *
     * @return The function used by this class to create projects.
     */
    public RedCapProjectFactory getProjectFactory()
    {
        return projectFactory;
    }
    
    /**
     * Sets the function used to create projects in this class.
     * This method would normally only be used if you have extended
     * the RedCapProject class and want RedCap to return
     * projects using your extended class.
     *
     * @param The function to call to create a new project.
     *     The function will be passed the same arguments as the RedCapProject
     *     constructor gets.
     */
    public void setProjectFactory(RedCapProjectFactory projectFactory)
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
     * @param The error handler to use.
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
    public RedCapApiConnectionInterface getConnection()
    {
        return connection;
    }
    
    /**
     * Sets the connection that is used.
     *
     * @param RedCapApiConnectionInterface $connection the connection to use.
     * @throws JavaRedcapException 
     */
    public void setConnection(RedCapApiConnectionInterface connection) throws JavaRedcapException
    {
    	if (null == connection) {
    		errorHandler.throwException(
    				"The connection argument is not valid",
    				ErrorHandlerInterface.INVALID_ARGUMENT);
        }

    	this.connection = connection;
    }

    public static String processApiTokenArgument(String apiToken, int length, ErrorHandlerInterface errorHandler) throws JavaRedcapException
    {
    	if (null == apiToken || 0 == apiToken.trim().length()) {
    		errorHandler.throwException(
    				"The REDCap API token specified for the project was null or blank.",
    				ErrorHandlerInterface.INVALID_ARGUMENT
    				);
        } else if (!apiToken.matches("^[A-F0-9]+$")) {   // check token for hexidecimal
        	errorHandler.throwException(
        			"The REDCap API token has an invalid format. " +
        			"It should only contain numbers and the letters A, B, C, D, E and F.",
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

    
    protected String processImportDataArgument(String data, String dataName, RedCapApiFormat format) throws JavaRedcapException
    {
        if (null == data) {
        	errorHandler.throwException(
        			"No value specified for required argument '"+dataName+"'.",
        			ErrorHandlerInterface.INVALID_ARGUMENT
        			);
        }

        return data;
    }
    
    protected void checkForRedcapError(String result) throws JavaRedcapException
    {
    	Pattern pattern = Pattern.compile(RedCapProject.JSON_RESULT_ERROR_PATTERN);
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
