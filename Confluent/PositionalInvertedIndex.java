package Confluent;

import java.util.*;

public class PositionalInvertedIndex {

    private final Map<String, Map<Integer, List<Integer>>> index = new HashMap<>();

    // Build index with word positions
    public void buildIndex(List<String> docs) {
        for (int docId = 0; docId < docs.size(); docId++) {
            String[] tokens = docs.get(docId).toLowerCase().split("\\W+");

            for (int pos = 0; pos < tokens.length; pos++) {
                String token = tokens[pos];
                if (token.isBlank()) continue;

                index
                    .computeIfAbsent(token, k -> new HashMap<>())
                    .computeIfAbsent(docId, k -> new ArrayList<>())
                    .add(pos);
            }
        }
    }

    // Single word lookup
    public Set<Integer> searchWord(String word) {
        if (!index.containsKey(word.toLowerCase()))
            return Collections.emptySet();
        return index.get(word.toLowerCase()).keySet();
    }

    // Phrase lookup using positional matching
    public Set<Integer> searchPhrase(String phrase) {
        String[] words = phrase.toLowerCase().split("\\W+");
        if (words.length == 0) return Collections.emptySet();
        if (words.length == 1) return searchWord(words[0]);

        Map<Integer, List<Integer>> firstWordPositions = index.get(words[0]);
        if (firstWordPositions == null) return Collections.emptySet();

        Set<Integer> possibleDocs = new HashSet<>(firstWordPositions.keySet());

        // Intersect with docs containing all words
        for (int i = 1; i < words.length; i++) {
            Map<Integer, List<Integer>> nextPositions = index.get(words[i]);
            if (nextPositions == null) return Collections.emptySet();
            possibleDocs.retainAll(nextPositions.keySet());
            if (possibleDocs.isEmpty()) return Collections.emptySet();
        }

        // Verify adjacency
        Set<Integer> validDocs = new HashSet<>();
        for (int docId : possibleDocs) {
            boolean match = true;

            // For each word, compare positions
            List<Integer> prevPositions = index.get(words[0]).get(docId);
            for (int w = 1; w < words.length; w++) {
                List<Integer> currPositions = index.get(words[w]).get(docId);
                boolean adjacentFound = false;

                for (int pos : prevPositions) {
                    if (currPositions.contains(pos + 1)) {
                        adjacentFound = true;
                        break;
                    }
                }

                if (!adjacentFound) {
                    match = false;
                    break;
                }

                prevPositions = currPositions;
            }

            if (match) validDocs.add(docId);
        }

        return validDocs;
    }

    public static void main(String[] args) {
        List<String> docs = Arrays.asList(
            "The quick brown fox jumps over the lazy dog.",
            "The fox is quick and smart.",
            "Dogs are loyal animals.",
            "The lazy dog was sleeping."
        );

        PositionalInvertedIndex index = new PositionalInvertedIndex();
        index.buildIndex(docs);

        System.out.println("Word 'fox' in docs: " + index.searchWord("fox"));
        System.out.println("Phrase 'lazy dog' in docs: " + index.searchPhrase("lazy dog"));
        System.out.println("Phrase 'quick brown fox' in docs: " + index.searchPhrase("quick brown fox"));
        System.out.println("Phrase 'dog lazy' in docs: " + index.searchPhrase("dog lazy"));
    }
}