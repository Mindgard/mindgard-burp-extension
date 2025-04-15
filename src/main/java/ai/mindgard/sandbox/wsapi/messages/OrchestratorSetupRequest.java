package ai.mindgard.sandbox.wsapi.messages;
import java.util.List;

public record OrchestratorSetupRequest(
    String target,
    int parallelism,
    String system_prompt,
    String dataset,
    String custom_dataset,
    String modelType,
    String attackSource,
    String attackPack,
    Object labels,
    List<String> exclude,
    List<String> include,
    Integer prompt_repeats
) {}
