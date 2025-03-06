package ai.mindgard.sandbox.wsapi.messages;

public record WSPayload(String correlationId, String messageType, GroupId payload) {
}
