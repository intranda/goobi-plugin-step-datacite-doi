package de.intranda.goobi.plugins.step.datacite.data;

import de.intranda.goobi.plugins.step.http.HTTPClient;
import de.intranda.goobi.plugins.step.http.HTTPRequest;
import de.intranda.goobi.plugins.step.http.HTTPResponse;

/**
 * Demonstration of DOI Citation Formatter. 
 * @see <a href="http://www.crosscite.org/cn/#sec-4-1">http://www.crosscite.org/cn/#sec-4-1</a>
 * @author mpaluch
 */
public class CitationFormatter {

	public static final String SERVICE_ADDRESS = "http://doi.org/";
	
	public void execute(){
		try{			
			
			//String doi = "10.1126/science.169.3946.635";	/** CrossRef DOI */
			String doi = "10.4224/21268323";	/** DataCite DOI */
			
			StringBuilder accept = new StringBuilder("text/x-bibliography;");	
						
			//String style = "apa";
			String style = "harvard3";
			
			//String locale = "en-US";
			String locale = "fr-FR";

			accept.append(" style=").append(style).append(";");
			accept.append(" locale=").append(locale).append(";");
			
			HTTPRequest request = new HTTPRequest();
			request.setMethod(HTTPRequest.Method.GET);
			request.setURL(SERVICE_ADDRESS + doi);
			request.setAccept(accept.toString());
			
			print(request.toString());
			
			HTTPResponse response = HTTPClient.doHTTPRequest(request);
			print(response.toString());
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		CitationFormatter exec = new CitationFormatter();
		exec.execute();
		exec = null;
	}

	private void print(String str){
		System.out.println(str);
	}
}
