package ring;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/** A local, single-threaded ring with unsigned 64-bit coordinates. */
public final class Ring {
    private final NavigableMap<Long, String> tokens = new TreeMap<>(Long::compareUnsigned);

    public Ring() {}

    Ring(Map<Long, String> initialTokens) {
        tokens.putAll(initialTokens);
    }

    public record Route(long token, String owner) {}

    public Route locate(long position) {
        if (tokens.isEmpty()) throw new IllegalStateException("empty ring");
        var entry = tokens.ceilingEntry(position);
        if (entry == null) entry = tokens.firstEntry();
        return new Route(entry.getKey(), entry.getValue());
    }

    public Route lookup(String key) {
        return locate(hash(key));
    }

    public static long hash(String value) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            return ByteBuffer.wrap(digest.digest(value.getBytes(StandardCharsets.UTF_8))).getLong();
        } catch (NoSuchAlgorithmException impossible) {
            throw new AssertionError(impossible);
        }
    }
}
