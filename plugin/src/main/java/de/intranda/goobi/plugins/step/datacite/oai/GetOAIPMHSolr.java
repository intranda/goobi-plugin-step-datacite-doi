package de.intranda.goobi.plugins.step.datacite.oai;

import org.apache.commons.codec.binary.Base64;

import de.intranda.goobi.plugins.step.http.HTTPClient;
import de.intranda.goobi.plugins.step.http.HTTPRequest;
import de.intranda.goobi.plugins.step.http.HTTPResponse;

/**
 * Demo of DataCite OAI-PMH Provider with arbitrary Solr query 
 * @see <a href="http://oai.datacite.org">http://oai.datacite.org</a>
 * @author mpaluch
 */
public class GetOAIPMHSolr {

	public static final String SERVICE_ADDRESS = "http://oai.datacite.org/oai";
	
	public void execute(){
		try{			
			String verb = "ListRecords";
			String metadataPrefix = "oai_dc";

			byte[] bytes = Base64.encodeBase64URLSafe("q=ocean+ice".getBytes());
			String solrQuery = new String(bytes);
			
			StringBuilder address = new StringBuilder();
			address.append(SERVICE_ADDRESS);
			address.append("?verb=").append(verb);
			address.append("&metadataPrefix=").append(metadataPrefix);
			address.append("&set=").append("~").append(solrQuery);
						
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
		GetOAIPMHSolr exec = new GetOAIPMHSolr();
		exec.execute();
		exec = null;
	}

	private void print(String str){
		System.out.println(str);
	}
}
