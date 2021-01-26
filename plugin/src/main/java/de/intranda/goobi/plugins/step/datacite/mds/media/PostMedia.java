package de.intranda.goobi.plugins.step.datacite.mds.media;

import de.intranda.goobi.plugins.step.http.HTTPClient;
import de.intranda.goobi.plugins.step.http.HTTPRequest;
import de.intranda.goobi.plugins.step.http.HTTPResponse;

/**
 * Demonstration of DataCite Metadata Store API /media resource. 
 * @see <a href="http://mds.datacite.org">http://mds.datacite.org</a>
 * @author mpaluch
 */
public class PostMedia {

	public static final String SERVICE_ADDRESS = "https://mds.datacite.org/media/";
	public static final String USERNAME = "[username]";
	public static final String PASSWORD = "[password]";	
	
	public void execute(){
		try{			
			
			String doi = "10.5072/testing.doi.post.1";
			
			String contentType = "text/plain;charset=UTF-8";			
			String requestBody = "";
			requestBody += "text/plain=http://url.to.plain.text.version";
			requestBody += "\n";
			requestBody += "application/pdf=http://url.to.pdf.version";
			
			HTTPRequest request = new HTTPRequest();
			request.setMethod(HTTPRequest.Method.POST);
			request.setURL(SERVICE_ADDRESS + doi);
			request.setUsername(USERNAME);
			request.setPassword(PASSWORD);
						
			request.setContentType(contentType);
			request.setBody(requestBody);
			
			print(request.toString());
			
			HTTPResponse response = HTTPClient.doHTTPRequest(request);
			print(response.toString());
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		PostMedia exec = new PostMedia();
		exec.execute();
		exec = null;
	}

	private void print(String str){
		System.out.println(str);
	}
}
