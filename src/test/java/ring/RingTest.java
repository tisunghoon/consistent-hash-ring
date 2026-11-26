package ring;

import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RingTest {
    @Test void exactBetweenAndWrap() {
        var ring = new Ring(Map.of(10L, "A", 40L, "B", 80L, "C"));
        assertEquals("B", ring.locate(40).owner());
        assertEquals("C", ring.locate(41).owner());
        assertEquals("A", ring.locate(81).owner());
        assertThrows(IllegalStateException.class, () -> new Ring().lookup("key"));
    }
    @Test void hashesAndRoutesInUnsignedOrder() {
        assertEquals("ba7816bf8f01cfea", Long.toHexString(Ring.hash("abc")));
        var ring = new Ring(Map.of(0L, "A", Long.MIN_VALUE, "B"));
        assertEquals("B", ring.locate(1).owner());
        assertEquals("A", ring.locate(-1).owner());
        assertEquals("A", ring.lookup("abc").owner());
    }
}
