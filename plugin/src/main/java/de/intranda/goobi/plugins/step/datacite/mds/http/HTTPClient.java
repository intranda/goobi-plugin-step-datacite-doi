package de.intranda.goobi.plugins.step.datacite.mds.http;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.codec.binary.Base64;

/**
 * Generic HTTP client. 
 */
public class HTTPClient {

	private static final int CONNECT_TIMEOUT = 5000;
	private static final int READ_TIMEOUT = 5000;
	
	public static HTTPResponse doHTTPRequest(HTTPRequest request) throws Exception{
		HTTPResponse response = new HTTPResponse();
		
		try{	
			HttpURLConnection conn;
			URL url = new URL(request.getUrl());			

			// Open connection
			if (url.getProtocol().equalsIgnoreCase("https")){
				conn = (HttpsURLConnection)url.openConnection();
			}
			else{
				conn = (HttpURLConnection)url.openConnection();
			}

			// Set request method
			conn.setRequestMethod(request.getMethod().toString());
			
			// Set request parameters
			conn.setDoOutput(request.getBody()!=null);
			conn.setUseCaches(false);				
			conn.setDoInput(true);
			conn.setAllowUserInteraction(false);
			conn.setInstanceFollowRedirects(true);

			// Set request timeouts
			conn.setConnectTimeout(CONNECT_TIMEOUT);
			conn.setReadTimeout(READ_TIMEOUT);

			// Set HTTP Accept header
			if (request.getAccept()!=null){
				conn.setRequestProperty("Accept", request.getAccept());
			}

			// Set request content type
			if (request.getContentType()!=null){
				conn.setRequestProperty("Content-Type", request.getContentType());
			}

			// Add Basic HTTP Authentication
			if (request.getUsername()!=null && request.getPassword()!=null){
				Base64 enc = new Base64(-1);	
				String encAuth = enc.encodeToString((request.getUsername()+":"+request.getPassword()).getBytes("UTF-8"));	
				conn.setRequestProperty("Authorization", "Basic "+encAuth);
			}

			// Add request body
			if (request.getBody()!=null){
				OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream(),"UTF-8");
				osw.write(request.getBody());
				osw.close();
			}

			// Connect
			conn.connect();
			response.setResponseCode(conn.getResponseCode());
			response.setContentType(conn.getHeaderField("Content-Type"));
			
			// Read response
			InputStream in;
			if (response.getResponseCode() >= 400){in = conn.getErrorStream();}
			else{in = conn.getInputStream();}

			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int n;
			while ((n=in.read(buffer))!=-1){
				bout.write(buffer, 0, n);
			}
			
			bout.close();
			
			// Set response body
			response.setBody(bout.toByteArray());			
			
			// Disconnect
			conn.disconnect();
		}
		catch(Exception e){
			throw e;
		}

		return response;
		
	}
	
}
