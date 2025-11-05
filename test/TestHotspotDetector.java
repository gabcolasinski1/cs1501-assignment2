import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

/**
 * Public tests for DLBHotspotDetector.
 * 
 * Note:
 * - These are *basic checks*. The autograder will include hidden tests
 *   with additional cases, larger datasets, and edge conditions.
 */
public class TestHotspotDetector {

    @Test
    public void testBasicHotspotExtraction() {
        HotspotDetector det = new DLBHotspotDetector();

        // Insert a few leaked passwords
        det.addLeakedPassword("password123", 3, 6);
        det.addLeakedPassword("admin123", 3, 6);
        det.addLeakedPassword("letmein", 3, 6);

        // Candidate contains "123" and "pass"
        Set<Hotspot> results = det.hotspotsIn("mypass123");

        boolean found123 = false;
        boolean foundPass = false;

        for (Hotspot h : results) {
            if (h.ngram.equals("123")) {
                found123 = true;
                assertTrue("Should appear at end", h.candidateAtEnd);
            }
            if (h.ngram.equals("pass")) {
                foundPass = true;
                assertTrue("Should appear at middle", h.candidateMiddleCount > 0);
            }
        }

        assertTrue("Hotspot '123' must be found", found123);
        assertTrue("Hotspot 'pass' must be found", foundPass);
    }

    @Test
    public void testNoDuplicates() {
        HotspotDetector det = new DLBHotspotDetector();
        det.addLeakedPassword("123123", 3, 3);

        // Candidate has "123" twice
        Set<Hotspot> results = det.hotspotsIn("abc123xyz123");

        int count123 = 0;
        for (Hotspot h : results) {
            if (h.ngram.equals("123")) count123++;
        }

        assertEquals("Hotspot '123' should only appear once", 1, count123);
    }
}
