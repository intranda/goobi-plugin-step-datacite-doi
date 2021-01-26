package de.intranda.goobi.plugins.step.datacite.mds.doi;

import de.intranda.goobi.plugins.step.datacite.mds.AccessObject;
import de.intranda.goobi.plugins.step.doi.DoiException;
import de.intranda.goobi.plugins.step.http.HTTPClient;
import de.intranda.goobi.plugins.step.http.HTTPRequest;
import de.intranda.goobi.plugins.step.http.HTTPResponse;

/**
 * DataCite Metadata Store API /doi resource. 
 */
public class PostDOI {

    private AccessObject ao;
	
    public PostDOI(AccessObject ao) {
        this.ao = ao;
    }
    
	public HTTPResponse newURL(String doi, String url) throws DoiException{
		try{						
			//Note: To successfully POST a DOI, its metadata must be POSTed first (/metadata resource)			
			String contentType = "text/plain;charset=UTF-8";
			
//			String url = "http://url.test.doi";
//			String doi = "10.5072/testing.doi.post.1";
			
			String requestBody = "";
			requestBody += "doi=" + doi;
			requestBody += "\n";
			requestBody += "url=" + url;
						
			HTTPRequest request = new HTTPRequest();
			request.setMethod(HTTPRequest.Method.POST);
			request.setURL(ao.SERVICE_ADDRESS);
			request.setUsername(ao.USERNAME);
			request.setPassword(ao.PASSWORD);
			
			request.setContentType(contentType);
			request.setBody(requestBody);
			
			HTTPResponse response = HTTPClient.doHTTPRequest(request);
			return response;
		}
		catch(Exception e){
			throw new DoiException(e);
		}
	}

}
