package de.intranda.goobi.plugins.step.datacite.mds.metadata;

import de.intranda.goobi.plugins.step.datacite.mds.AccessObject;
import de.intranda.goobi.plugins.step.doi.DoiException;
import de.intranda.goobi.plugins.step.http.HTTPClient;
import de.intranda.goobi.plugins.step.http.HTTPRequest;
import de.intranda.goobi.plugins.step.http.HTTPResponse;

/**
 * DataCite Metadata Store API /metadata resource.
 */
public class GetMetadata {

    private AccessObject ao;

    public GetMetadata(AccessObject ao) {
        this.ao = ao;
    }
    
    public HTTPResponse forDoi(String doi) throws DoiException {
        try {

            // doi = "10.5072/testing.doi.post.1";

            HTTPRequest request = new HTTPRequest();
            request.setMethod(HTTPRequest.Method.GET);
            request.setURL(ao.SERVICE_ADDRESS + doi);
            request.setUsername(ao.USERNAME);
            request.setPassword(ao.PASSWORD);
            request.setAccept("application/xml");

            HTTPResponse response = HTTPClient.doHTTPRequest(request);

            return response;
            
        } catch (Exception e) {
           throw new DoiException(e); 
        }
    }

}
