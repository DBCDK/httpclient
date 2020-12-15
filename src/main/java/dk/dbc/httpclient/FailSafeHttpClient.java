/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt or at https://opensource.dbc.dk/licenses/gpl-3.0/
 */

package dk.dbc.httpclient;

import dk.dbc.invariant.InvariantUtil;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

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
        return new FailSafeHttpClient(httpClient, retryPolicy);
    }

    private FailSafeHttpClient(Client client, RetryPolicy<Response> retryPolicy) throws NullPointerException {
        super(client);
        this.retryPolicy = InvariantUtil.checkNotNullOrThrow(retryPolicy, "retryPolicy");
        this.retryPolicy.onRetry(response -> {
            if (response != null && response.getLastResult() != null) response.getLastResult().close();
        });
    }

    @Override
    public Response execute(HttpRequest<? extends HttpRequest> request) {
        return Failsafe.with(retryPolicy).get(() -> super.execute(request));
    }
}
