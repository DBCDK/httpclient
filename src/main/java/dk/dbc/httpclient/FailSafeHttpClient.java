package dk.dbc.httpclient;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Response;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

/**
 * Class for executing HTTP requests in a fail safe manner with automatic retry functionality
 *
 * <p>
 * Example:
 * <pre>
 * {@code
 *
 * final Client client = HttpClient.newClient();
 * final RetryPolicy<Response> retryPolicy = new RetryPolicy<Response>()
 *          .handle(ProcessingException.class)
 *          .handleResultIf(response -> response.getStatus() == 404 || response.getStatus() == 500)
 *          .withDelay(Duration.ofSeconds(1))
 *          .withMaxRetries(3);
 *
 * final FailSafeHttpClient failSafeHttpClient = FailSafeHttpClient.create(client, retryPolicy);
 * final HttpGet httpGet = failSafeHttpClient.createHttpGet()
 *          .withBaseUrl("http://localhost:8080")
 *          .withPathElements("path", "to", "resource");
 *
 * failSafeHttpClient.execute(httpGet);
 *
 * }
 * </pre>
 */
public class FailSafeHttpClient extends HttpClient {
    private final RetryPolicy<Response> retryPolicy;

    public static FailSafeHttpClient create(Client httpClient, RetryPolicy<Response> retryPolicy) throws NullPointerException {
        return new FailSafeHttpClient(httpClient, retryPolicy, true);
    }

    public static FailSafeHttpClient create(Client httpClient, RetryPolicy<Response> retryPolicy,
                                            boolean overrideOnRetry) throws NullPointerException {
        return new FailSafeHttpClient(httpClient, retryPolicy, overrideOnRetry);
    }

    private FailSafeHttpClient(Client client, RetryPolicy<Response> retryPolicy, boolean overrideOnRetry)
            throws NullPointerException {
        super(client);
        if (retryPolicy == null) {
            throw new NullPointerException("retryPolicy can not be null");
        }
        this.retryPolicy = retryPolicy;
        if (overrideOnRetry) {
            this.retryPolicy.onRetry(response -> {
                if (response != null && response.getLastResult() != null) response.getLastResult().close();
            });
        }
    }

    @Override
    public Response execute(HttpRequest<? extends HttpRequest> request) {
        return Failsafe.with(retryPolicy).get(() -> super.execute(request));
    }
}
