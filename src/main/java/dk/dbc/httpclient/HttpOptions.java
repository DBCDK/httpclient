package dk.dbc.httpclient;

import jakarta.ws.rs.core.Response;

import java.util.Arrays;

/**
 * HTTP OPTIONS request representation
 *
 * <p>
 * Example:
 * <pre>
 * {@code
 * final HttpOptions httpOptions = new HttpOptions(client)
 *          .withBaseUrl("http://localhost:8080")
 *          .withPathElements("path", "to", "resource")
 *          .withQueryParameter("key", "value")
 *          .withHeader("Accept", "text/html");
 * }
 * </pre>
 */
public class HttpOptions extends HttpRequest<HttpOptions> {
    public HttpOptions(HttpClient httpClient) {
        super(httpClient);
    }

    @Override
    public Response call() throws Exception {
        return HttpClient.doOptions(this);
    }

    @Override
    public String toString() {
        return "HttpOptions{" +
                "headers=" + headers +
                ", queryParameters=" + queryParameters +
                ", baseUrl='" + baseUrl + '\'' +
                ", pathElements=" + Arrays.toString(pathElements) +
                '}';
    }
}
