package de.intranda.goobi.plugins.step.datacite.mds.doi;

import de.intranda.goobi.plugins.step.datacite.mds.AccessObject;
import de.intranda.goobi.plugins.step.doi.DoiException;
import de.intranda.goobi.plugins.step.http.HTTPClient;
import de.intranda.goobi.plugins.step.http.HTTPRequest;
import de.intranda.goobi.plugins.step.http.HTTPResponse;

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

            //String doi = "10.4224/21268323";

            HTTPRequest request = new HTTPRequest();
            request.setMethod(HTTPRequest.Method.GET);
            request.setURL(ao.SERVICE_ADDRESS + "doi/" + doi);
            request.setUsername(ao.USERNAME);
            request.setPassword(ao.PASSWORD);

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
