package com.embabel.examples.ragbot.javelit;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Exposes Spring Shell commands for controlling the Javelit browser chat interface.
 * <p>
 * This class acts as a thin adapter around {@link JavelitChatUI}, exposing
 * start/stop/status operations as easy-to-discover shell commands.
 * In the documented learning flow, users typically run shell ingestion commands
 * first, then launch this UI with {@code uichat} for interactive conversations.
 *
 * @param javelitChatUI runtime component that manages the embedded web server
 */
@ShellComponent
public record JavelitShell(JavelitChatUI javelitChatUI) {

    /**
     * Starts the chat web UI.
     * <p>
     * Passing {@code 0} keeps startup aligned with configuration defaults
     * (commonly port 8888 in this project).
     *
     * @param port requested HTTP port; {@code 0} means use configured default
     * @return Returns startup message including the URL to open in a browser
     */
    @ShellMethod(value = "Launch web-based chat UI", key = "uichat")
    public String uichat(
            @ShellOption(defaultValue = "0", help = "Port number (0 uses default from config)") int port) {
        if (javelitChatUI.isRunning()) {
            return "Chat UI is already running. Use 'uichat-stop' to stop it first.";
        }

        String url = port > 0 ? javelitChatUI.start(port) : javelitChatUI.start();
        return "Chat UI started at " + url + "\nOpen this URL in your browser to chat.";
    }

    /**
     * Stops the running chat web UI, if present.
     *
     * @return Returns status message indicating whether the UI was stopped or already inactive
     */
    @ShellMethod(value = "Stop the web-based chat UI", key = "uichat-stop")
    public String uichatStop() {
        if (!javelitChatUI.isRunning()) {
            return "Chat UI is not running.";
        }

        javelitChatUI.stop();
        return "Chat UI stopped.";
    }

    /**
     * Reports whether the web chat UI is currently running.
     *
     * @return Returns user-friendly running status text
     */
    @ShellMethod(value = "Check if web-based chat UI is running", key = "uichat-status")
    public String uichatStatus() {
        if (javelitChatUI.isRunning()) {
            return "Chat UI is running.";
        } else {
            return "Chat UI is not running. Use 'uichat' to start it.";
        }
    }
}
