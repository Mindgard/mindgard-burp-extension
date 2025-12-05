package ai.mindgard.sandbox.wsapi;

import ai.mindgard.sandbox.wsapi.messages.CliInitResponse;

import java.net.http.WebSocket;

public record SandboxConnection(CliInitResponse cliInitResponse, WebSocket ws, MindgardWebsocketClient client) {

    /**
     * Sends a reply message through the WebSocket connection.
     * @param correlationId the correlation ID of the message being replied to
     * @param reply the reply message content
     */
    public void reply(String correlationId, String reply) {
        if (ws != null && this.client != null) {
            this.client.reply(ws, correlationId, reply);
        }
    }
}

