package ring;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class Experiment {
    private static final List<String> NODES = List.of("A", "B", "C", "D");

    static Map<String,Integer> distribution(String[] owners) {
        Map<String,Integer> counts = new LinkedHashMap<>();
        NODES.forEach(node -> counts.put(node, 0));
        for (String owner : owners) counts.compute(owner, (k, v) -> v + 1);
        return counts;
    }

    static int sum(Map<String,Integer> counts) {
        return counts.values().stream().mapToInt(Integer::intValue).sum();
    }

    private static void checkFlows(Movement movement) {
        if (sum(movement.incoming()) != movement.moved() || sum(movement.outgoing()) != movement.moved()) {
            throw new IllegalStateException("flow totals disagree");
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 1) throw new IllegalArgumentException("usage: java -jar ring.jar [placement-file]");
        long[] keys = new long[100_000];
        for (int i = 0; i < keys.length; i++) keys[i] = Ring.hash("key-" + i);
        System.out.println("| 가상 노드/대 | 시드 | A | B | C | D | 최대/평균 | E 추가 이동 수 (%) | B 제거 이동 수 (%) |");
        System.out.println("|---:|---:|---:|---:|---:|---:|---:|---:|---:|");
        try (var placements = new BufferedOutputStream(args.length == 0
                ? OutputStream.nullOutputStream() : Files.newOutputStream(Path.of(args[0])))) {
            for (int count : new int[]{1, 16, 128}) {
                double skewSum = 0;
                int addedSum = 0, removedSum = 0;
                for (int seed = 1; seed <= 5; seed++) {
                    var ring = new Ring();
                    for (String node : NODES) ring.add(node, count, seed);
                    String[] before = Movement.owners(ring, keys);
                    var counts = distribution(before);
                    if (sum(counts) != keys.length) throw new IllegalStateException("missing keys");
                    double skew = counts.values().stream().mapToInt(Integer::intValue).max().orElseThrow()
                            / (keys.length / (double) NODES.size());
                    ring.add("E", count, seed);
                    String[] added = Movement.owners(ring, keys);
                    Movement add = Movement.compare(before, added, "E", true);
                    checkFlows(add);
                    ring.remove("E");
                    if (!Arrays.equals(before, Movement.owners(ring, keys))) throw new IllegalStateException("restore failed");
                    ring.remove("B");
                    String[] removed = Movement.owners(ring, keys);
                    Movement remove = Movement.compare(before, removed, "B", false);
                    checkFlows(remove);
                    for (int i = 0; i < keys.length; i++) {
                        placements.write(before[i].charAt(0));
                        placements.write(added[i].charAt(0));
                        placements.write(removed[i].charAt(0));
                    }
                    System.out.printf(Locale.ROOT, "| %d | %d | %d | %d | %d | %d | %.4f | %d (%.3f%%) | %d (%.3f%%) |%n",
                            count, seed, counts.get("A"), counts.get("B"), counts.get("C"), counts.get("D"),
                            skew, add.moved(), add.percent(), remove.moved(), remove.percent());
                    skewSum += skew;
                    addedSum += add.moved();
                    removedSum += remove.moved();
                }
                System.out.printf(Locale.ROOT, "| %d | 평균 | — | — | — | — | %.4f | %.1f (%.3f%%) | %.1f (%.3f%%) |%n",
                        count, skewSum / 5, addedSum / 5.0, addedSum / 5000.0, removedSum / 5.0, removedSum / 5000.0);
            }
        }
        printExamples(keys);
    }

    private static void printExamples(long[] keys) {
        var baseline = new Ring();
        var expanded = new Ring();
        for (String node : NODES) {
            baseline.add(node, 1, 1);
            expanded.add(node, 1, 1);
        }
        expanded.add("E", 1, 1);
        System.out.println("\n시드 1, 가상 노드 1개에서 E 추가 전후의 경로 (16진수 좌표)\n");
        for (boolean moved : new boolean[]{true, false}) {
            for (int i = 0; i < keys.length; i++) {
                var before = baseline.locate(keys[i]);
                var after = expanded.locate(keys[i]);
                if (moved == !before.owner().equals(after.owner())) {
                    System.out.printf("- key-%d (%016x) → %s (%016x) → %s (%016x)%n",
                            i, keys[i], before.owner(), before.token(), after.owner(), after.token());
                    break;
                }
            }
        }
    }
}
