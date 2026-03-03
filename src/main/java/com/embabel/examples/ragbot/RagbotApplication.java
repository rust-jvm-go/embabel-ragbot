/*
 * Copyright 2024-2025 Embabel Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.embabel.examples.ragbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Bootstraps the Ragbot learning application.
 * <p>
 * This class bootstraps the Spring context. Chatbot behavior is assembled from
 * discovered beans such as Embabel actions and configuration classes.
 * The default runtime experience is Spring Shell for ingestion and chat commands,
 * with an optional browser UI started from shell commands when desired.
 */
@SpringBootApplication
class RagbotApplication {

    /**
     * Creates the application bootstrap class.
     * <p>
     * Spring uses this type as the primary source for component scanning and startup configuration.
     */
    RagbotApplication() {
    }

    /**
     * Starts the application.
     *
     * @param args command-line arguments passed to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(RagbotApplication.class, args);
    }
}
