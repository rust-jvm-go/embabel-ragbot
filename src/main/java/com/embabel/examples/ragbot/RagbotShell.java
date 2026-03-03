package com.embabel.examples.ragbot;

import com.embabel.agent.rag.ingestion.TikaHierarchicalContentReader;
import com.embabel.agent.rag.ingestion.policy.NeverRefreshExistingDocumentContentPolicy;
import com.embabel.agent.rag.lucene.LuceneSearchOperations;
import com.embabel.agent.rag.model.ContentElement;
import com.embabel.agent.rag.model.Section;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.nio.file.Path;

/**
 * Exposes Spring Shell commands for ingestion, inspection, and maintenance of the local RAG store.
 * <p>
 * This record is intentionally thin: each command delegates to
 * {@link LuceneSearchOperations} and related Embabel ingestion helpers.
 * Keeping shell commands simple is a good practice because it makes behavior easy
 * to test and easy to reuse from other interfaces.
 * In the learning flow described for this project, shell commands are the primary
 * way to ingest content first, then chat against the persisted index.
 *
 * @param luceneSearchOperations search/index component backed by Lucene and
 *                               shared by chat and ingestion
 */
@ShellComponent
record RagbotShell(LuceneSearchOperations luceneSearchOperations) {

    /**
     * Ingests a single URL or file into the RAG store.
     * <p>
     * If the input is a local file path, it is normalized to an absolute file URI
     * before ingestion. If the path is a directory, this command returns a friendly
     * message guiding the user to {@link #ingestDirectory(String)}.
     * Content extraction is delegated to {@link TikaHierarchicalContentReader},
     * which handles common formats such as HTML, PDF, and office documents.
     *
     * @param location url or local file path to ingest
     * @return Returns ingestion outcome including document id or skip reason
     */
    @ShellMethod("Ingest URL or file path: Ingests Schumann's music criticism by default")
    String ingest(@ShellOption(
            help = "URL or file path to ingest",
            defaultValue = "./data/schumann/musicandmusician001815mbp.md") String location) {
        // Check if it's a local path (not a URL) and if so, verify it's not a directory
        if (!location.startsWith("http://") && !location.startsWith("https://")) {
            var path = Path.of(location).toAbsolutePath();
            if (path.toFile().isDirectory()) {
                return "Error: '" + location + "' is a directory. Use 'ingest-directory' command for directories.";
            }
        }
        var uri = location.startsWith("http://") || location.startsWith("https://")
                ? location
                : Path.of(location).toAbsolutePath().toUri().toString();
        var ingested = NeverRefreshExistingDocumentContentPolicy.INSTANCE
                .ingestUriIfNeeded(
                        luceneSearchOperations,
                        new TikaHierarchicalContentReader(),
                        uri
                );
        return ingested != null ?
                "Ingested document with ID: " + ingested.getId() :
                "Document already exists, no ingestion performed.";
    }

    /**
     * Ingests all files in a local directory into the RAG store.
     * <p>
     * Best practice for learning projects: keep validation close to user input
     * (existence checks, file-vs-directory checks) so failure modes are clear and
     * easy to debug from the command line.
     * This command is useful for bulk indexing before interactive chat sessions,
     * where retrieval should remain grounded in project-specific source content.
     *
     * @param directoryPath local directory containing files to ingest
     * @return Returns summary of how many files produced newly ingested documents
     */
    @ShellMethod("Ingest a directory of files")
    String ingestDirectory(@ShellOption(
            help = "Directory path to ingest",
            defaultValue = "./data") String directoryPath) {
        var dirFile = Path.of(directoryPath);
        var dir = dirFile.toAbsolutePath().toFile();

        // Check if it's a file rather than a directory
        if (dir.isFile()) {
            return "Error: '" + directoryPath + "' is a file. Use 'ingest' command for individual files.";
        }
        if (!dir.exists()) {
            return "Error: '" + directoryPath + "' does not exist.";
        }

        var dirUri = dirFile.toAbsolutePath().toUri().toString();
        var ingestedCount = 0;

        try {
            System.out.println("Ingesting files from directory: " + dir.getAbsolutePath());
            if (dir.isDirectory()) {
                var files = dir.listFiles();
                if (files != null) {
                    for (var file : files) {
                        if (file.isFile()) {
                            var fileUri = file.toPath().toAbsolutePath().toUri().toString();
                            var ingested = NeverRefreshExistingDocumentContentPolicy.INSTANCE
                                    .ingestUriIfNeeded(
                                            luceneSearchOperations,
                                            new TikaHierarchicalContentReader(),
                                            fileUri
                                    );
                            if (ingested != null) {
                                ingestedCount++;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            return "Error during ingestion: " + e.getMessage();
        }

        return "Ingested " + ingestedCount + " documents from directory: " + dirUri;
    }

    /**
     * Deletes all indexed documents and chunks from the Lucene store.
     *
     * @return Returns confirmation message with number of deleted documents
     */
    @ShellMethod("clear all documents")
    String zap() {
        var count = luceneSearchOperations.clear();
        return "All %d documents deleted".formatted(count);
    }

    /**
     * Prints all chunks and metadata currently stored in the index.
     *
     * @return Returns total chunk count after listing details to the console
     */
    @ShellMethod("show chunks")
    String chunks() {
        var chunks = luceneSearchOperations.findAll();
        for (var chunk : chunks) {
            System.out.println("Chunk ID: " + chunk.getId());
            System.out.println("Content: " + chunk.getText());
            System.out.println("Metadata: " + chunk.getMetadata());
            System.out.println("-----");
        }
        return "\n\nTotal chunks: " + chunks.size();
    }

    /**
     * Prints all indexed {@link Section} records.
     *
     * @return Returns total section count after listing section ids and titles
     */
    @ShellMethod("show sections")
    String sections() {
        var sections = luceneSearchOperations.findAll(Section.class);
        for (var section : sections) {
            System.out.println("Section ID: " + section.getId());
            System.out.println("Content: " + section.getTitle());
            System.out.println("-----");
        }
        return "\n\nTotal sections: " + sections.size();
    }

    /**
     * Prints all indexed {@link ContentElement} instances.
     *
     * @return Returns total content element count after listing identifiers and types
     */
    @ShellMethod("show content elements")
    String contentElements() {
        var contentElements = luceneSearchOperations.findAll(ContentElement.class);
        for (var contentElement : contentElements) {
            System.out.println("Section ID: " + contentElement.getId());
            System.out.println(contentElement.getClass().getSimpleName());
            System.out.println("-----");
        }
        return "\n\nTotal content elements: " + contentElements.size();
    }

    /**
     * Shows high-level diagnostics for the Lucene store.
     *
     * @return Returns textual stats including document and chunk counts
     */
    @ShellMethod("show lucene info: number of documents etc.")
    String info() {
        var info = luceneSearchOperations.info();
        return "Stats: " + info;
    }
}
