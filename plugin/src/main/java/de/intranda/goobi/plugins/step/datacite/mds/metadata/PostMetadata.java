package de.intranda.goobi.plugins.step.datacite.mds.metadata;

import de.intranda.goobi.plugins.step.datacite.mds.AccessObject;
import de.intranda.goobi.plugins.step.doi.DoiException;
import de.intranda.goobi.plugins.step.http.HTTPClient;
import de.intranda.goobi.plugins.step.http.HTTPRequest;
import de.intranda.goobi.plugins.step.http.HTTPResponse;

/**
 * DataCite Metadata Store API /metadata resource.
 * 
 */
public class PostMetadata {  
    
    private AccessObject ao;

    public PostMetadata(AccessObject ao) {
        this.ao = ao;
    }

    public HTTPResponse forDoi(String doi, String metadata) throws DoiException {
        try {
            String requestBody = metadata;  

            HTTPRequest request = new HTTPRequest();
            request.setMethod(HTTPRequest.Method.POST);
            request.setURL(ao.SERVICE_ADDRESS + "metadata/" +  doi);

            request.setUsername(ao.USERNAME);
            request.setPassword(ao.PASSWORD);

            request.setContentType("application/xml;charset=UTF-8");
            request.setBody(requestBody);

            HTTPResponse response = HTTPClient.doHTTPRequest(request);
            return response;
            
        } catch (Exception e) {
            throw new DoiException(e);
        }
    }

    public HTTPResponse forUpdatingDoi(String doi, String metadata) throws DoiException {
        try {
            String requestBody = metadata;  

            HTTPRequest request = new HTTPRequest();
            request.setMethod(HTTPRequest.Method.PUT);
            request.setURL(ao.SERVICE_ADDRESS + "metadata/" +  doi);

            request.setUsername(ao.USERNAME);
            request.setPassword(ao.PASSWORD);

            request.setContentType("application/xml;charset=UTF-8");
            request.setBody(requestBody);

            HTTPResponse response = HTTPClient.doHTTPRequest(request);
            return response;
            
        } catch (Exception e) {
            throw new DoiException(e);
        }
    }
}
