package ai.mindgard.sandbox;

import ai.mindgard.Log;
import ai.mindgard.MindgardAuthentication;
import ai.mindgard.MindgardSettingsManager;
import ai.mindgard.sandbox.wsapi.SandboxConnection;
import ai.mindgard.sandbox.wsapi.SandboxConnectionFactory;

import java.net.http.HttpClient;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class MindgardWebSocketPrompts implements Mindgard {
    private final Log logger;
    private final MindgardAuthentication auth;
    private final MindgardSettingsManager mgsm;
    private final Supplier<SandboxConnectionFactory> connectionFactory;
    private BlockingDeque<Probe> newProbes = new LinkedBlockingDeque<>();
    private Pending pending = new Pending();

    private boolean started = false;
    private SandboxConnection connection;
    private final long pollTimeout;

    public MindgardWebSocketPrompts(Log logger, MindgardAuthentication auth, MindgardSettingsManager mgsm, long pollTimeoutSeconds) {
        this(logger, auth, mgsm, pollTimeoutSeconds, () -> new SandboxConnectionFactory(HttpClient.newHttpClient()));
    }

    public MindgardWebSocketPrompts(Log logger, MindgardAuthentication auth, MindgardSettingsManager mgsm, long pollTimeoutSeconds, Supplier<SandboxConnectionFactory> connectionFactory) {
        this.logger = logger;
        this.auth = auth;
        this.mgsm = mgsm;
        this.connectionFactory = connectionFactory;
        this.pollTimeout = pollTimeoutSeconds;
    }

    @Override
    public void startGeneratingProbes(){
        if (!mgsm.validLogin()) {
            logger.log("["+ Instant.now() + "] ERROR: User not logged in.");
            javax.swing.SwingUtilities.invokeLater(() -> {
                javax.swing.JOptionPane.showMessageDialog(null,
                        "You must be logged into your Mindgard tenant to use this generator.\nPlease log in on the Mindgard tab.",
                        "Mindgard Settings Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
            });
        }
        this.started = true;
        this.connection = connectionFactory.get().connect(this, auth, mgsm.getSettings() ,logger);
        logger.log("["+ Instant.now() + "] [ws] Starting probe generation");
    }

    @Override
    public void reset() {
        started = false;
        newProbes = new LinkedBlockingDeque<>();
        pending = new Pending();
    }

    @Override
    public Optional<Probe> poll() {
        return poll(pollTimeout);
    }

    private Optional<Probe> poll(Long timeoutSeconds) {
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

    @Override
    public Probe reply(String correlationId, String reply) {
        Probe probe = pending.get(correlationId);
        pending.clear(correlationId);
        if (this.connection != null) {
            this.connection.reply(correlationId, reply);
        }
        return probe;
    }

    @Override
    public List<Probe> pendingProbes() {
        return pending.list();
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public void push(Probe probe) {
        newProbes.push(probe);
    }
}
