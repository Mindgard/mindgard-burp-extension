package ai.mindgard.sandbox.wsapi.messages;

public record SendToGroup(String type, String group, WSPayload data, String dataType) {
}
