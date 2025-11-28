package ai.mindgard.sandbox;

import ai.mindgard.Log;
import ai.mindgard.MindgardAuthentication;
import ai.mindgard.MindgardSettings;
import ai.mindgard.sandbox.wsapi.SandboxConnection;
import ai.mindgard.sandbox.wsapi.SandboxConnectionFactory;
import ai.mindgard.MindgardServerConfiguration;

import java.net.http.HttpClient;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class Mindgard {
    private final Log logger;
    private final MindgardAuthentication auth;
    private MindgardSettings settings;
    private MindgardServerConfiguration uriManager;
    private final Supplier<SandboxConnectionFactory> connectionFactory;
    private BlockingDeque<Probe> newProbes = new LinkedBlockingDeque<>();
    private Pending pending = new Pending();

    private boolean started = false;
    private boolean doneSomething = false;
    private SandboxConnection connection;

    public Mindgard(Log logger, MindgardAuthentication auth, MindgardSettings settings) {
        this(logger, auth, settings, () -> new SandboxConnectionFactory(settings, HttpClient.newHttpClient()));
    }

    public Mindgard(Log logger, MindgardAuthentication auth, MindgardSettings settings, Supplier<SandboxConnectionFactory> connectionFactory) {
        this.logger = logger;
        this.auth = auth;
        this.settings = settings;
        this.connectionFactory = connectionFactory;
        this.uriManager = new MindgardServerConfiguration(this.settings);
    }

    public void startGeneratingProbes(){
        this.started = true;
        this.connection = connectionFactory.get().connect(this, auth, settings,logger);
    }

    public void reset() {
        started = false;
        newProbes = new LinkedBlockingDeque<>();
        doneSomething = false;
        pending = new Pending();
    }

    public Optional<Probe> poll() {
        return poll(60L);
    }

    public Optional<Probe> poll(Long timeoutSeconds) {
        try {
            return Optional.ofNullable(pending(newProbes.poll(timeoutSeconds, TimeUnit.SECONDS)));
        } catch (InterruptedException e) {
            return Optional.empty();
        }
    }

    private Probe pending(Probe popped) {
        if (popped != null) {
            pending.add(popped);
        }
        return popped;
    }

    public Probe reply(String correlationId, String reply) {
        Probe probe = pending.get(correlationId);
        pending.clear(correlationId);
        if (this.connection != null) {
            this.connection.reply(correlationId, reply);
        }
        return probe;
    }

    public boolean hasMoreProbes() {
        return !doneSomething || !newProbes.isEmpty();
    }

    public List<Probe> pendingProbes() {
        return pending.list();
    }

    public boolean isStarted() {
        return started;
    }

    public void push(Probe probe) {
        newProbes.push(probe);
        this.doneSomething = true;
    }
}
