package ai.mindgard.sandbox.wsapi.messages;

import java.util.List;

public record CliInitResponse(String groupId, String url, List<Error> errors) {
    public record Error(String message) {}
    public CliInitResponse(String groupId, String url) {
        this(groupId,url,null);
    }
}
