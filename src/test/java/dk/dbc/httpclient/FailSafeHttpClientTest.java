/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt or at https://opensource.dbc.dk/licenses/gpl-3.0/
 */

package dk.dbc.httpclient;

import net.jodah.failsafe.RetryPolicy;
import org.junit.Test;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.time.Duration;

import static dk.dbc.commons.testutil.Assert.assertThat;
import static dk.dbc.commons.testutil.Assert.isThrowing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FailSafeHttpClientTest {
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

        final FailSafeHttpClient failSafeHttpClient = FailSafeHttpClient.create(client, retryPolicy);
        final HttpGet httpGet = new HttpGet(failSafeHttpClient)
                .withBaseUrl(baseurl);

        assertThat(() -> failSafeHttpClient.execute(httpGet), isThrowing(ProcessingException.class));

        verify(client, times(numberOfRetries + 1)).target(baseurl);
    }
}
