package de.intranda.goobi.plugins.step.datacite.mds.http;

import lombok.Data;

/**
 * Encapsulates a HTTP request
 */
@Data
public final class HTTPRequest {
	public enum Method {
		DELETE, GET, POST, PUT,
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

	public HTTPRequest() {
	}

	public HTTPRequest(String url) {
		method = Method.GET;
		this.url = url;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("HTTPRequest [ \n");
		builder.append("method = \"").append(method).append("\"");
		builder.append(", \n");
		builder.append("url = \"").append(url).append("\"");
		builder.append(", \n");
		builder.append("accept = \"").append(accept).append("\"");
		builder.append(", \n");
		builder.append("contentType = \"").append(contentType).append("\"");
		builder.append(", \n");
		builder.append("body = \"").append(body).append("\"");
		builder.append(", \n");
		builder.append("username = \"").append(username).append("\"");
		builder.append(", \n");
		builder.append("password = \"").append((password == null ? "null" : "*****")).append("\"");
		builder.append(" ]\n");

		return builder.toString();
	}

}
