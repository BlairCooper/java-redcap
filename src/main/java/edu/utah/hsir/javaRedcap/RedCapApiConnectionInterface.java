/* --------------------------------------------------------------------------
 * Copyright (C) 2021 University of Utah Health Systems Research & Innovation
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 * Derived from work by The Trustees of Indiana University Copyright (C) 2019 
 * --------------------------------------------------------------------------
 */

package edu.utah.hsir.javaRedcap;

import java.util.Map;

/**
 * Interface for connection to the API of a REDCap instance.
 * Classes implementing this interface are used to provide low-level
 * access to the REDCap API.
 */
public interface RedCapApiConnectionInterface extends Cloneable
{
    /**
     * Constructor that creates a REDCap API connection for the specified URL, with the
     * specified settings.
     *
     * @param string url
     *            the URL for the API of the REDCap site that you want to connect to.
     * @param boolean sslVerify indicates if verification should be done for the SSL
     *            connection to REDCap. Setting this to false is not secure.
     * @param string caCertificateFile
     *            the CA (Certificate Authority) certificate file used for veriying the REDCap site's
     *            SSL certificate (i.e., for verifying that the REDCap site that is
     *            connected to is the one specified).
     * @param ErrorHandlerInterface errorHandler the error handler for the connection.
     */
    // public RedCapApiConnectionInterface (String url, boolean sslVerify, String caCertificateFile, ErrorHandlerInterface errorHandler);

    /**
     * Makes a call to REDCap's API and returns the results.
     *
     * @param mixed data data for the call.
     *         
     * @throws PhpCapException
     * 
     * @return string the response returned by the REDCap API for the specified call data.
     *         See the REDCap API documentation for more information.
     */
    public String call (RedCapApiParams data) throws JavaRedcapException;
    
    /**
     * Calls REDCap's API using a with a correctly formatted string version
     * of the specified data and returns the results.
     *
     * @param dataMap A map of data that is converted to a string and then
     * 		passed to the REDCap API. Keys in the map are assumed to be REDCap API parameters.
     * 
     * @return string the response returned by the REDCap API for the specified call data.
     *         See the REDCap API documentation for more information.
     *         
     * @throws JavaRedcapException
     */
    public String callWithMap(Map<String, Object> dataMap) throws JavaRedcapException;
    
    /**
     * Gets the error handler for the connection.
     *
     * return ErrorHandlerInterface the error handler for the connection.
     */
    public ErrorHandlerInterface getErrorHandler();
    
    /**
     * Sets the error handler;
     *
     * @param ErrorHandlerInterface errorHandler the error handler to use.
     */
    public void setErrorHandler(ErrorHandlerInterface errorHandler);

    /**
     * Gets the URL of the connection.
     *
     * return string the URL of the connection.
     */
    public String getUrl();
    
    /**
     * Sets the URL of the connection.
     *
     * @param string url the URL of the connection.
     * @throws JavaRedcapException 
     */
    public void setUrl(String url) throws JavaRedcapException;
    
    /**
     * Gets the status of SSL verification for the connection.
     *
     * @return boolean true if SSL verification is enabled, and false otherwise.
     */
    public boolean getSslVerify();
    
    /**
     * Sets SSL verification for the connection.
     *
     * @param boolean sslVerify if this is true, then the site being connected to will
     *     have its SSL certificate verified.
     */
    public void setSslVerify(boolean sslVerify);
    
    
    public String getCaCertificateFile(); 
    
    public void setCaCertificateFile(String caCertificateFile) throws JavaRedcapException;
    
    /**
     * Gets the timeout in seconds for calls to the connection.
     *
     * @return integer timeout in seconds for calls to connection.
     */
    public int getTimeoutInSeconds();
    
    
    /**
     * Sets the timeout in seconds for calls to the connection.
     *
     * @param timeoutInSeconds timeout in seconds for call to connection.
     * @throws JavaRedcapException 
     */
    public void setTimeoutInSeconds(int timeoutInSeconds) throws JavaRedcapException;
    
    /**
     * Gets the timeout for time to make a connection in seconds.
     *
     * @return integer connection timeout in seconds.
     */
    public int getConnectionTimeoutInSeconds();
    
    /**
     * Sets the timeout for time to make a connection in seconds.
     *
     * @param integer connection timeout in seconds.
     * @throws JavaRedcapException 
     */
    public void setConnectionTimeoutInSeconds(int connectionTimeoutInSeconds) throws JavaRedcapException;
    
    /**
     * Create a copy of the object
     * 
     * @return 
     */
    public Object clone();
}
