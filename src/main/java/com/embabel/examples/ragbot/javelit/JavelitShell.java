package com.embabel.examples.ragbot.javelit;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Shell commands for the Javelit web-based chat UI.
 */
@ShellComponent
public record JavelitShell(JavelitChatUI javelitChatUI) {

    @ShellMethod(value = "Launch web-based chat UI", key = "uichat")
    public String uichat(
            @ShellOption(defaultValue = "0", help = "Port number (0 uses default from config)") int port) {
        if (javelitChatUI.isRunning()) {
            return "Chat UI is already running. Use 'uichat-stop' to stop it first.";
        }

        String url = port > 0 ? javelitChatUI.start(port) : javelitChatUI.start();
        return "Chat UI started at " + url + "\nOpen this URL in your browser to chat.";
    }

    @ShellMethod(value = "Stop the web-based chat UI", key = "uichat-stop")
    public String uichatStop() {
        if (!javelitChatUI.isRunning()) {
            return "Chat UI is not running.";
        }

        javelitChatUI.stop();
        return "Chat UI stopped.";
    }

    @ShellMethod(value = "Check if web-based chat UI is running", key = "uichat-status")
    public String uichatStatus() {
        if (javelitChatUI.isRunning()) {
            return "Chat UI is running.";
        } else {
            return "Chat UI is not running. Use 'uichat' to start it.";
        }
    }
}
