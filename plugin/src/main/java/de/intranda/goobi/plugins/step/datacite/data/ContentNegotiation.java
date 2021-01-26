package de.intranda.goobi.plugins.step.datacite.data;

import de.intranda.goobi.plugins.step.http.HTTPClient;
import de.intranda.goobi.plugins.step.http.HTTPRequest;
import de.intranda.goobi.plugins.step.http.HTTPResponse;

/**
 * Demonstration of DOI Content Negotiation. 
 * @see <a href="http://www.crosscite.org/cn/">http://www.crosscite.org/cn/</a>
 * @author mpaluch
 */
public class ContentNegotiation {

	public static final String SERVICE_ADDRESS = "http://doi.org/";
	
	public void execute(){
		try{			
			
			String doi = "10.1126/science.169.3946.635";	/** CrossRef DOI */
			//String doi = "10.4224/21268323";	/** DataCite DOI */

			String accept = "application/x-research-info-systems";	/** RIS */
			//String accept = "application/rdf+xml";	/** RDF XML */
			//String accept = "text/turtle";	/** RDF Turtle */
			//String accept = "application/vnd.citationstyles.csl+json";	/** Citeproc JSON */
			//String accept = "text/x-bibliography";	/** Formatted text citation */			
			//String accept = "application/x-bibtex";	/** BibTeX */
			//String accept = "application/vnd.crossref.unixref+xml";	/** CrossRef Unixref XML */
			//String accept = "application/vnd.datacite.datacite+xml";	/** DataCite Metadata Kernel */
			
			HTTPRequest request = new HTTPRequest();
			request.setMethod(HTTPRequest.Method.GET);
			request.setURL(SERVICE_ADDRESS+doi);
			request.setAccept(accept);
			
			print(request.toString());
			
			HTTPResponse response = HTTPClient.doHTTPRequest(request);
			print(response.toString());
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		ContentNegotiation exec = new ContentNegotiation();
		exec.execute();
		exec = null;
	}

	private void print(String str){
		System.out.println(str);
	}
}
