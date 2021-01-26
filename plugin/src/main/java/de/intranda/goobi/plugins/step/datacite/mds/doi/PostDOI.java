package de.intranda.goobi.plugins.step.datacite.mds.doi;

import de.intranda.goobi.plugins.step.doi.DoiException;
import de.intranda.goobi.plugins.step.http.HTTPClient;
import de.intranda.goobi.plugins.step.http.HTTPRequest;
import de.intranda.goobi.plugins.step.http.HTTPResponse;

/**
 * Demonstration of DataCite Metadata Store API /doi resource. 
 * @see <a href="http://mds.datacite.org">http://mds.datacite.org</a>
 * @author mpaluch
 */
public class PostDOI {

	public static final String SERVICE_ADDRESS = "https://mds.datacite.org/doi/";
	private String USERNAME = "[username]";
	private String PASSWORD = "[password]";
	
    public PostDOI(String username, String pw) {
        this.USERNAME = username;
        this.PASSWORD = pw;
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
			request.setURL(SERVICE_ADDRESS);
			request.setUsername(USERNAME);
			request.setPassword(PASSWORD);
			
			request.setContentType(contentType);
			request.setBody(requestBody);
			
			HTTPResponse response = HTTPClient.doHTTPRequest(request);
			return response;
		}
		catch(Exception e){
			throw new DoiException(e);
		}
	}

//	public static void main(String[] args) {
//		PostDOI exec = new PostDOI();
//		exec.execute();
//		exec = null;
//	}
//
//	private void print(String str){
//		System.out.println(str);
//	}
}
