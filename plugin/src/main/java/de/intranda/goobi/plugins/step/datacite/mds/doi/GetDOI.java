package de.intranda.goobi.plugins.step.datacite.mds.doi;

import de.intranda.goobi.plugins.step.datacite.mds.AccessObject;
import de.intranda.goobi.plugins.step.datacite.mds.http.HTTPClient;
import de.intranda.goobi.plugins.step.datacite.mds.http.HTTPRequest;
import de.intranda.goobi.plugins.step.datacite.mds.http.HTTPResponse;
import de.intranda.goobi.plugins.step.doi.DoiException;

/**
 * DataCite Metadata Store API /doi resource.
 * 
 */
public class GetDOI {

    private AccessObject ao;
    
    public GetDOI(AccessObject ao) {
        this.ao = ao;
    }

    public HTTPResponse ifExists(String doi) throws DoiException {
        try {

            HTTPRequest request = new HTTPRequest();
            request.setMethod(HTTPRequest.Method.GET);
            request.setUrl(ao.getServiceAddress() + "doi/" + doi);
            request.setUsername(ao.getUsername());
            request.setPassword(ao.getPassword());
            
            HTTPResponse response = HTTPClient.doHTTPRequest(request);
            if (response.getResponseCode() != HTTPResponse.OK) {
                return null;
            }

            //otherwise
            return response;

        } catch (Exception e) {
            throw new DoiException(e);
        }
    }
}
