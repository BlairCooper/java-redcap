/* --------------------------------------------------------------------------
 * Copyright (C) 2021 University of Utah Health Systems Research & Innovation
 * SPDX-License-Identifier: BSD-3-Clause
 * 
 * Derived from work by The Trustees of Indiana University Copyright (C) 2019 
 * --------------------------------------------------------------------------
 */

package edu.utah.hsir.javaRedcap;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

import edu.utah.hsir.javaRedcap.enums.REDCapApiParameter;

/**
 * Represents a connection to the API of a REDCap instance. This class
 * provides a low-level interface to the REDCap API, and is primarily
 * intended for internal use by JavaRedcap, but could be used directly by a
 * user to access REDCap functionality not provided by JavaRedcap.
 */
public class REDCapApiConnection implements REDCapApiConnectionInterface
{
	/** The default timeout for requests */
	public static final int DEFAULT_TIMEOUT_IN_SECONDS = 1200; // 1,200 seconds = 20 minutes
	
	/** The default timeout of connections to  complete */
	public static final int DEFAULT_CONNECTION_TIMEOUT_IN_SECONDS = 20;
	
	private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT_IN_SECONDS;
	private int timeout = DEFAULT_TIMEOUT_IN_SECONDS;
	private boolean sslVerify = true;
	private String caCertFile = null;

	protected URI serverUri;
	protected HttpClient httpClient;

    /** the error handler for the connection. */
    protected ErrorHandlerInterface errorHandler;

    private REDCapApiConnection() {}

    /**
     * Construct an object used for communicating with the REDCap server.
     *  
     * @param url The REDCap server URL.
     * @param sslVerify If true, the SSL connection with the server will be
     * 		verified.
     * @param caCertificateFile The CA certificate file used to verify the
     * 		connection with the server.
     * 
     * @throws JavaREDCapException Thrown if an error occurs and the Error
     * 		Handler being used throws the exception.
     */
    public REDCapApiConnection (String url, boolean sslVerify, String caCertificateFile)
        throws JavaREDCapException
    {
    	this(url, sslVerify, caCertificateFile, null);
    }
    
    /**
     * Construct an object used for communicating with the REDCap server.
     *  
     * @param url The REDCap server URL.
     * @param sslVerify If true, the SSL connection with the server will be
     * 		verified.
     * @param caCertificateFile The CA certificate file used to verify the
     * 		connection with the server.
     * @param errorHandler An implementation of ErrorHandlerInterface to use
     * 		in handling errors. If null the default error handler will be used.
     *
     * @throws JavaREDCapException Thrown if an error occurs and the Error
     * 		Handler being used throws the exception.
     */
    public REDCapApiConnection (String url, boolean sslVerify, String caCertificateFile, ErrorHandlerInterface errorHandler)
    	throws JavaREDCapException
    {
    	setUrl(url);
    	setSslVerify(sslVerify);
    	setCaCertificateFile(caCertificateFile);
    	setErrorHandler(errorHandler);
    }

/*    
    public function call($data)
    {
        if (!is_string($data) && !is_array($data)) {
            $message = "Data passed to ".__METHOD__." has type ".gettype($data)
                .", but should be a string or an array.";
            $code = ErrorHandlerInterface::INVALID_ARGUMENT;
            $this->errorHandler->throwException($message, $code);
        } // @codeCoverageIgnore
        
        $errno = 0;
        $response = '';
        
        // Post specified data (and do NOT save this in the options array)
        curl_setopt($this->curlHandle, CURLOPT_POSTFIELDS, $data);
        $response = curl_exec($this->curlHandle);
        
        if ($errno = curl_errno($this->curlHandle)) {
            $message = curl_error($this->curlHandle);
            $code    = ErrorHandlerInterface::CONNECTION_ERROR;
            
            # Had one case where curl_error didn't return a message
            if ($message == null || $message == '') {
                $message = curl_strerror($errno);
                if ($message == null || $message == '') {
                    $message = 'Connection error '.$errno.' occurred.';
                }
            }
            $this->errorHandler->throwException($message, $code, $errno);
        } else { // @codeCoverageIgnore
            // Check for HTTP errors
            $httpCode = curl_getinfo($this->curlHandle, CURLINFO_HTTP_CODE);
            if ($httpCode == 301) {
                $callInfo = curl_getinfo($this->curlHandle);
                $message =  "The page for the specified URL ("
                    .$this->getCurlOption(CURLOPT_URL). ") has moved to "
                    .$callInfo ['redirect_url'] . ". Please update your URL.";
                $code = ErrorHandlerInterface::INVALID_URL;
                $this->errorHandler->throwException($message, $code, null, $httpCode);
            } elseif ($httpCode == 404) {
                $message = 'The specified URL ('.$this->getCurlOption(CURLOPT_URL)
                    .') appears to be incorrect. Nothing was found at this URL.';
                $code = ErrorHandlerInterface::INVALID_URL;
                $this->errorHandler->throwException($message, $code, null, $httpCode);
            } // @codeCoverageIgnore
        }
        
        return ($response);
    }
    

    public function callWithArray($dataArray)
    {
        $data = http_build_query($dataArray, '', '&');
        return $this->call($data);
    }
*/
    
