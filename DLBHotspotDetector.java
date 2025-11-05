import java.util.*;

public class DLBHotspotDetector implements HotspotDetector {
    
    // I am beginning by creating a DLB Node with child and sibling structure
    private static class DLBNode {
        char ch;
        DLBNode child;   // This is the first child (next character in path)
        DLBNode sibling; // This is the next sibling (different character at the exact same level)
        boolean isTerminal; // This marks end of a hotspot substring
        
        // These are the statistics for this hotspot (this is only meaningful if isTerminal is true)
        int freq;        // Total occurrences across all of the passwords
        int docFreq;     // Number of distinct passwords containing this
        int beginCount;  // The ocurrences at beginning
        int middleCount; // The occurrences in middle
        int endCount;    // The occurrences at end
        
        // Initializing everything
        DLBNode(char ch) {
            this.ch = ch;
            this.child = null;
            this.sibling = null;
            this.isTerminal = false;
            this.freq = 0;
            this.docFreq = 0;
            this.beginCount = 0;
            this.middleCount = 0;
            this.endCount = 0;
        }
    }
    
    private DLBNode root;
    
    public DLBHotspotDetector() {
        this.root = null;
    }
    
    @Override
    public void addLeakedPassword(String leakedPassword, int minN, int maxN) {
        if (leakedPassword == null)
            throw new IllegalArgumentException("null leakedPassword");
        if (minN < 1 || maxN < minN)
            throw new IllegalArgumentException("invalid n-range");
        
        // Track which substrings that have been found in THIS password for docFreq
        Set<String> seenInThisPassword = new HashSet<>();
        
        int len = leakedPassword.length();
        
        // Extract all of the n's for each length in [minN, maxN]
        for (int n = minN; n <= maxN; n++) {
            for (int i = 0; i <= len - n; i++) {
                String substring = leakedPassword.substring(i, i + n);
                
                // Determine position here as either beginning, middle, or end
                boolean atBegin = (i == 0);
                boolean atEnd = (i + n == len);
                boolean inMiddle = !atBegin && !atEnd;
                
                // Insert into the DLB and update stats
                boolean isFirstOccurrenceInPassword = !seenInThisPassword.contains(substring);
                insertAndUpdate(substring, atBegin, inMiddle, atEnd, isFirstOccurrenceInPassword);
                
                seenInThisPassword.add(substring);
            }
        }
    }
    
    /**
     * Insert a substring into the DLB tree and proceed onto update its statistics
     */
    private void insertAndUpdate(String substring, boolean atBegin, boolean inMiddle, 
                                 boolean atEnd, boolean isFirstInPassword) {
        if (substring.isEmpty()) return;
        
        // Navigate or create a path in DLB tree
        if (root == null) {
            root = new DLBNode(substring.charAt(0));
        }
        
        DLBNode current = root;
        int index = 0;
        
        while (index < substring.length()) {
            char ch = substring.charAt(index);
            
            // Find or create node for this character at current level
            if (current.ch == ch) {
                // A match is found 
                if (index == substring.length() - 1) {
                    // This is the end of substring so mark it as terminal and then continue on to update the statistics so far
                    current.isTerminal = true;
                    current.freq++;
                    if (isFirstInPassword) {
                        current.docFreq++;
                    }
                    if (atBegin) current.beginCount++;
                    if (inMiddle) current.middleCount++;
                    if (atEnd) current.endCount++;
                    return;
                } else {
                    // Move to the child for the next character
                    if (current.child == null) {
                        current.child = new DLBNode(substring.charAt(index + 1));
                    }
                    current = current.child;
                    index++;
                }
            } else {
                // There is no match so we will now check sibling
                if (current.sibling == null) {
                    // Creates a new sibling here
                    current.sibling = new DLBNode(ch);
                }
                current = current.sibling;
            }
        }
    }
    
    @Override
    public Set<Hotspot> hotspotsIn(String candidatePassword) {
        if (candidatePassword == null)
            throw new IllegalArgumentException("null candidatePassword");
        
        // Use Map in order to aggregate candidate password statistics for each hotspot substring
        Map<String, CandidateStats> hotspotMap = new LinkedHashMap<>();
        
        int len = candidatePassword.length();
        
        // Try beginning from each position in the candidate password
        for (int i = 0; i < len; i++) {
            // Go through the DLB starting from root, while matching characters starting at position i
            DLBNode current = root;
            int j = i;
            StringBuilder matched = new StringBuilder();
            
            while (j < len && current != null) {
                char ch = candidatePassword.charAt(j);
                
                // Find a matching child and/or sibling
                boolean found = false;
                while (current != null) {
                    if (current.ch == ch) {
                        matched.append(ch);
                        
                        // Check to see if end of a complete hotspot substring has been reached
                        if (current.isTerminal) {
                            String ngram = matched.toString();
                            
                            // Determine where in the password the hotspot occurrence is location (start, middle, end)
                            boolean atBegin = (i == 0);
                            boolean atEnd = (i + ngram.length() == len);
                            boolean inMiddle = !atBegin && !atEnd;
                            
                            // Aggregate statistics for where the hotspot appears in the password
                            CandidateStats stats = hotspotMap.get(ngram);
                            if (stats == null) {
                                stats = new CandidateStats(current);
                                hotspotMap.put(ngram, stats);
                            }
                            
                            if (atBegin) stats.candidateAtBegin = true;
                            if (inMiddle) stats.candidateMiddleCount++;
                            if (atEnd) stats.candidateAtEnd = true;
                        }
                        
                        // A matching character is found in the DLB Trie, so now move to the child for the next character
                        current = current.child;
                        found = true;
                        break;
                    } else {
                        // Try the sibling
                        current = current.sibling;
                    }
                }
                
                if (!found) break; // At this point, there is not a matching character for the next character in the password in question, so we cannot find any more hotspots starting from this position
                j++;
            }
        }
        
        // Here I am converting the map to Set of Hotspot objects
        Set<Hotspot> result = new LinkedHashSet<>();
        for (Map.Entry<String, CandidateStats> entry : hotspotMap.entrySet()) {
            String ngram = entry.getKey();
            CandidateStats stats = entry.getValue();
            
            Hotspot h = new Hotspot(
                ngram,
                stats.freq,
                stats.docFreq,
                stats.beginCount,
                stats.middleCount,
                stats.endCount,
                stats.candidateAtBegin,
                stats.candidateMiddleCount,
                stats.candidateAtEnd
            );
            result.add(h);
        }
        
        return result;
    }
    
    /**
     * This is the helper class which serves to aggregate candidate-specific statistics
     */
    private static class CandidateStats {
        // These are the global statistics
        int freq;
        int docFreq;
        int beginCount;
        int middleCount;
        int endCount;
        
        // These are the candidate-specific statistics (that are aggregated)
        boolean candidateAtBegin;
        int candidateMiddleCount;
        boolean candidateAtEnd;
        
        CandidateStats(DLBNode node) {
            this.freq = node.freq;
            this.docFreq = node.docFreq;
            this.beginCount = node.beginCount;
            this.middleCount = node.middleCount;
            this.endCount = node.endCount;
            this.candidateAtBegin = false;
            this.candidateMiddleCount = 0;
            this.candidateAtEnd = false;
        }
    }
}
