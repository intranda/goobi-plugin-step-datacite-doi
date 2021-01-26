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
public class GetMetadata {

    public static final String SERVICE_ADDRESS = "https://mds.datacite.org/metadata/";
    private  String USERNAME = "[username]";
    private  String PASSWORD = "[password]";

    public GetMetadata(String username, String pw) {
        this.USERNAME = username;
        this.PASSWORD = pw;
    }
    
    public HTTPResponse forDoi(String doi) throws DoiException {
        try {

            // doi = "10.5072/testing.doi.post.1";

            HTTPRequest request = new HTTPRequest();
            request.setMethod(HTTPRequest.Method.GET);
            request.setURL(SERVICE_ADDRESS + doi);
            request.setUsername(USERNAME);
            request.setPassword(PASSWORD);
            request.setAccept("application/xml");

            HTTPResponse response = HTTPClient.doHTTPRequest(request);

            return response;
            
        } catch (Exception e) {
           throw new DoiException(e); 
        }
    }

//    public static void main(String[] args) {
//        GetMetadata exec = new GetMetadata();
//        exec.execute();
//        exec = null;
//    }
//
//    private void print(String str) {
//        System.out.println(str);
//    }
}
