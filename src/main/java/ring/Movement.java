package ring;

import java.util.HashMap;
import java.util.Map;

public record Movement(int total, int moved, Map<String,Integer> incoming, Map<String,Integer> outgoing) {
    public double percent() {
        return 100.0 * moved / total;
    }

    public static String[] owners(Ring ring, long[] keys) {
        String[] owners = new String[keys.length];
        for (int i = 0; i < keys.length; i++) owners[i] = ring.locate(keys[i]).owner();
        return owners;
    }

    /** An addition constrains the destination, a removal constrains the source. */
    public static Movement compare(String[] before, String[] after, String node, boolean addition) {
        if (before.length == 0 || before.length != after.length) {
            throw new IllegalArgumentException("expected aligned nonempty snapshots");
        }
        Map<String,Integer> in = new HashMap<>();
        Map<String,Integer> out = new HashMap<>();
        int moved = 0;
        for (int i = 0; i < before.length; i++) {
            if (before[i].equals(after[i])) continue;
            if (!node.equals(addition ? after[i] : before[i])) {
                throw new IllegalStateException("unexpected movement at index " + i);
            }
            in.merge(after[i], 1, Integer::sum);
            out.merge(before[i], 1, Integer::sum);
            moved++;
        }
        return new Movement(before.length, moved, Map.copyOf(in), Map.copyOf(out));
    }
}
