package ai.mindgard.sandbox.wsapi;

import ai.mindgard.sandbox.wsapi.messages.CliInitResponse;

import java.net.http.WebSocket;

public record SandboxConnection(CliInitResponse cliInitResponse, WebSocket ws, MindgardWebsocketClient client) {

    public void reply(String correlationId, String reply) {
        if (ws != null && this.client != null) {
            this.client.reply(ws, correlationId, reply);
        }
    }
}

