package de.intranda.goobi.plugins.step.datacite.mds.doi;

import de.intranda.goobi.plugins.step.datacite.mds.AccessObject;
import de.intranda.goobi.plugins.step.doi.DoiException;
import de.intranda.goobi.plugins.step.doi.DoiHandler;
import de.intranda.goobi.plugins.step.http.HTTPClient;
import de.intranda.goobi.plugins.step.http.HTTPRequest;
import de.intranda.goobi.plugins.step.http.HTTPResponse;
import lombok.extern.log4j.Log4j;

/**
 * DataCite Metadata Store API /doi resource. 
 */
@Log4j
public class PostDOI {

    private AccessObject ao;
	
    public PostDOI(AccessObject ao) {
        this.ao = ao;
    }
    
	public HTTPResponse newURL(String handle, String url) throws DoiException{
		try{						
			//Note: To successfully POST a DOI, its metadata must be POSTed first (/metadata resource)			
			String contentType = "text/plain;charset=UTF-8";
			
			String requestBody = "";
			requestBody += "doi=" + handle;
			requestBody += "\n";
			requestBody += "url=" + url;
						
			HTTPRequest request = new HTTPRequest();
			request.setMethod(HTTPRequest.Method.PUT);
			request.setURL(ao.SERVICE_ADDRESS + "doi/" + handle);
			request.setUsername(ao.USERNAME);
			request.setPassword(ao.PASSWORD);
			
			request.setContentType(contentType);
			request.setBody(requestBody);
			
			HTTPResponse response = HTTPClient.doHTTPRequest(request);
			
            if (response.getResponseCode() != HTTPResponse.CREATED) {
                log.debug(request.toString());
                log.debug(response.toString());
            }
            
			return response;
		}
		catch(Exception e){
			throw new DoiException(e);
		}
	}

}
