package ai.mindgard.sandbox.wsapi.messages;

public record Message(String type, String event, String userId, String connectionId, String from, String fromUserId,
                      String group, String dataType, MessageData data) {
}
