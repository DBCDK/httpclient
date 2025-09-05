package dk.dbc.httpclient;

import jakarta.ws.rs.core.Response;

import java.util.Arrays;

/**
 * HTTP HEAD request representation
 *
 * <p>
 * Example:
 * <pre>
 * {@code
 * final HttpHead httpHead = new HttpHead(client)
 *          .withBaseUrl("http://localhost:8080")
 *          .withPathElements("path", "to", "resource")
 *          .withQueryParameter("key", "value")
 *          .withHeader("Accept", "text/html");
 * }
 * </pre>
 */
public class HttpHead extends HttpRequest<HttpHead> {
    public HttpHead(HttpClient httpClient) {
        super(httpClient);
    }

    @Override
    public Response call() throws Exception {
        return HttpClient.doHead(this);
    }

    @Override
    public String toString() {
        return "HttpHead{" +
                "headers=" + headers +
                ", queryParameters=" + queryParameters +
                ", baseUrl='" + baseUrl + '\'' +
                ", pathElements=" + Arrays.toString(pathElements) +
                '}';
    }
}
