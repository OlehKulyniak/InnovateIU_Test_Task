package cat.project;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc.
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */

public class DocumentManager {

    private final List<Document> documentList = new ArrayList<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if(document.getId() == null) {
            UUID generatedId = UUID.randomUUID();
            document.setId(generatedId.toString());
        }

        for(int i = 0; i < documentList.size(); i++) {
            if(documentList.get(i).getId().equals(document.getId())) {
                document.setCreated(documentList.get(i).getCreated());
                documentList.set(i, document);
                return document;
            }
        }
        documentList.add(document);
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        List<Document> foundDocuments = new ArrayList<>();
        for(var document : documentList) {
            if(isSearchedDocument(document, request)) {
                foundDocuments.add(document);
            }
        }
        return foundDocuments;
    }

    private boolean isSearchedDocument(Document document, SearchRequest request) {
        if(document == null || request == null) {
            return false;
        }

        boolean searchResult = true;
        if(request.getTitlePrefixes() != null) {
            searchResult = request.getTitlePrefixes().parallelStream()
                    .anyMatch(prefix -> document.getTitle().startsWith(prefix));

        }

        if(searchResult && request.getContainsContents() != null) {
            searchResult = request.getContainsContents().parallelStream()
                    .anyMatch(content -> document.getContent().contains(content));
        }

        if(searchResult && request.getAuthorIds() != null) {
            searchResult = request.getAuthorIds().parallelStream()
                    .anyMatch(authorId -> document.getAuthor().getId().equals(authorId));
        }

        if(searchResult && request.getCreatedFrom() != null) {
            searchResult = !document.getCreated().isBefore(request.getCreatedFrom());
        }

        if(searchResult && request.getCreatedTo() != null) {
            searchResult = !document.getCreated().isAfter(request.getCreatedTo());
        }

        return searchResult;
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        if(id != null) {
            for (var document : documentList) {
                if (document.getId().equals(id)) {
                    return Optional.of(document);
                }
            }
        }
        return Optional.empty();
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}
