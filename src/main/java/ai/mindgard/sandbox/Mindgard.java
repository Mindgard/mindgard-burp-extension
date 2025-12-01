package ai.mindgard.sandbox;

import java.util.List;
import java.util.Optional;

public interface Mindgard {
    void startGeneratingProbes();

    void reset();

    Optional<Probe> poll();

    Probe reply(String correlationId, String reply);

    List<Probe> pendingProbes();

    boolean isStarted();

    void push(Probe probe);
}
