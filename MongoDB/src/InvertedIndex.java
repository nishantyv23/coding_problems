import java.util.*;

public class InvertedIndex {

    // term -> documents containing the term
    private final Map<String, Set<String>> invertedIndex = new HashMap<>();

    // document -> unique words in the document
    private final Map<String, Set<String>> documentWords = new HashMap<>();

    /**
     * Inserts a document into the inverted index.
     */
    public void insert(String doc) {
        if (doc == null || doc.isBlank()) {
            return;
        }

        String[] tokens = doc.toLowerCase().split("\\W+");
        Set<String> uniqueWords = new HashSet<>();

        for (String word : tokens) {
            if (word.isEmpty()) {
                continue;
            }

            uniqueWords.add(word);
            invertedIndex
                    .computeIfAbsent(word, k -> new HashSet<>())
                    .add(doc);
        }

        documentWords.put(doc, uniqueWords);
    }

    /**
     * Returns all documents containing the given term.
     */
    public Set<String> search(String term) {
        if (term == null) {
            return Collections.emptySet();
        }

        return invertedIndex.getOrDefault(
                term.toLowerCase(),
                Collections.emptySet()
        );
    }

    /**
     * Deletes a document from the inverted index.
     */
    public void delete(String doc) {

        Set<String> words = documentWords.get(doc);

        if (words == null) {
            return; // document not found
        }

        for (String word : words) {
            Set<String> docs = invertedIndex.get(word);

            if (docs != null) {
                docs.remove(doc);

                // Remove the term if no documents contain it anymore
                if (docs.isEmpty()) {
                    invertedIndex.remove(word);
                }
            }
        }

        documentWords.remove(doc);
    }

    /**
     * Returns documents containing both terms.
     */
    public Set<String> andSearch(String term1, String term2) {

        Set<String> docs1 = invertedIndex.getOrDefault(
                term1.toLowerCase(),
                Collections.emptySet());

        Set<String> docs2 = invertedIndex.getOrDefault(
                term2.toLowerCase(),
                Collections.emptySet());

        docs1.retainAll(docs2);

        return docs1;
    }

    public static void main(String[] args) {

        InvertedIndex index = new InvertedIndex();

        index.insert("the quick brown fox");
        index.insert("the lazy dog");
        index.insert("quick dog jumps");
        index.insert("quick brown dog");

        System.out.println("Search 'quick':");
        System.out.println(index.search("quick"));

        System.out.println("\nSearch 'dog':");
        System.out.println(index.search("dog"));

        System.out.println("\nAND Search ('quick', 'dog'):");
        System.out.println(index.andSearch("quick", "dog"));

        index.delete("quick dog jumps");

        System.out.println("\nAfter deleting 'quick dog jumps':");

        System.out.println("\nSearch 'quick':");
        System.out.println(index.search("quick"));

        System.out.println("\nSearch 'dog':");
        System.out.println(index.search("dog"));

        System.out.println("\nAND Search ('quick', 'dog'):");
        System.out.println(index.andSearch("quick", "dog"));
    }
}