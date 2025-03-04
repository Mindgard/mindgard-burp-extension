package ai.mindgard.sandbox.wsapi.messages;

import ai.mindgard.sandbox.wsapi.messages.GroupId;

public record WSPayload(String correlationId, String messageType, GroupId payload) {
}
