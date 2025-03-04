package ai.mindgard.sandbox.wsapi.messages;

public record OrchestratorSetupRequest(
    String target,
    int parallelism,
    String system_prompt,
    String dataset,
    String custom_dataset,
    String modelType,
    String attackSource,
    String attackPack,
    Object labels
) {}
