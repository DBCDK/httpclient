package dk.dbc.httpclient;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.ReaderInterceptorContext;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Interceptor that decompresses the response body according the Content-Encoding header if needed.
 * Encodings that are not supported will be left untouched.
 *
 * The auto decompression can be disabled by setting HTTP_AUTO_INFLATE_ENABLED=false
 *
 * @see Decompressor for the supported encodings
 */
public class DecompressionInterceptor implements ReaderInterceptor {

    @Override
    public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
        List<String> encodings = context.getHeaders().get("Content-Encoding");
        if(encodings == null || encodings.isEmpty()) return context.proceed();
        Decompressor decompressor = encodings.stream().filter(Objects::nonNull).map(Decompressor::from).findFirst().orElse(null);
        if(decompressor != null) context.setInputStream(decompressor.streamer.apply(context.getInputStream()));
        return context.proceed();
    }
}
