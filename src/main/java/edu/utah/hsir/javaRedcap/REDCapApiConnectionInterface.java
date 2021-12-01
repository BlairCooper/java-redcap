/* --------------------------------------------------------------------------
 * Copyright (C) 2021 University of Utah Health Systems Research & Innovation
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 * Derived from work by The Trustees of Indiana University Copyright (C) 2019 
 * --------------------------------------------------------------------------
 */

package edu.utah.hsir.javaRedcap;

import java.util.Map;

import edu.utah.hsir.javaRedcap.enums.REDCapApiParameter;

/**
 * Interface for connection to the API of a REDCap instance.
 * Classes implementing this interface are used to provide low-level
 * access to the REDCap API.
 */
public interface REDCapApiConnectionInterface extends Cloneable
{
    /**
     * Makes a call to REDCap's API and returns the results.
     *
     * @param data The parameters for the call.
     *         
     * @return The response returned by the REDCap API for the specified call data.
     *         See the REDCap API documentation for more information.

     * @throws JavaREDCapException Thrown for an error completing the request. 
     */
    public String call (REDCapApiRequest data) throws JavaREDCapException;
    
    /**
     * Calls REDCap's API using a with a correctly formatted string version
     * of the specified data and returns the results.
     *
     * @param dataMap A map of data that is converted to a string and then
     * 		passed to the REDCap API.
     * 
     * @return The response returned by the REDCap API for the specified call data.
     *         See the REDCap API documentation for more information.
     *         
     * @throws JavaREDCapException Thrown for any errors in completing the request.
     */

    public String callWithMap(Map<REDCapApiParameter, Object> dataMap) throws JavaREDCapException;
    
    /**
     * Gets the error handler for the connection.
     *
     * @return ErrorHandlerInterface the error handler for the connection.
     */
    public ErrorHandlerInterface getErrorHandler();
    
    /**
     * Sets the error handler;
     *
     * @param errorHandler The error handler to use.
     */
    public void setErrorHandler(ErrorHandlerInterface errorHandler);

    /**
     * Gets the URL of the connection.
     *
     * @return Te URL of the connection.
     */
    public String getUrl();
    
    /**
     * Sets the URL of the connection.
     *
     * @param url The URL of the connection.
     * 
     * @throws JavaREDCapException Thrown if there is an issue with the URL. 
     */
    public void setUrl(String url) throws JavaREDCapException;
    
    /**
     * Gets the status of SSL verification for the connection.
     *
     * @return Returns true if SSL verification is enabled, and false otherwise.
     */
    public boolean getSslVerify();
    
    /**
     * Sets SSL verification for the connection.
     *
     * @param sslVerify If this is true, then the site being connected to will
     *     have its SSL certificate verified.
     */
    public void setSslVerify(boolean sslVerify);
    

    /**
     * Fetch the current certificate file.
     *  
     * @return The current certificate file being used, or null if no file set.
     */
    public String getCaCertificateFile(); 

    /**
     * Set the CA certificate file to use in validating connections to the server.
     *  
     * @param caCertificateFile A fully qualified name to the CA Certificate file to use.
     * 
     * @throws JavaREDCapException Thrown if the file cannot be found or cannot be read.
     */
    public void setCaCertificateFile(String caCertificateFile) throws JavaREDCapException;
    
    /**
     * Gets the timeout in seconds for calls to the connection.
     *
     * @return integer timeout in seconds for calls to connection.
     */
    public int getTimeoutInSeconds();
    
    
    /**
     * Sets the timeout in seconds for calls to the connection.
     *
     * @param timeoutInSeconds The timeout in seconds for call to connection.
     * 
     * @throws JavaREDCapException Thrown if the value specified is invalid. 
     */
    public void setTimeoutInSeconds(int timeoutInSeconds) throws JavaREDCapException;
    
    /**
     * Gets the timeout for time to make a connection in seconds.
     *
     * @return The current connection timeout in seconds.
     */
    public int getConnectionTimeoutInSeconds();
    
    /**
     * Sets the timeout for time to make a connection in seconds.
     *
     * @param connectionTimeoutInSeconds A timeout in seconds.
     * 
     * @throws JavaREDCapException Thrown if the value specified is invalid. 
     */
    public void setConnectionTimeoutInSeconds(int connectionTimeoutInSeconds) throws JavaREDCapException;
    
    /**
     * Create a copy of the object
     * 
     * @return A cloned copy of the connection object. 
     */
    public Object clone();
}
