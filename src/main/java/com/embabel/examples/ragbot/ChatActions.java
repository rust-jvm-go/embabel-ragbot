package com.embabel.examples.ragbot;

import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.EmbabelComponent;
import com.embabel.agent.api.common.ActionContext;
import com.embabel.agent.rag.service.SearchOperations;
import com.embabel.agent.rag.tools.ToolishRag;
import com.embabel.chat.Conversation;
import com.embabel.chat.UserMessage;

import java.util.Map;

/**
 * Defines chat-focused Embabel actions that the utility chatbot can invoke.
 * <p>
 * In this project, a chatbot is not implemented as a giant "chat loop" method.
 * Instead, Embabel discovers {@link Action}-annotated methods on
 * {@link EmbabelComponent} classes and runs them when their trigger matches.
 * This class contributes one such action: respond to each {@link UserMessage}.
 * The model can call retrieval tools via {@link ToolishRag}, enabling agentic
 * retrieval rather than a fixed retrieve-then-generate pipeline.
 */
@EmbabelComponent
public class ChatActions {

    /**
     * RAG facade exposed to the LLM as a safe, high-level reference named "sources".
     * <p>
     * Internally this facade wraps {@link SearchOperations} and exposes tool-style
     * retrieval operations that the model can invoke when needed.
     */
    private final ToolishRag toolishRag;

    /**
     * Runtime configuration bound from {@code ragbot.*} properties.
     */
    private final RagbotProperties properties;

    /**
     * Creates action handlers and wires retrieval capabilities.
     * <p>
     * Best practice: keep retrieval setup in the constructor so action methods remain
     * focused on orchestration (input -> AI call -> output), making them easier to read
     * and test.
     *
     * @param searchOperations vector and metadata search abstraction consumed by
     *                         {@link ToolishRag}
     * @param properties application-level chatbot configuration
     */
    public ChatActions(
            SearchOperations searchOperations,
            RagbotProperties properties) {
        this.toolishRag = new ToolishRag(
                "sources",
                "Classic music criticism",
                searchOperations);
        this.properties = properties;
    }

    /**
     * Responds to an incoming user message using the configured LLM and RAG reference.
     * <p>
     * The method is package-private on purpose: visibility does not affect Embabel's
     * ability to discover and invoke {@link Action} methods, but narrower visibility
     * keeps the API surface small.
     * The {@code trigger = UserMessage.class} setting runs this action whenever a
     * user message arrives, and {@code canRerun = true} allows repeated execution
     * over the same long-lived conversation.
     *
     * @param conversation current conversation containing prior user/assistant messages
     * @param context action execution context used to call AI and publish the new message
     */
    @Action(
            canRerun = true,
            trigger = UserMessage.class
    )
    void respond(
            Conversation conversation,
            ActionContext context) {
        // We could use a simple prompt here but choose to use a template
        // as chatbots tend to require longer prompts
        var assistantMessage = context.
                ai()
                .withLlm(properties.chatLlm())
                .withReference(toolishRag)
                .rendering("ragbot")
                .respondWithSystemPrompt(conversation, Map.of(
                        "properties", properties,
                        "voice", properties.voice(),
                        "objective", properties.objective()
                ));
        context.sendMessage(conversation.addMessage(assistantMessage));
    }
}
