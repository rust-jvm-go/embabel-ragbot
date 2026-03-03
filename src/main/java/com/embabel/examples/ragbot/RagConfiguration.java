package com.embabel.examples.ragbot;

import com.embabel.agent.rag.ingestion.transform.AddTitlesChunkTransformer;
import com.embabel.agent.rag.lucene.LuceneSearchOperations;
import com.embabel.common.ai.model.DefaultModelSelectionCriteria;
import com.embabel.common.ai.model.ModelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;

/**
 * Configures retrieval infrastructure used by the chatbot.
 * <p>
 * This class wires a {@link LuceneSearchOperations} bean, which stores embeddings
 * and searchable chunks on local disk. The bean is shared by ingestion commands,
 * RAG tools, and UI status screens.
 * In this architecture, {@link LuceneSearchOperations} is the concrete
 * {@code SearchOperations} backend consumed by {@code ToolishRag}.
 */
@Configuration
@EnableConfigurationProperties(RagbotProperties.class)
class RagConfiguration {

    /**
     * Creates the retrieval configuration bean container.
     * <p>
     * Spring instantiates this configuration class during context startup.
     */
    RagConfiguration() {
    }

    /**
     * Logger used for lifecycle and indexing diagnostics.
     */
    private final Logger logger = LoggerFactory.getLogger(RagConfiguration.class);

    /**
     * Creates and initializes the Lucene-backed RAG store.
     * <p>
     * Best practices applied here:
     * <ul>
     *     <li>
     *         Choose embeddings through {@link ModelProvider} so model selection
     *         stays centralized.
     *     </li>
     *     <li>
     *         Use a configured chunking strategy from {@link RagbotProperties}
     *         for reproducible indexing.
     *     </li>
     *     <li>
     *         Add section titles to chunks to improve source attribution in
     *         retrieval-grounded responses.
     *     </li>
     *     <li>Persist the index to disk so data survives application restarts.</li>
     * </ul>
     *
     * @param modelProvider source for embedding service instances
     * @param properties application configuration, including chunking settings
     * @return Returns initialized Lucene search operations ready for ingestion and retrieval
     */
    @Bean
    LuceneSearchOperations luceneSearchOperations(
            ModelProvider modelProvider,
            RagbotProperties properties) {
        var embeddingService = modelProvider.getEmbeddingService(DefaultModelSelectionCriteria.INSTANCE);
        var luceneSearchOperations = LuceneSearchOperations
                .withName("docs")
                .withEmbeddingService(embeddingService)
                .withChunkerConfig(properties.chunkerConfig())
                // Add titles to chunks so we can distinguish sources during retrieval
                .withChunkTransformer(AddTitlesChunkTransformer.INSTANCE)
                .withIndexPath(Paths.get("./.lucene-index"))
                .buildAndLoadChunks();
        logger.info("Loaded {} chunks into Lucene RAG store", luceneSearchOperations.info().getChunkCount());
        return luceneSearchOperations;
    }
}
