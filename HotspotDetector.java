import java.util.Set;

/**
 * Interface for DLB-based hotspot detection.
 *
 * Responsibilities:
 * 1) Build an index from leaked passwords by harvesting n-grams in [minN, maxN].
 * 2) Given a candidate password, return a de-duplicated set of hotspots
 * (each hotspot substring appears at most once in the returned set).
 */
public interface HotspotDetector {

    /**
     * Adds a leaked password to the hotspot index by extracting all substrings
     * of lengths between minN and maxN (inclusive) and updating their statistics.
     *
     * @param leakedPassword the leaked password to index
     * @param minN           minimum n-gram length (inclusive), e.g., 3
     * @param maxN           maximum n-gram length (inclusive), e.g., 6
     * @throws IllegalArgumentException if minN < 1, maxN < minN, or leakedPassword
     *                                  is null
     */
    void addLeakedPassword(String leakedPassword, int minN, int maxN);

    /**
     * Finds hotspots that appear anywhere within the candidate password,
     * returning each hotspot substring at most once (no duplicates). if a hotspot
     * substring appears multiple times in the candidate, it should be represented
     * once in the returned set (with candidate position info aggregated inside the
     * Hotspot object).
     * 
     * @param candidatePassword the password to analyze
     * @return a de-duplicated set of Hotspot objects for this candidate
     * @throws IllegalArgumentException if candidatePassword is null
     */
    Set<Hotspot> hotspotsIn(String candidatePassword);
}
