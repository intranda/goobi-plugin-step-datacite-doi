package de.intranda.goobi.plugins.step.http;

/**
 * Encapsulates a HTTP request
 */
public final class HTTPRequest
{
	/*********************************************************************************************
	 * VARIABLE DECLARATIONS                                                                     *
	 *********************************************************************************************/
	public enum Method
	{
		DELETE,
		GET,
		POST,
		PUT,		
	}

	// The HTTP method
	private Method method;

	// The URL where request is sent
	private String url;

	// The accept header 
	private String accept;

	// The content type of the request body
	private String contentType;

	// The request body
	private String body;

	// The username used for authentication
	private String username;

	// The password used for authentication
	private String password;

	/*********************************************************************************************
	 * CONSTRUCTORS AND PUBLIC INITIALISATION                                                    *
	 *********************************************************************************************/

	public HTTPRequest(){}

	public HTTPRequest(String url){
		method = Method.GET;
		this.url = url;
	}

	/*********************************************************************************************
	 * PUBLIC METHODS                                                                            *
	 *********************************************************************************************/

	public Method getMethod(){
		return method;
	}

	public void setMethod(Method method){
		this.method = method;
	}

	public String getURL(){
		return url;
	}

	public void setURL(String url){
		this.url = url;
	}

	public String getAccept(){
		return accept;
	}

	public void setAccept(String accept){
		this.accept = accept;
	}

	public String getContentType(){
		return contentType;
	}

	public void setContentType(String contentType){
		this.contentType = contentType;
	}

	public String getBody(){
		return body;
	}

	public void setBody(String body){
		this.body = body;
	}

	public String getUsername(){
		return username;
	}

	public void setUsername(String username){
		this.username = username;
	}

	public String getPassword(){
		return password;
	}

	public void setPassword(String password){
		this.password = password;
	}

	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();

		builder.append("HTTPRequest [ \n");
		builder.append("method = \"").append(method).append("\"");
		builder.append(", \n" );
		builder.append("url = \"").append(url).append("\"");
		builder.append(", \n" );
		builder.append("accept = \"").append(accept).append("\"");
		builder.append(", \n" );
		builder.append("contentType = \"").append(contentType).append("\"");
		builder.append(", \n" );
		builder.append("body = \"" ).append(body).append("\"");
		builder.append(", \n" );
		builder.append("username = \"").append(username).append("\"");
		builder.append(", \n" );
		builder.append("password = \"").append((password==null ? "null" : "*****")).append("\"");
		builder.append(" ]\n" );

		return builder.toString();
	}

	/*********************************************************************************************
	 * PRIVATE METHODS                                                                           *
	 *********************************************************************************************/

}
