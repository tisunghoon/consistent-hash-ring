package ring;

import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MovementTest {
    @Test void countsBothDirectionsAndRejectsWrongOwners() {
        String[] original = {"A", "B", "C", "B"};
        String[] added = {"E", "B", "E", "B"};
        String[] removed = {"A", "A", "C", "C"};
        var add = Movement.compare(original, added, "E", true);
        assertEquals(50.0, add.percent());
        assertEquals(Map.of("E", 2), add.incoming());
        assertEquals(Map.of("A", 1, "C", 1), add.outgoing());
        var remove = Movement.compare(original, removed, "B", false);
        assertEquals(2, remove.moved());
        assertEquals(Map.of("A", 1, "C", 1), remove.incoming());
        assertEquals(Map.of("B", 2), remove.outgoing());
        assertThrows(IllegalStateException.class, () -> Movement.compare(original, added, "B", false));
        assertThrows(IllegalStateException.class, () -> Movement.compare(original, removed, "E", true));
    }
}
