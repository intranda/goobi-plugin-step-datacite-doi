package de.intranda.goobi.plugins.step.datacite.mds.metadata;

import de.intranda.goobi.plugins.step.http.HTTPClient;
import de.intranda.goobi.plugins.step.http.HTTPRequest;
import de.intranda.goobi.plugins.step.http.HTTPResponse;

/**
 * Demo of DataCite Metadata Store API /metadata resource. 
 * @see <a href="http://mds.datacite.org">http://mds.datacite.org</a>
 * @author mpaluch
 */
public class DeleteMetadata {

	public static final String SERVICE_ADDRESS = "https://mds.datacite.org/metadata/";
	public static final String USERNAME = "[username]";
	public static final String PASSWORD = "[password]";
	
	public void execute(){
		try{			
			String doi = "10.5072/testing.doi.post.1";
			
			HTTPRequest request = new HTTPRequest();
			request.setMethod(HTTPRequest.Method.DELETE);
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
		DeleteMetadata exec = new DeleteMetadata();
		exec.execute();
		exec = null;
	}

	private void print(String str){
		System.out.println(str);
	}
}
