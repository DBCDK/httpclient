package dk.dbc.httpclient;

import jakarta.ws.rs.core.Response;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * Base class for HTTP requests
 * @param <T> recursive request type parameter
 */
@SuppressWarnings("unchecked")
public abstract class HttpRequest<T extends HttpRequest<T>> implements Callable<Response> {
    protected final HttpClient httpClient;
    protected final Map<String, String> headers = new HashMap<>();
    protected final Map<String, Object> queryParameters = new HashMap<>();
    protected String baseUrl;
    protected String[] pathElements = new String[] {};

    public HttpRequest(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.headers.put("User-Agent", httpClient.getUserAgent().toString());
    }

    public Response execute() {
        return httpClient.execute(this);
    }

    public Response executeAndExpect(Response.Status expectedStatus) {
        return httpClient.executeAndExpect(this, expectedStatus);
    }

    public Response executeAndExpect() {
        return executeAndExpect(Response.Status.OK);
    }

    public <U> U executeAndExpect(Response.Status expectedStatus, Class<U> entityClass) {
        return httpClient.executeAndExpect(this, expectedStatus, entityClass);
    }

    public <U> U executeAndExpect(Class<U> entityClass) {
        return executeAndExpect(Response.Status.OK, entityClass);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public T withHeader(String name, String value) {
        headers.put(name, value);
        return (T) this;
    }

    public Map<String, Object> getQueryParameters() {
        return queryParameters;
    }

    public T withQueryParameter(String name, Object value) {
        queryParameters.put(name, value);
        return (T) this;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public T withBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return (T) this;
    }

    public T withCompression(Decompressor... decompressors) {
        Stream.of(decompressors).distinct().forEach(d -> headers.put("Accept-Encoding", d.toString()));
        return (T) this;
    }

    public String[] getPathElements() {
        return pathElements;
    }

    public T withPathElements(String... pathElements) {
        this.pathElements = pathElements;
        return (T) this;
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HttpRequest<?> that = (HttpRequest<?>) o;

        if (!headers.equals(that.headers)) {
            return false;
        }
        if (!queryParameters.equals(that.queryParameters)) {
            return false;
        }
        if (baseUrl != null ? !baseUrl.equals(that.baseUrl) : that.baseUrl != null) {
            return false;
        }
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(pathElements, that.pathElements);
    }

    @Override
    public int hashCode() {
        int result = headers.hashCode();
        result = 31 * result + queryParameters.hashCode();
        result = 31 * result + (baseUrl != null ? baseUrl.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(pathElements);
        return result;
    }
}
