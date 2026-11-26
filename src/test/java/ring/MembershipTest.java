package ring;

import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MembershipTest {
    @Test void rejectsMembershipErrorsAtomically() {
        var ring = new Ring(Map.of(10L, "A"));
        assertThrows(IllegalArgumentException.class, () -> ring.add("A", 3, 1));
        assertThrows(IllegalArgumentException.class, () -> ring.addTokens("B", 5, 10));
        assertThrows(IllegalArgumentException.class, () -> ring.addTokens("B", 5, 5));
        assertThrows(IllegalArgumentException.class, () -> ring.remove("B"));
        assertEquals(new Ring.Route(10, "A"), ring.locate(5));
    }

    @Test void restoresPlacementAfterRemoval() {
        var ring = new Ring(Map.of(10L, "A"));
        ring.addTokens("B", 5, 15);
        assertEquals("B", ring.locate(12).owner());
        ring.remove("B");
        assertEquals("A", ring.locate(12).owner());
        ring.remove("A");
        assertThrows(IllegalStateException.class, () -> ring.locate(0));
    }
}
