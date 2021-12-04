package de.intranda.goobi.plugins.step.datacite.mds.metadata;

import de.intranda.goobi.plugins.step.datacite.mds.AccessObject;
import de.intranda.goobi.plugins.step.datacite.mds.http.HTTPClient;
import de.intranda.goobi.plugins.step.datacite.mds.http.HTTPRequest;
import de.intranda.goobi.plugins.step.datacite.mds.http.HTTPResponse;
import de.intranda.goobi.plugins.step.doi.DoiException;
import lombok.extern.log4j.Log4j;

/**
 * DataCite Metadata Store API /metadata resource.
 * 
 */
@Log4j
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
            request.setUrl(ao.getServiceAddress() + "metadata/" + doi);
            request.setUsername(ao.getUsername());
            request.setPassword(ao.getPassword());
            request.setContentType("application/xml;charset=UTF-8");
            request.setBody(requestBody);
            log.debug("Posting metadata for doi ");
            log.debug(requestBody);
            
            HTTPResponse response = HTTPClient.doHTTPRequest(request);
            log.debug(response.toString());
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
            request.setUrl(ao.getServiceAddress() + "metadata/" + doi);
            request.setUsername(ao.getUsername());
            request.setPassword(ao.getPassword());
            request.setContentType("application/xml;charset=UTF-8");
            request.setBody(requestBody);

            HTTPResponse response = HTTPClient.doHTTPRequest(request);
            if (response.getResponseCode() != HTTPResponse.CREATED) {
                log.debug(request.toString());
                log.debug(response.toString());
            }

            return response;
        } catch (Exception e) {
            throw new DoiException(e);
        }
    }
}
