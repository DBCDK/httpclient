/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt or at https://opensource.dbc.dk/licenses/gpl-3.0/
 */

package dk.dbc.httpclient;

import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class HttpClientTest {
    @Test
    public void test_setPathParametersOnWebTarget()
            throws URISyntaxException {
        Client client = HttpClient.newClient();
        HttpClient httpClient = new HttpClient(client);
        HttpGet httpGet = new HttpGet(httpClient).withPathElements(
            "square", "pants");
        WebTarget target = httpGet.getHttpClient().getClient()
            .target("sponge/bob");
        target = HttpClient.setPathParametersOnWebTarget(
            httpGet.getPathElements(), target);

        assertThat(target.getUri(), is(new URI("sponge/bob/square/pants")));
    }

    @Test
    public void test_setPathParametersOnWebTarget_noTrailingSlash()
            throws URISyntaxException {
        Client client = HttpClient.newClient();
        HttpClient httpClient = new HttpClient(client);
        HttpGet httpGet = new HttpGet(httpClient);
        WebTarget target = httpGet.getHttpClient().getClient()
            .target("sponge/bob");
        target = HttpClient.setPathParametersOnWebTarget(
            httpGet.getPathElements(), target);

        assertThat(target.getUri(), is(new URI("sponge/bob")));
    }
}
