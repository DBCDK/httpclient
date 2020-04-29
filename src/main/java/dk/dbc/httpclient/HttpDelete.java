/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt or at https://opensource.dbc.dk/licenses/gpl-3.0/
 */

package dk.dbc.httpclient;

import javax.ws.rs.core.Response;
import java.util.Arrays;

/**
 * HTTP DELETE request representation
 *
 * <p>
 * Example:
 * <pre>
 * {@code
 * final HttpDelete httpDelete = new HttpDelete(client)
 *          .withBaseUrl("http://localhost:8080")
 *          .withPathElements("path", "to", "resource");
 * }
 * </pre>
 */
public class HttpDelete extends HttpRequest<HttpDelete> {
    public HttpDelete(HttpClient httpClient) {
        super(httpClient);
    }

    @Override
    public Response call() {
        return HttpClient.doDelete(this);
    }

    @Override
    public String toString() {
        return "HttpDelete{" +
                "headers=" + headers +
                ", queryParameters=" + queryParameters +
                ", baseUrl='" + baseUrl + '\'' +
                ", pathElements=" + Arrays.toString(pathElements) +
                '}';
    }
}
