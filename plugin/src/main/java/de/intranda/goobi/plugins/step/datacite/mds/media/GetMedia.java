package de.intranda.goobi.plugins.step.datacite.mds.media;

import de.intranda.goobi.plugins.step.http.HTTPClient;
import de.intranda.goobi.plugins.step.http.HTTPRequest;
import de.intranda.goobi.plugins.step.http.HTTPResponse;

/**
 * Demonstration of DataCite Metadata Store API /media resource. 
 * @see <a href="http://mds.datacite.org">http://mds.datacite.org</a>
 * @author mpaluch
 */
public class GetMedia {

	public static final String SERVICE_ADDRESS = "https://mds.datacite.org/media/";
	public static final String USERNAME = "[username]";
	public static final String PASSWORD = "[password]";	
	
	public void execute(){
		try{			
			//At time of creation, this DOI has an application/pdf media entry. 
			String doi = "10.4224/21268323";
			
			HTTPRequest request = new HTTPRequest();
			request.setMethod(HTTPRequest.Method.GET);
			request.setURL(SERVICE_ADDRESS + doi);
			request.setUsername(USERNAME);
			request.setPassword(PASSWORD);
			
			print(request.toString());
			
			HTTPResponse response = HTTPClient.doHTTPRequest(request);
			print(response.toString());
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		GetMedia exec = new GetMedia();
		exec.execute();
		exec = null;
	}

	private void print(String str){
		System.out.println(str);
	}
}