    @Override
	public String call(REDCapApiRequest data) throws JavaREDCapException {
    	String result = null;
    	
    	HttpRequest request = HttpRequest.newBuilder()
    		      .uri(serverUri)
    		      .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT_IN_SECONDS))
    		      .header("Content-Type", "application/x-www-form-urlencoded")
    		      .POST(HttpRequest.BodyPublishers.ofString(data.toWwwFormUrlencoded()))
    		      .build();

    	try {
			HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
			
			result = response.body();
		} catch (IOException | InterruptedException ioe) {
			throw new JavaREDCapException(
					"IOException communicating with the server",
					ErrorHandlerInterface.COMMUNICATION_ERROR,
					ioe
					);
		}
    		      
		return result;
	}
    
	@Override
	public String callWithMap(Map<REDCapApiParameter, Object> dataMap) throws JavaREDCapException {
		return call(new REDCapApiRequest(dataMap));
	}
    

	@Override
	public ErrorHandlerInterface getErrorHandler() {
		return errorHandler;
	}

	@Override
	public void setErrorHandler(ErrorHandlerInterface errorHandler) {
    	// If an error handler was specified, use it,
        // otherwise, use the default JavaRedcap error handler
    	if (null == errorHandler) {
            this.errorHandler = new ErrorHandler();
        } else {
            this.errorHandler = errorHandler;
        }
	}

	@Override
	public String getUrl() {
		return serverUri.toString();
	}

	@Override
	public void setUrl(String url) throws JavaREDCapException {
		if (null != url && url.trim().length() != 0) {
			serverUri = URI.create(url);
		}
		else {
			throw new JavaREDCapException("Invalid REDCap URL provided", ErrorHandlerInterface.INVALID_URL);
		}
	}

	@Override
	public boolean getSslVerify() {
		return sslVerify;
	}

	@Override
	public void setSslVerify(boolean sslVerify) {
		this.sslVerify = sslVerify;

		// reset the client so a new one is created on the next request.
		httpClient = null;
	}

    public String getCaCertificateFile()
    {
    	return caCertFile;
    }
    
    public void setCaCertificateFile(String caCertificateFile) throws JavaREDCapException
    {
    	if (null == caCertificateFile) {
    		caCertFile = null;
    	}
    	else {
    		if (caCertificateFile.trim().length() != 0) {
    			File file = new File(caCertificateFile);
    			
    			if (!file.exists()) {
    				throw new JavaREDCapException(
    						"The cert file '" + caCertificateFile + "' does not exist.",
    						ErrorHandlerInterface.CA_CERTIFICATE_FILE_NOT_FOUND
    						);
                }
    			else if (!file.canRead()) {
    				throw new JavaREDCapException(
    						"The cert file '" + caCertificateFile + "' exists, but cannot be read.",
    						ErrorHandlerInterface.CA_CERTIFICATE_FILE_UNREADABLE
    						);
                }
    			
    	    	caCertFile = caCertificateFile;
    		}
    		else {
    			throw new JavaREDCapException("The cert file is not defined", ErrorHandlerInterface.CA_CERTIFICATE_FILE_NOT_FOUND);
    		}
    	}
    			
		// reset the client so a new one is created on the next request.
		httpClient = null;
    }
	
	@Override
	public int getTimeoutInSeconds() {
		return timeout;
	}

	@Override
	public void setTimeoutInSeconds(int timeoutInSeconds) throws JavaREDCapException {
		if (timeoutInSeconds > 1) {
			timeout = timeoutInSeconds;
		}
		else {
			throw new JavaREDCapException("Timeout must be at least 1 second", ErrorHandlerInterface.INVALID_ARGUMENT);
		}
	}

	@Override
	public int getConnectionTimeoutInSeconds() {
		return connectionTimeout;
	}

	@Override
	public void setConnectionTimeoutInSeconds(int connectionTimeoutInSeconds) throws JavaREDCapException {
		if (connectionTimeoutInSeconds > 1) {
			connectionTimeout = connectionTimeoutInSeconds;

			// reset the client so a new one is created on the next request.
			httpClient = null;
		}
		else {
			throw new JavaREDCapException("Timeout must be at elast 1 second", ErrorHandlerInterface.INVALID_ARGUMENT);
		}
	}

	@Override
	public REDCapApiConnectionInterface clone() {
		REDCapApiConnection clone = null;

		try {
			clone = (REDCapApiConnection)super.clone();
		} catch (CloneNotSupportedException e) {
			clone = new REDCapApiConnection();
		}

		clone.errorHandler = (ErrorHandler)errorHandler.clone();
		clone.serverUri = URI.create(serverUri.toString());
		clone.httpClient = httpClient;

		return clone;
	}
    
	
	HttpClient getHttpClient () {
		if (null == httpClient) {
	    	httpClient = HttpClient
	    			.newBuilder()
	    			.connectTimeout(Duration.ofSeconds(connectionTimeout))
	    			.build();
		}
		
		return httpClient;
	}

}
