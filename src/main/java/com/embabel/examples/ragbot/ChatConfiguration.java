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
 */
@Configuration
class ChatConfiguration {

    /**
     * Builds a chatbot backed by the shared {@link AgentPlatform}.
     * <p>
     * Best practice: expose chatbot creation as a dedicated bean so transport layers
     * (Spring Shell, HTTP UI, tests) can all reuse the same behavior.
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
