package ai.mindgard.sandbox.wsapi.messages;
import java.util.List;

public record OrchestratorSetupRequest(
    String projectID,
    Integer parallelism,
    String system_prompt,
    String dataset_id,
    String dataset_data,
    String modelType,
    String attackSource,
    String attackPack,
    Object labels,
    List<String> exclude,
    List<String> include,
    Integer prompt_repeats
) {}
