package de.intranda.goobi.plugins.step.datacite.data;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import de.intranda.goobi.plugins.step.http.HTTPClient;
import de.intranda.goobi.plugins.step.http.HTTPRequest;
import de.intranda.goobi.plugins.step.http.HTTPResponse;

/**
 * Demonstration of DataCite Content Resolver. 
 * @see <a href="http://data.datacite.org">http://data.datacite.org</a>
 * @author mpaluch
 */
public class DataCiteContentResolver {

	public static final String SERVICE_ADDRESS = "http://data.datacite.org";
	
	public void execute(){
		try{						
			String doi = "10.4224/21268323";
			String mimeType = "application/pdf";
			
			HTTPRequest request = new HTTPRequest();
			request.setMethod(HTTPRequest.Method.GET);
			request.setURL(SERVICE_ADDRESS + "/" + mimeType + "/" + doi);
			
			print(request.toString());
			
			HTTPResponse response = HTTPClient.doHTTPRequest(request);
			print(response.toString());
						
			File file = new File("resource/document.pdf");
			BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(file));
			bout.write(response.getBody());
			bout.close();			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		DataCiteContentResolver exec = new DataCiteContentResolver();
		exec.execute();
		exec = null;
	}

	private void print(String str){
		System.out.println(str);
	}
}
