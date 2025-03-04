package ai.mindgard.sandbox.wsapi.messages;

public record ReplyData(String correlationId, String messageType, String status,
                        ResponsePayload payload) {
}
