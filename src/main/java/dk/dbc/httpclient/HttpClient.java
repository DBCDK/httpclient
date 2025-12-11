package dk.dbc.httpclient;

import dk.dbc.commons.useragent.UserAgent;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;

import java.util.Map;

/**
 * This utility class provides convenience methods for accessing web resources via HTTP
 */
public class HttpClient {
    protected final Client client;

    private final UserAgent userAgent;

    /**
     * Creates new HTTP client with default configuration
     * @param userAgent user agent to be used in requests
     * @return new HTTP client
     */
    public static HttpClient create(UserAgent userAgent) {
        return create(newClient(), userAgent);
    }

    /**
     * Creates new HTTP client with given configuration
     * @param client web resources client
     * @param userAgent user agent to be used in requests
     * @return new HTTP client
     */
    public static HttpClient create(Client client, UserAgent userAgent) throws NullPointerException {
        return new HttpClient(client, userAgent);
    }

    /**
     * Executes given HTTP GET request
     * @param httpGet request
     * @return server response
     */
    public static Response doGet(HttpGet httpGet) {
        Invocation.Builder request = configureRequest(httpGet);
        return request.get();
    }

    /**
     * Executes given HTTP HEAD request
     * @param httpHead request
     * @return server response
     */
    public static Response doHead(HttpHead httpHead) {
        Invocation.Builder request = configureRequest(httpHead);
        return request.head();
    }

    /**
     * Executes given HTTP OPTIONS request
     * @param httpHead request
     * @return server response
     */
    public static Response doOptions(HttpOptions httpHead) {
        Invocation.Builder request = configureRequest(httpHead);
        return request.options();
    }

    /**
     * Executes given HTTP POST request
     * @param httpPost request
     * @return server response
     */
    public static Response doPost(HttpPost httpPost) {
        Invocation.Builder request = configureRequest(httpPost);
        return request.post(httpPost.getEntity());
    }

    /**
     * Executes given HTTP PUT request
     * @param httpPut request
     * @return server response
     */
    public static Response doPut(HttpPut httpPut) {
        Invocation.Builder request = configureRequest(httpPut);
        return request.put(httpPut.getEntity());
    }

    /**
     * Executes given HTTP DELETE request
     * @param httpDelete request
     * @return server response
     */
    public static Response doDelete(HttpDelete httpDelete) {
        Invocation.Builder request = configureRequest(httpDelete);
        return request.delete();
    }

    /**
     * @return new web resources client
     */
    public static Client newClient() {
       return ClientBuilder.newClient();
    }

    /**
     * @param config the client config
     * @return new web resources client with given configuration
     */
    public static Client newClient(ClientConfig config) {
       return ClientBuilder.newClient(config);
    }

    /**
     * Closes given client instance thereby releasing all resources held
     * @param client web resource client (can be null)
     */
    public static void closeClient(Client client) {
        if (client != null) {
            client.close();
        }
    }

    HttpClient(Client client, UserAgent userAgent) throws NullPointerException {
        if (client == null) {
            throw new NullPointerException("client can not be null");
        }
        this.client = client;
        if (userAgent == null) {
            throw new NullPointerException("userAgent can not be null");
        }
        this.userAgent = userAgent;
    }

    public UserAgent getUserAgent() {
        return userAgent;
    }

    public HttpClient enableCompression() {
        client.register(DecompressionInterceptor.class);
        return this;
    }

    /**
     * Executes given HTTP request
     * @param request request
     * @return server response
     */
    public Response execute(HttpRequest<? extends HttpRequest<?>> request) {
        try {
            return request.call();
        } catch (Exception e) {
            if (!(e instanceof ProcessingException)) {
                throw new ProcessingException(e);
            } else {
                throw (ProcessingException) e;
            }
        }
    }

    /**
     * Executes given HTTP request and expects a specific status code
     * @param request request
     * @param expectedStatus expected status code
     * @return server response
     * @throws UnexpectedStatusCodeException if the actual status code differs from the expected one, be advised
     * that the attached response must be closed to avoid resource leaks
     */
    public Response executeAndExpect(HttpRequest<? extends HttpRequest<?>> request, Response.Status expectedStatus)
            throws UnexpectedStatusCodeException {
        Response response = execute(request);
        if (response.getStatus() != expectedStatus.getStatusCode()) {
            throw new UnexpectedStatusCodeException(Response.Status.fromStatusCode(response.getStatus()), response);
        }
        return response;
    }

    /**
     * Executes given HTTP request and expects a 200 OK status code
     * @param request request
     * @return server response
     * @throws UnexpectedStatusCodeException if the actual status code differs from 200, be advised that the attached
     * response must be closed to avoid resource leaks
     */
    public Response executeAndExpect(HttpRequest<? extends HttpRequest<?>> request) {
        return executeAndExpect(request, Response.Status.OK);
    }

    /**
     * Executes given HTTP request and expects a specific status code and to be able to read an entity of a specific type
     * @param request request
     * @param expectedStatus expected status code
     * @param entityClass entity class
     * @param <T> entity type
     * @return entity
     * @throws UnexpectedStatusCodeException if the actual status code differs from the expected one, be advised
     * that the attached response must be closed to avoid resource leaks
     */
    public <T> T executeAndExpect(HttpRequest<? extends HttpRequest<?>> request, Response.Status expectedStatus, Class<T> entityClass)
            throws UnexpectedStatusCodeException {
        final Response response = executeAndExpect(request, expectedStatus);
        try {
            return response.readEntity(entityClass);
        } finally {
            response.close();
        }
    }

    /**
     * Executes given HTTP request and expects a 200 OK status code and to be able to read an entity of a specific type
     * @param request request
     * @param entityClass entity class
     * @param <T> entity type
     * @return entity
     * @throws UnexpectedStatusCodeException if the actual status code differs from 200, be advised that the attached
     * response must be closed to avoid resource leaks
     */
    public <T> T executeAndExpect(HttpRequest<? extends HttpRequest<?>> request, Class<T> entityClass) {
        return executeAndExpect(request, Response.Status.OK, entityClass);
    }

    public Client getClient() {
        return client;
    }

    private static void setHeadersOnRequest(Map<String, String> headers, Invocation.Builder request) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            request.header(entry.getKey(), entry.getValue());
        }
    }

    static WebTarget setPathParametersOnWebTarget(String[] pathElements, WebTarget target) {
        for (String pathElement : pathElements) {
            target = target.path(pathElement);
        }
        return target;
    }

    private static WebTarget setQueryParametersOnWebTarget(Map<String, Object> queryParameters, WebTarget target) {
        for (Map.Entry<String, Object> queryParameter : queryParameters.entrySet()) {
            target = target.queryParam(queryParameter.getKey(), queryParameter.getValue());
        }
        return target;
    }

    private static Invocation.Builder configureRequest(HttpRequest<?> httpRequest) {
        WebTarget target = httpRequest.getHttpClient().getClient().target(httpRequest.getBaseUrl());
        target = setPathParametersOnWebTarget(httpRequest.getPathElements(), target);
        target = setQueryParametersOnWebTarget(httpRequest.queryParameters, target);
        Invocation.Builder request = target.request();
        setHeadersOnRequest(httpRequest.getHeaders(), request);
        return request;
    }
}
