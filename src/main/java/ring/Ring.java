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

    public void add(String node, int count, int seed) {
        if (node == null || node.isBlank() || count <= 0) {
            throw new IllegalArgumentException("expected node name and positive token count");
        }
        long[] positions = new long[count];
        for (int i = 0; i < count; i++) positions[i] = hash(seed + ":" + node + ":" + i);
        addTokens(node, positions);
    }

    void addTokens(String node, long... positions) {
        if (tokens.containsValue(node)) throw new IllegalArgumentException("duplicate node");
        var pending = new java.util.HashSet<Long>();
        for (long position : positions) {
            if (tokens.containsKey(position) || !pending.add(position)) {
                throw new IllegalArgumentException("token collision");
            }
        }
        for (long position : positions) tokens.put(position, node);
    }

    public void remove(String node) {
        if (!tokens.values().removeIf(node::equals)) throw new IllegalArgumentException("unknown node");
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
