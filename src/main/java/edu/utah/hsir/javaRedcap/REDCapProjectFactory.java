package edu.utah.hsir.javaRedcap;

/**
 * Interface for creating REDCapProject instances.
 *
 */
public interface REDCapProjectFactory {
	/**
	 * Create an instance of REDCapProject using the provided parameters.
	 * 
	 * When the connection parameter is provided, the implementation of the
	 * interface will determine if it is used or an new connection is
	 * constructed using the apiUrl parameter.
	 * 
	 * @param apiUrl The URL to the REDCap server
	 * @param apiToken	The API Token to be used in making requests to the server.
     * @param sslVerify If true, the SSL connection with the server will be
     * 		verified.
     * @param caCertificateFile The CA certificate file used to verify the
     * 		connection with the server.
     * @param errorHandler An implementation of ErrorHandlerInterface to use
     * 		in handling errors. If null the default error handler will be used.
	 * @param connection A connection object to be used in communicating with
	 * 		the REDCap server.  
     * 
	 * @return A newly created REDCapProject instance.
	 * 
	 * @throws JavaREDCapException Thrown if there is an issue with one of
	 * 		the parameters or a problem creating the REDCapProject instance.
	 */
	public REDCapProject createProject (
			String apiUrl,
    		String apiToken,
    		boolean sslVerify,
    		String caCertificateFile,
    		ErrorHandlerInterface errorHandler,
    		REDCapApiConnectionInterface connection
    		) throws JavaREDCapException;
}
