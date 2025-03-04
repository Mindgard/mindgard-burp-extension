package ai.mindgard.sandbox.wsapi.messages;

import ai.mindgard.sandbox.wsapi.messages.ReplyData;

public record SendResponseToGroup(String type, String group, ReplyData data, String dataType) {
}
