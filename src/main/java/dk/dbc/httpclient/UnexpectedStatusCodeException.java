package dk.dbc.httpclient;

import jakarta.ws.rs.core.Response;

/**
 * Exception thrown when an unexpected status code is returned from a server.
 * The response is included in the exception and must be closed by the caller to avoid resource leakage.
 */
public class UnexpectedStatusCodeException extends RuntimeException {
    private final Response.Status statusCode;
    private final transient Response response;

    public UnexpectedStatusCodeException(Response.Status statusCode, Response response) {
        super("Unexpected status code: " + statusCode);
        this.statusCode = statusCode;
        this.response = response;
    }

    public Response.Status getStatusCode() {
        return statusCode;
    }

    public Response getResponse() {
        return response;
    }
    
    public void close() {
        if (response != null) {
            response.close();
        }
    }
}
