package eu.sdi4apps.indexer;

/**
 *
 * @author runarbe
 */
public enum IndexingStatus {

    Enqueued("Enqueued"),
    Indexing("Indexing"),
    Indexed("Indexed"),
    Error("Error");

    public final String Label;

    private IndexingStatus(String label) {

        this.Label = label;

    }

}
