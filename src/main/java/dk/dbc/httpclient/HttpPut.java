package dk.dbc.httpclient;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Arrays;

/**
 * HTTP PUT request representation
 *
 * <p>
 * Example:
 * <pre>
 * {@code
 * final HttpPut httpPut = new HttpPut(client)
 *          .withBaseUrl("http://localhost:8080")
 *          .withPathElements("path", "to", "resource")
 *          .withHeader("User-Agent", "AwesomeJavaClient")
 *          .withData(object, MediaType.APPLICATION_JSON);
 * }
 * </pre>
 */
public class HttpPut extends HttpRequest<HttpPut> {
    private Entity entity;

    public HttpPut(HttpClient httpClient) {
        super(httpClient);
    }

    public Entity getEntity() {
        return entity;
    }

    public <T> HttpPut withData(T data, String mediaType) {
        this.entity = Entity.entity(data, mediaType);
        return this;
    }

    public <T> HttpPut withJsonData(T data) {
        return withData(data, MediaType.APPLICATION_JSON);
    }

    @Override
    public Response call() {
        return HttpClient.doPut(this);
    }

    @Override
    public String toString() {
        return "HttpPut{" +
                "headers=" + headers +
                ", queryParameters=" + queryParameters +
                ", baseUrl='" + baseUrl + '\'' +
                ", pathElements=" + Arrays.toString(pathElements) +
                ", entity=" + entity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        HttpPut httpPut = (HttpPut) o;

        return entity != null ? entity.equals(httpPut.entity) : httpPut.entity == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (entity != null ? entity.hashCode() : 0);
        return result;
    }
}
