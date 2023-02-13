package dk.dbc.httpclient;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * This utility class provides convenience methods for accessing web resources via HTTP
 */
public class HttpClient {
    protected final Client client;

    public static HttpClient create(Client client) throws NullPointerException {
        return new HttpClient(client);
    }

    public static InetAddress getRemoteHostAddress(String url) {
        try {
            return InetAddress.getByName(new URL(url).getHost());
            // DO NOT merge these into a single multi catch as it causes
            // java.lang.VerifyError: Stack map does not match the one at exception handler
            // when running the tests in the gui module
        } catch (MalformedURLException e) {
            // unable to get remote host address;
            return null;
        } catch (UnknownHostException e) {
            // unable to get remote host address;
            return null;
        } catch (RuntimeException e) {
            // unable to get remote host address;
            return null;
        }
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
     *
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

    public Response execute(HttpRequest<? extends HttpRequest> request) {
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

    // Old util methods - will be deprecated in the near future

    private final static Map<String, String> NO_HEADERS = null;

    private static boolean headerExists(Map<String, String> headers) {
        return headers != NO_HEADERS;
    }

    /**
     * Issues HTTP GET request to endpoint constructed using given baseurl and path elements
     *
     * @param client web resource client
     * @param queryParameters query parameters to be added to request
     * @param baseUrl base URL on the form http(s)://host:port/path
     * @param pathElements additional path elements to be added to base URL
     *
     * @return server response
     */
    public static Response doGet(Client client, Map<String, Object> queryParameters, String baseUrl, String... pathElements)  {

        WebTarget target = client.target(baseUrl);

        target = setPathParametersOnWebTarget(pathElements, target);

        target = setQueryParametersOnWebTarget(queryParameters, target);

        return target.request().get();
    }

    /**
     * Issues HTTP GET request to endpoint constructed using given baseurl and path elements
     *
     * @param client web resource client
     * @param baseUrl base URL on the form http(s)://host:port/path
     * @param pathElements additional path elements to be added to base URL
     *
     * @return server response
     */
    public static Response doGet(Client client, String baseUrl, String... pathElements)  {
        return doGet(client, new HashMap<String, Object>(), baseUrl, pathElements);
    }

    /**
     * HTTP POSTs given data entity to endpoint constructed using given queryParameters, headers, baseurl and path elements
     *
     * @param client web resource client
     * @param queryParameters the query parameters
     * @param headers HTTP headers
     * @param data data entity
     * @param baseUrl base URL on the form http(s)://host:port/path
     * @param pathElements additional path elements to be added to base URL
     *
     * @return server response
     */
    public static Response doPost(Client client, Map<String, Object> queryParameters, Map<String, String> headers, Entity data, String baseUrl, String... pathElements) {

        WebTarget target = client.target(baseUrl);

        target = setPathParametersOnWebTarget(pathElements, target);

        target = setQueryParametersOnWebTarget(queryParameters, target);

        Invocation.Builder request = target.request();

        if (headerExists(headers)) {
            setHeadersOnRequest(headers, request);
        }

        return request.post(data);
    }

    /**
     * HTTP POSTs given data entity to endpoint constructed using given headers, baseurl and path elements
     *
     * @param client web resource client
     * @param headers HTTP headers
     * @param data data entity
     * @param baseUrl base URL on the form http(s)://host:port/path
     * @param pathElements additional path elements to be added to base URL
     *
     * @return server response
     */
    public static Response doPost(Client client, Map<String, String> headers, Entity data, String baseUrl, String... pathElements) {

        WebTarget target = client.target(baseUrl);

        target = setPathParametersOnWebTarget(pathElements, target);

        Invocation.Builder request = target.request();

        if (headerExists(headers)) {
            setHeadersOnRequest(headers, request);
        }

        return request.post(data);
    }

    /**
     * HTTP POSTs given data as application/json to endpoint constructed using given headers, baseurl and path elements
     *
     * @param client web resource client
     * @param headers HTTP headers
     * @param data JSON data
     * @param baseUrl base URL on the form http(s)://host:port/path
     * @param pathElements additional path elements to be added to base URL
     *
     * @return server response
     */
    public static Response doPostWithJson(Client client, Map<String, String> headers, String data, String baseUrl, String... pathElements) {
        return doPost(client, headers, Entity.entity(data, MediaType.APPLICATION_JSON), baseUrl, pathElements);
    }
    public static <T> Response doPostWithJson(Client client, Map<String, String> headers, T data, String baseUrl, String... pathElements) {
        return doPost(client, headers, Entity.entity(data, MediaType.APPLICATION_JSON), baseUrl, pathElements);
    }
    public static Response doPostWithJson(Client client, String data, String baseUrl, String... pathElements) {
        return doPost(client, NO_HEADERS, Entity.entity(data, MediaType.APPLICATION_JSON), baseUrl, pathElements);
    }
    public static <T> Response doPostWithJson(Client client, T data, String baseUrl, String... pathElements) {
        return doPost(client, NO_HEADERS,  Entity.entity(data, MediaType.APPLICATION_JSON), baseUrl, pathElements);
    }
    public static <T> Response doPostWithJson(Client client, Map<String, Object> queryParameters, Map<String, String> headers, T data, String baseUrl, String... pathElements) {
        return doPost(client, queryParameters, headers, Entity.entity(data, MediaType.APPLICATION_JSON), baseUrl, pathElements);
    }

    /**
     * Issues HTTP DELETE request to endpoint constructed using given baseurl and path elements
     *
     * @param client web resource client
     * @param baseUrl base URL on the form http(s)://host:port/path
     * @param pathElements additional path elements to be added to base URL
     *
     * @return server response
     */
    public static Response doDelete(Client client, String baseUrl, String... pathElements) {
        return doDelete(client, NO_HEADERS, baseUrl, pathElements);
    }

    /**
     * Issues HTTP DELETE request to endpoint constructed using given baseurl and path elements
     *
     * @param client web resource client
     * @param headers HTTP headers
     * @param baseUrl base URL on the form http(s)://host:port/path
     * @param pathElements additional path elements to be added to base URL
     *
     * @return server response
     */
    public static Response doDelete(Client client, Map<String, String> headers, String baseUrl, String... pathElements) {

        WebTarget target = client.target(baseUrl);

        target = setPathParametersOnWebTarget(pathElements, target);

        Invocation.Builder request = target.request();

        if (headerExists(headers)) {
            setHeadersOnRequest(headers, request);
        }

        return request.delete();
    }
}
