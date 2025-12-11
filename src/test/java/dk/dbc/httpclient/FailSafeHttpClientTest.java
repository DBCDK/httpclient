package dk.dbc.httpclient;

import dk.dbc.commons.useragent.UserAgent;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Response;
import net.jodah.failsafe.RetryPolicy;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FailSafeHttpClientTest {

    private static final UserAgent USER_AGENT = new UserAgent("FailSafeHttpClientTest");

    @Test
    public void retriesIfPolicyDictatesIt() {
        final int numberOfRetries = 3;
        final String baseurl = "http://no.such.host";
        final Client client = mock(Client.class);
        when(client.target(baseurl)).thenThrow(new ProcessingException("err"));

        final RetryPolicy<Response> retryPolicy = new RetryPolicy<Response>()
                .handle(ProcessingException.class)
                .handleResultIf(response -> response.getStatus() == 404 || response.getStatus() == 500)
                .withDelay(Duration.ofMillis(1))
                .withMaxRetries(numberOfRetries);

        final FailSafeHttpClient failSafeHttpClient = FailSafeHttpClient.create(client, USER_AGENT, retryPolicy);
        final HttpGet httpGet = new HttpGet(failSafeHttpClient)
                .withBaseUrl(baseurl);

        assertThrows(ProcessingException.class, () -> failSafeHttpClient.execute(httpGet));

        verify(client, times(numberOfRetries + 1)).target(baseurl);
    }
}
