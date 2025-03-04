package ai.mindgard.sandbox.wsapi.messages;


public record MessageData(String correlationId, String messageType, Payload payload) {
}
