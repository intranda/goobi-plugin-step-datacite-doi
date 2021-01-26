package de.intranda.goobi.plugins.step.datacite.oai;

import de.intranda.goobi.plugins.step.http.HTTPClient;
import de.intranda.goobi.plugins.step.http.HTTPRequest;
import de.intranda.goobi.plugins.step.http.HTTPResponse;

/**
 * Demo of DataCite OAI-PMH Provider 
 * @see <a href="http://oai.datacite.org">http://oai.datacite.org</a>
 * @author mpaluch
 */
public class GetOAIPMH {

	public static final String SERVICE_ADDRESS = "http://oai.datacite.org/oai";
	
	public void execute(){
		try{			
			String verb = "ListRecords";
			String metadataPrefix = "oai_dc";

			StringBuilder address = new StringBuilder();
			address.append(SERVICE_ADDRESS);
			address.append("?verb=").append(verb);
			address.append("&metadataPrefix=").append(metadataPrefix);
						
			HTTPRequest request = new HTTPRequest();
			request.setMethod(HTTPRequest.Method.GET);
			request.setURL(address.toString());
			
			print(request.toString());
			
			HTTPResponse response = HTTPClient.doHTTPRequest(request);
			print(response.toString());
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		GetOAIPMH exec = new GetOAIPMH();
		exec.execute();
		exec = null;
	}

	private void print(String str){
		System.out.println(str);
	}
}
