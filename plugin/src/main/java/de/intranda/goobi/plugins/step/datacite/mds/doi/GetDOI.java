package de.intranda.goobi.plugins.step.datacite.mds.doi;

import de.intranda.goobi.plugins.step.doi.DoiException;
import de.intranda.goobi.plugins.step.http.HTTPClient;
import de.intranda.goobi.plugins.step.http.HTTPRequest;
import de.intranda.goobi.plugins.step.http.HTTPResponse;

/**
 * Demonstration of DataCite Metadata Store API /doi resource.
 * 
 * @see <a href="http://mds.datacite.org">http://mds.datacite.org</a>
 * @author mpaluch
 */
public class GetDOI {

    public static final String SERVICE_ADDRESS = "https://mds.datacite.org/doi/";
    private String USERNAME = "[username]";
    private String PASSWORD = "[password]";

    public GetDOI(String username, String pw) {
        this.USERNAME = username;
        this.PASSWORD = pw;
    }

    public HTTPResponse ifExists(String doi) throws DoiException {
        try {

            //String doi = "10.4224/21268323";

            HTTPRequest request = new HTTPRequest();
            request.setMethod(HTTPRequest.Method.GET);
            request.setURL(SERVICE_ADDRESS + doi);
            request.setUsername(USERNAME);
            request.setPassword(PASSWORD);

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

    //	public static void main(String[] args) {
    //		GetDOI exec = new GetDOI();
    //		exec.execute();
    //		exec = null;
    //	}
    //
    //	private void print(String str){
    //		System.out.println(str);
    //	}
}
