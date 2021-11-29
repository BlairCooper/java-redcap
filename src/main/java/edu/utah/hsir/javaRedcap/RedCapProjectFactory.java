package edu.utah.hsir.javaRedcap;

public interface RedCapProjectFactory {
	public RedCapProject createProject (
			String apiUrl,
    		String apiToken,
    		boolean sslVerify,
    		String caCertificateFile,
    		ErrorHandlerInterface errorHandler,
    		RedCapApiConnectionInterface connection
    		) throws JavaRedcapException;
}
