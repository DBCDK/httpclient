package dk.dbc.httpclient;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import dk.dbc.commons.useragent.UserAgent;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.status;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class HttpClientTest {
    WireMockServer wireMockServer = startWireMockServer();

    private static final UserAgent USER_AGENT = new UserAgent("HttpClientTest");

    @Test
    public void test_setPathParametersOnWebTarget()
            throws URISyntaxException {
        Client client = HttpClient.newClient();
        HttpClient httpClient = new HttpClient(client, USER_AGENT);
        HttpGet httpGet = new HttpGet(httpClient).withPathElements(
            "square", "pants");
        WebTarget target = httpGet.getHttpClient().getClient()
            .target("sponge/bob");
        target = HttpClient.setPathParametersOnWebTarget(
            httpGet.getPathElements(), target);

        assertThat(target.getUri(), is(new URI("sponge/bob/square/pants")));
    }

    @Test
    public void testUserAgentHeader() {
        wireMockServer.stubFor(get(urlMatching("/sponge/bob")).willReturn(status(200)));
        HttpClient client = HttpClient.create(USER_AGENT);
        new HttpGet(client)
                .withBaseUrl(wireMockServer.baseUrl())
                .withPathElements("sponge", "bob")
                .executeAndExpect(Response.Status.OK);

        wireMockServer.verify(getRequestedFor(urlMatching("/sponge/bob"))
                .withHeader("User-Agent", equalTo(USER_AGENT.toString())));
    }

    @Test
    public void test_setPathParametersOnWebTarget_noTrailingSlash()
            throws URISyntaxException {
        Client client = HttpClient.newClient();
        HttpClient httpClient = new HttpClient(client, USER_AGENT);
        HttpGet httpGet = new HttpGet(httpClient);
        WebTarget target = httpGet.getHttpClient().getClient()
            .target("sponge/bob");
        target = HttpClient.setPathParametersOnWebTarget(
            httpGet.getPathElements(), target);

        assertThat(target.getUri(), is(new URI("sponge/bob")));
    }

    @Test
    public void testBrotliCompression() {
        HttpClient client = HttpClient.create(USER_AGENT).enableCompression();
        String baseResponse = new HttpGet(client).withBaseUrl(wireMockServer.baseUrl()).withPathElements("no-compression").executeAndExpect(Response.Status.OK, String.class);
        String brResponse = new HttpGet(client).withBaseUrl(wireMockServer.baseUrl()).withPathElements("compression").withCompression(Decompressor.BR).executeAndExpect(Response.Status.OK, String.class);
        assertThat(baseResponse, is(brResponse));
    }

    @Test
    public void testGzipCompression() {
        HttpClient client = HttpClient.create(USER_AGENT).enableCompression();
        String baseResponse = new HttpGet(client).withBaseUrl(wireMockServer.baseUrl()).withPathElements("no-compression").executeAndExpect(Response.Status.OK, String.class);
        String gzipResponse = new HttpGet(client).withBaseUrl(wireMockServer.baseUrl()).withPathElements("compression").withCompression(Decompressor.GZIP).executeAndExpect(Response.Status.OK, String.class);
        assertThat(baseResponse, is(gzipResponse));
    }

    private static WireMockServer startWireMockServer() {
        WireMockServer server = new WireMockServer(new WireMockConfiguration().dynamicPort());
        server.start();
        configureFor("localhost", server.port());
        server.stubFor(get(urlMatching("/no-compression")).willReturn(status(200).withResponseBody(Body.ofBinaryOrText(readFile("brotli-pro.html"), new ContentTypeHeader("text/html")))));
        server.stubFor(get(urlMatching("/compression")).withHeader("accept-encoding", equalTo("br")).willReturn(
                status(200).withHeader("content-encoding", "br").withResponseBody(Body.ofBinaryOrText(readFile("brotli-pro.br"), new ContentTypeHeader("text/html")))));
        server.stubFor(get(urlMatching("/compression")).withHeader("accept-encoding", equalTo("gzip")).willReturn(
                status(200).withHeader("content-encoding", "gzip").withResponseBody(Body.ofBinaryOrText(readFile("brotli-pro.gzip"), new ContentTypeHeader("text/html")))));
        return server;
    }

    private static byte[] readFile(String fileName) {
            try {
                Path p = Path.of(HttpClientTest.class.getClassLoader().getResource(".").toURI()).resolve(fileName);
                return Files.readAllBytes(p);
            } catch (URISyntaxException | IOException e) {
                throw new RuntimeException(e);
            }
    }
}
