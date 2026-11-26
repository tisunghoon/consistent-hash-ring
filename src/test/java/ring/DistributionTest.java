package ring;

import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DistributionTest {
    @Test void includesZeroLoadNodes() {
        assertEquals(Map.of("A", 1, "B", 0, "C", 0, "D", 2),
                Experiment.distribution(new String[]{"A", "D", "D"}));
    }
}
