package dk.dbc.httpclient;

import org.brotli.dec.BrotliInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

/**
 * Enum that link the Content-Encoding name to the corresponding stream deflater
 */
public enum Decompressor {
    GZIP(GZIPInputStream::new),
    BR(BrotliInputStream::new);

    private static final Map<String, Decompressor> MAP = Stream.of(values()).collect(Collectors.toMap(Enum::name, e -> e));
    public final SpicyIOUnaryOperator<InputStream> streamer;

    Decompressor(SpicyIOUnaryOperator<InputStream> streamer) {
        this.streamer = streamer;
    }

    public static Decompressor from(String name) {
        return MAP.get(name.toUpperCase());
    }

    public String toString() {
        return name().toLowerCase();
    }

    public interface SpicyIOUnaryOperator<T> {
        T apply(T t) throws IOException;
    }
}
