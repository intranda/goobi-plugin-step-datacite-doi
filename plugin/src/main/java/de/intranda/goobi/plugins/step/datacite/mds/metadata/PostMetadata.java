package de.intranda.goobi.plugins.step.datacite.mds.metadata;

import de.intranda.goobi.plugins.step.doi.DoiException;
import de.intranda.goobi.plugins.step.http.HTTPClient;
import de.intranda.goobi.plugins.step.http.HTTPRequest;
import de.intranda.goobi.plugins.step.http.HTTPResponse;

/**
 * Demo of DataCite Metadata Store API /metadata resource.
 * 
 * @see <a href="http://mds.datacite.org">http://mds.datacite.org</a>
 * @author mpaluch
 */
public class PostMetadata {

    public static final String SERVICE_ADDRESS = "https://mds.datacite.org/metadata/";

    private String USERNAME = "[username]";
    private String PASSWORD = "[password]";

    public PostMetadata(String username, String pw) {
        this.USERNAME = username;
        this.PASSWORD = pw;
    }

    public HTTPResponse forDoi(String doi, String metadata) throws DoiException {
        try {
            String requestBody = metadata; //Metadata.getMetadataFromFile();

            HTTPRequest request = new HTTPRequest();
            request.setMethod(HTTPRequest.Method.POST);
            request.setURL(SERVICE_ADDRESS +  doi);

            request.setUsername(USERNAME);
            request.setPassword(PASSWORD);

            request.setContentType("application/xml;charset=UTF-8");
            request.setBody(requestBody);

            HTTPResponse response = HTTPClient.doHTTPRequest(request);
            return response;
            
        } catch (Exception e) {
            throw new DoiException(e);
        }
    }

}
