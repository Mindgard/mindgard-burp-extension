package ai.mindgard.sandbox;

import ai.mindgard.sandbox.Probe;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Pending {

    private ConcurrentHashMap<String, Probe> probes = new ConcurrentHashMap<>();

    public Probe get(String correlationId) {
        return probes.get(correlationId);
    }

    public void add(Probe probe) {
        probes.put(probe.correlationId(), probe);
    }

    public void clear(String correlationId) {
        probes.remove(correlationId);
    }

    public List<Probe> list() {
        return probes.values().stream().toList();
    }
}
