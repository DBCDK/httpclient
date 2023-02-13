package dk.dbc.httpclient;

import jakarta.ws.rs.core.Response;
import java.util.Arrays;

/**
 * HTTP GET request representation
 *
 * <p>
 * Example:
 * <pre>
 * {@code
 * final HttpGet httpGet = new HttpGet(client)
 *          .withBaseUrl("http://localhost:8080")
 *          .withPathElements("path", "to", "resource")
 *          .withQueryParameter("key", "value")
 *          .withHeader("Accept", "text/html");
 * }
 * </pre>
 */
public class HttpGet extends HttpRequest<HttpGet> {
    public HttpGet(HttpClient httpClient) {
        super(httpClient);
    }

    @Override
    public Response call() {
        return HttpClient.doGet(this);
    }

    @Override
    public String toString() {
        return "HttpGet{" +
                "headers=" + headers +
                ", queryParameters=" + queryParameters +
                ", baseUrl='" + baseUrl + '\'' +
                ", pathElements=" + Arrays.toString(pathElements) +
                '}';
    }
}
