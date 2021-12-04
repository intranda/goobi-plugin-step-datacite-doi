package de.intranda.goobi.plugins.step.datacite.mds.metadata;

import de.intranda.goobi.plugins.step.datacite.mds.AccessObject;
import de.intranda.goobi.plugins.step.datacite.mds.http.HTTPClient;
import de.intranda.goobi.plugins.step.datacite.mds.http.HTTPRequest;
import de.intranda.goobi.plugins.step.datacite.mds.http.HTTPResponse;
import de.intranda.goobi.plugins.step.doi.DoiException;

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

            HTTPRequest request = new HTTPRequest();
            request.setMethod(HTTPRequest.Method.GET);
            request.setUrl(ao.getServiceAddress() + "metadata/"+ doi);
            request.setUsername(ao.getUsername());
            request.setPassword(ao.getPassword());
            request.setAccept("application/xml");

            HTTPResponse response = HTTPClient.doHTTPRequest(request);
            return response;
            
        } catch (Exception e) {
           throw new DoiException(e); 
        }
    }

}
