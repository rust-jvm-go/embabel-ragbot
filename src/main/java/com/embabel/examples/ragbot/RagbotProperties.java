package com.embabel.examples.ragbot;

import com.embabel.agent.rag.ingestion.ContentChunker;
import com.embabel.common.ai.model.LlmOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Holds externalized configuration for the Ragbot application.
 * <p>
 * Spring Boot binds values under {@code ragbot.*} into this immutable record.
 * Using a record here is a best practice for learning projects because it keeps
 * configuration strongly typed, explicit, and thread-safe by default.
 *
 * @param chatLlm model selection and generation parameters for chat responses
 * @param objective objective template name describing what the assistant should accomplish
 * @param voice persona configuration describing how the assistant should communicate
 * @param chunkerConfig chunking settings used during document ingestion for RAG
 * @param uiPort port used by the optional Javelit web chat UI
 * @param uiCssPath resource path to CSS used by the Javelit UI header file
 */
@ConfigurationProperties(prefix = "ragbot")
public record RagbotProperties(
        @NestedConfigurationProperty LlmOptions chatLlm,
        String objective,
        @NestedConfigurationProperty Voice voice,
        @NestedConfigurationProperty ContentChunker.Config chunkerConfig,
        @DefaultValue("8888") int uiPort,
        @DefaultValue("classpath:ui/chat.css") String uiCssPath
) {

    /**
     * Voice/persona settings that control response style.
     *
     * @param persona persona template name (without extension) resolved from
     *                {@code prompts/personas}
     * @param maxWords soft limit used in prompts to guide concise responses
     */
    public record Voice(
            String persona,
            int maxWords
    ) {
    }
}
