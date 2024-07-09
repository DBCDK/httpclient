package dk.dbc.httpclient;

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

    public static HttpClient create(Client client) throws NullPointerException {
        return new HttpClient(client);
    }

    /**
     * Executes given HTTP GET request
     * @param httpGet request
     * @return server response
     */
    public static Response doGet(HttpGet httpGet) {
        WebTarget target = httpGet.getHttpClient().getClient().target(httpGet.getBaseUrl());
        target = setPathParametersOnWebTarget(httpGet.getPathElements(), target);
        target = setQueryParametersOnWebTarget(httpGet.queryParameters, target);
        Invocation.Builder request = target.request();
        setHeadersOnRequest(httpGet.getHeaders(), request);
        return request.get();
    }

    /**
     * Executes given HTTP POST request
     * @param httpPost request
     * @return server response
     */
    public static Response doPost(HttpPost httpPost) {
        WebTarget target = httpPost.getHttpClient().getClient().target(httpPost.getBaseUrl());
        target = setPathParametersOnWebTarget(httpPost.getPathElements(), target);
        target = setQueryParametersOnWebTarget(httpPost.getQueryParameters(), target);
        Invocation.Builder request = target.request();
        setHeadersOnRequest(httpPost.getHeaders(), request);
        return request.post(httpPost.getEntity());
    }

    /**
     * Executes given HTTP PUT request
     * @param httpPut request
     * @return server response
     */
    public static Response doPut(HttpPut httpPut) {
        WebTarget target = httpPut.getHttpClient().getClient().target(httpPut.getBaseUrl());
        target = setPathParametersOnWebTarget(httpPut.getPathElements(), target);
        target = setQueryParametersOnWebTarget(httpPut.getQueryParameters(), target);
        Invocation.Builder request = target.request();
        setHeadersOnRequest(httpPut.getHeaders(), request);
        return request.put(httpPut.getEntity());
    }

    /**
     * Executes given HTTP DELETE request
     * @param httpDelete request
     * @return server response
     */
    public static Response doDelete(HttpDelete httpDelete) {
        WebTarget target = httpDelete.getHttpClient().client.target(httpDelete.getBaseUrl());
        target = setPathParametersOnWebTarget(httpDelete.getPathElements(), target);
        Invocation.Builder request = target.request();
        setHeadersOnRequest(httpDelete.getHeaders(), request);
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

    HttpClient(Client client) throws NullPointerException {
        if (client == null) {
            throw new NullPointerException("client can not be null");
        }
        this.client = client;
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
}
