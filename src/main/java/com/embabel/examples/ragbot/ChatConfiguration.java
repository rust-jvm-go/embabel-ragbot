package com.embabel.examples.ragbot;

import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.Verbosity;
import com.embabel.chat.Chatbot;
import com.embabel.chat.agent.AgentProcessChatbot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the chatbot facade used by shell and web UI.
 * <p>
 * The bean created here uses Embabel's "utility" chatbot mode, which discovers
 * available {@code @Action} methods from {@code @EmbabelComponent} beans on the
 * {@link AgentPlatform} and chooses suitable actions when messages arrive.
 * This keeps chat behavior aligned with Embabel's action-based architecture,
 * rather than a custom chat loop implementation.
 */
@Configuration
class ChatConfiguration {

    /**
     * Creates the chat configuration bean container.
     * <p>
     * Spring instantiates this configuration class during context startup.
     */
    ChatConfiguration() {
    }

    /**
     * Builds a chatbot backed by the shared {@link AgentPlatform}.
     * <p>
     * Best practice: expose chatbot creation as a dedicated bean so transport layers
     * (Spring Shell, HTTP UI, tests) can all reuse the same behavior.
     * The underlying {@code utilityFromPlatform(...)} setup automatically discovers
     * actions and lets the runtime choose an appropriate action per incoming message.
     *
     * @param agentPlatform central Embabel runtime containing discovered actions and tools
     * @return Returns a chatbot that routes conversation messages through Embabel action handling
     */
    @Bean
    Chatbot chatbot(AgentPlatform agentPlatform) {
        return AgentProcessChatbot.utilityFromPlatform(
                agentPlatform,
                new Verbosity().showPrompts()
        );
    }
}
