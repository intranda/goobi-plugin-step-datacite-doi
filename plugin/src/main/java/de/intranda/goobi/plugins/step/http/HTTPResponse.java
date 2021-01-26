package de.intranda.goobi.plugins.step.http;

/**
 * Encapsulates a HTTP response
 */
public final class HTTPResponse {
    /*********************************************************************************************
     * VARIABLE DECLARATIONS *
     *********************************************************************************************/

    // The HTTP response code
    private int responseCode;

    // The content type of the response body
    private String contentType;

    // The response body
    private byte[] body = new byte[0];

    /*********************************************************************************************
     * CONSTRUCTORS AND PUBLIC INITIALISATION *
     *********************************************************************************************/

    public HTTPResponse() {
    }

    public HTTPResponse(int responseCode) {
        this.responseCode = responseCode;
    }

    /*********************************************************************************************
     * PUBLIC METHODS *
     *********************************************************************************************/

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("HTTPResponse [\n ");
        builder.append("responseCode = \"").append(responseCode).append("\"");
        builder.append(",\n ");
        builder.append("contentType = \"").append(contentType).append("\"");
        builder.append(",\n ");
        builder.append("body = \"");
        if (contentType != null && contentType.contains("application/pdf")) {
            builder.append("byteStream size: " + (body.length > 0 ? body.length / 1024 : "0") + "KB");
        } else {
            builder.append((body.length > 0 ? new String(body) : "null"));
        }
        builder.append("\"");
        builder.append(" ]\n");

        return builder.toString();
    }

    /*********************************************************************************************
     * PRIVATE METHODS *
     *********************************************************************************************/

    /**
     * Responce codes
     */
    public static final Integer OK = 200;
    public static final Integer CREATED = 201;
    public static final Integer NOCONTENT = 204;
    public static final Integer BADREQUEST = 400;
    public static final Integer UNAUTHORIZED = 401;
    public static final Integer FORBIDDEN = 403;
    public static final Integer NOTFOUND = 404;
    public static final Integer PRECONDITIONFAILED = 412;
    public static final Integer UNPROCESSABLEENTITY = 422;
    public static final Integer WRONGCONTENTTYPE = 415;
}
